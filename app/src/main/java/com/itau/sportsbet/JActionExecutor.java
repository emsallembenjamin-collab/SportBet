package com.itau.sportsbet;


import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;

//.*=============================================================
//.class: JAction
//.desc: specific one action
//.
class JAction {
    public String name;
    public String type;
    public ArrayList<String> string_param_list;
    public ArrayList<Double> digit_param_list;
    public int delay = 0;
    public ArrayList<String> branch_success;
    public ArrayList<String> branch_fail;


    //. important. which param used? mine or prev result?
    boolean run_as_prev_success = true;
    //. memory result after execute action
    //. sure remember. in result_rects, must original coord x,y
    public ArrayList<Rect> result_rects = new ArrayList<Rect>();
    //. note. result_rects-> is original image coord size...
    public String result_string;


    public Rect parseRectParam(){
        //. make screen shot.

        int limitWidth = JUtilFunctions.screenshot.cols();
        int limitHeight = JUtilFunctions.screenshot.rows();

        //. first. parsing rect info.
        int x,y,right,bottom;
        double dx,dy,dright,dbottom;

        dx = digit_param_list.get(0).doubleValue();
        dy = digit_param_list.get(1).doubleValue();
        dright = digit_param_list.get(2).doubleValue();
        dbottom = digit_param_list.get(3).doubleValue();
        if (dx < 1 && dy < 1 && dright <= 1 && dbottom <= 1){
            x = (int)(limitWidth * dx);
            y = (int)(limitHeight * dy);
            right = (int)(limitWidth * dright);
            bottom = (int)(limitHeight * dbottom);
        }
        else {
            x = (int)(dx);
            y = (int)(dy);
            right = (int)(dright);
            bottom = (int)(dbottom);
        }

        Rect rcForAnalyse = new Rect(x, y, right - x, bottom - y);
        return rcForAnalyse;
    }

    //.*==========================================================
    //.func: run
    //.desc: run one action...
    //.
    public boolean run(JAction prevAction){
        boolean bFinished = false;

        Log.d("PPPP Action: " + name, "Started!");

        switch(type){
            case "go_website":{
                String site_url = string_param_list.get(0);
                JUtilFunctions.launchChrome(MyAccessibilityService.mainService, site_url);
                JUtilFunctions.delay_duration(delay);
                result_string = "success";
            }
            break;
            case "do_ocr":{
                //. first, get target region.
                int nDigitCnt = digit_param_list.size();
                if (nDigitCnt != 5){
                   //. invalid param...
                    result_string = "fail";
                }
                else{
                    JUtilFunctions.takeScreenshot();

                    //. first . get analyse Rect
                    Rect rcForAnalyse = parseRectParam();

                    //. second. get sub rect and mat.
                    Mat analyseAreaMat = JUtilFunctions.screenshot.submat(rcForAnalyse);

                    //. get text detector...
                    ArrayList<Rect> rcTexts = new ArrayList<Rect>();
                    int nTextRegionCnt = JUtilFunctions.textNormalDetector.do_detect(analyseAreaMat, rcTexts, 0);
                    if (nTextRegionCnt > 0){
                        float fResizeRate = digit_param_list.get(4).floatValue();

                        //. get original text rect and image.
                        Rect rcForOcr = JUtilFunctions.getOrigRectFromBaseRect(rcForAnalyse);

                        Mat ocrAreaMat = JUtilFunctions.originScreenShot.submat(rcForOcr);
                        for (int k = 0; k < rcTexts.size(); k++){
                            Rect rc = rcTexts.get(k);
                            JUtilFunctions.changeToOrigRectFromBaseRect(rc);
                        }

                        result_string = JUtilFunctions.do_ocr(ocrAreaMat, rcTexts, fResizeRate,
                                result_rects, string_param_list, 0);
                        //. must do offset operation.
                        if (result_string.equals("success")){
                            int result_rect_cnt = result_rects.size();
                            for (int i = 0; i < result_rect_cnt; i++){
                                Rect rc = result_rects.get(i);
                                rc.x += rcForOcr.x;
                                rc.y += rcForOcr.y;
                            }
                        }
                    }
                }

                JUtilFunctions.delay_duration(delay);

            }
                break;
            case "do_click":{

                boolean bInvalidParam = false;

                //. 2024-2-29.
                //. for indicate used index of prev's result_rects...
                int nPrevRectIdxForUse = 0;
                int nStringParamCnt = string_param_list.size();
                if (nStringParamCnt > 0){
                    nPrevRectIdxForUse = Integer.parseInt(string_param_list.get(0));
                    int nRectCnt = prevAction.result_rects.size();

                    //. decide to last order...
                    if (nPrevRectIdxForUse < 0){
                        nPrevRectIdxForUse = nRectCnt + nPrevRectIdxForUse;
                    }

                    //. you must guarantee nPrevRectIdxForUse's range in action script...
                }

                int nRepeatCnt = 1;
                Point pt = null;
                int nDigitParamCnt = digit_param_list.size();
                switch(nDigitParamCnt){
                    case 1: {
                        nRepeatCnt = digit_param_list.get(0).intValue();
                        int nRectCnt = prevAction.result_rects.size();
                        if (nRectCnt < nPrevRectIdxForUse + 1) {
                            bInvalidParam = true;
                        } else {
                            Rect rc = prevAction.result_rects.get(nPrevRectIdxForUse);
                            pt = JUtilFunctions.getCenterPoint(rc);
                        }
                    }
                        break;
                    case 2:{
                        double x = digit_param_list.get(0).doubleValue();
                        double y = digit_param_list.get(1).doubleValue();
                        if (x <= 1 && y <= 1){
                            pt = new Point(x * Config.Screen_Width, y * Config.Screen_Height);
                        } else{
                            pt = JUtilFunctions.getOrigPointFromBasePoint(x, y);
                        }
                    }
                        break;
                    case 3:{
                        double x = digit_param_list.get(0).doubleValue();
                        double y = digit_param_list.get(1).doubleValue();
                        if (x <= 1 && y <= 1){
                            pt = new Point(x * Config.Screen_Width, y * Config.Screen_Height);
                        } else{
                            pt = JUtilFunctions.getOrigPointFromBasePoint(x, y);
                        }
                        nRepeatCnt = digit_param_list.get(2).intValue();
                        }
                        break;
                    case 4:{
                        int nRectCnt = prevAction.result_rects.size();
                        if (nRectCnt < nPrevRectIdxForUse + 1){
                            bInvalidParam = true;
                        }
                        else{
                            Rect rc = prevAction.result_rects.get(nPrevRectIdxForUse);
                            Point ptCenter = JUtilFunctions.getCenterPoint(rc);

                            nRepeatCnt = digit_param_list.get(0).intValue();
                            int nBaseType = digit_param_list.get(1).intValue();
                            if (nBaseType == 1){
                                ptCenter.y = rc.y;
                            }
                            else if (nBaseType == 2){
                                ptCenter.y = rc.y + rc.height;
                            }
                            double offsetX = digit_param_list.get(2).doubleValue();
                            double offsetY = digit_param_list.get(3).doubleValue();
                            pt = new Point(ptCenter.x + offsetX / Config.resizeXRatio, ptCenter.y + offsetY / Config.resizeYRatio);

                        }
                    }
                    break;
                    default:
                        bInvalidParam = true;
                        break;
                }
                if (bInvalidParam){
                    result_string = "fail";
                }
                else{
                    for (int i = 0; i < nRepeatCnt; i++){
                        JUserActions.dispatchTap(pt.x, pt.y);
                        JUtilFunctions.delay_duration(200);
                    }
                    result_string = "success";
                }

                JUtilFunctions.delay_duration(delay);
            }
                break;
            case "do_serial_input":{

                result_string = "fail";
                String targetString = null;
                int nStrParamCnt = string_param_list.size();
                if (nStrParamCnt == 1){
                    targetString = string_param_list.get(0);
                }
                else if (nStrParamCnt == 0){
                    //. use prev node branch...

                    ArrayList<String> command = null;
                    if (run_as_prev_success == true){
                        command = prevAction.branch_success;
                    }
                    else{
                        command = prevAction.branch_fail;
                    }

                    int nPrevParamCnt = command.size();

                    if (nPrevParamCnt == 2){
                        targetString = command.get(1);
                        if (targetString.equals("$user_id") )
                            targetString = MyAccessibilityService.mainService.loadTask.user_id;
                        else if (targetString.equals("$password"))
                            targetString = MyAccessibilityService.mainService.loadTask.password;
                    }
                }
                else{
                    //. invalid param.
                }

                if (targetString != null){
                    JUserActions.copyTextToClipboard(MyAccessibilityService.mainService, targetString);
                    JUtilFunctions.delay_duration(100);
                    boolean bSuccessPaste = JUserActions.pasteTextFromClipboard(MyAccessibilityService.mainService);
                    if (bSuccessPaste == true){
                        result_string = "success";
                    }
                }

                JUtilFunctions.delay_duration(delay);
            }
                break;
            case "do_find_color_bar":{

                //. 2024-2-29
                //. parse string param list...
                boolean bCondBranch = false;
                boolean bGreatCond = false;
                int     nThres = 0;
                int nStringParamCnt = string_param_list.size();
                if (nStringParamCnt == 2){
                    String strCond = string_param_list.get(0);
                    if (strCond.equals("great")){
                        bGreatCond = true;
                        bCondBranch = true;
                        nThres = Integer.parseInt(string_param_list.get(1));
                    }
                    else if (strCond.equals("less")){
                        bGreatCond = false;
                        bCondBranch = true;
                        nThres = Integer.parseInt(string_param_list.get(1));
                    }
                    else{
                        result_string = "exception";
                        break;
                    }
                }


                int nDigitCnt = digit_param_list.size();
                if (nDigitCnt != 11){
                    //. invalid param.
                    result_string = "fail";
                }
                else{
                    JUtilFunctions.takeScreenshot();

                    //. first . get analyse Rect
                    Rect rcForAnalyse = parseRectParam();
                    boolean bVert = false;
                    boolean bContinue = true;
                    int startVal = 0,endVal = 0;
                    int fixedVal = 0;
                    if (rcForAnalyse.width == 0) {
                        bVert = true;
                        startVal = rcForAnalyse.y;
                        endVal = rcForAnalyse.y + rcForAnalyse.height;
                        fixedVal = rcForAnalyse.x;
                    }
                    else if (rcForAnalyse.height == 0) {
                        bVert = false;
                        startVal = rcForAnalyse.x;
                        endVal = rcForAnalyse.x + rcForAnalyse.width;
                        fixedVal = rcForAnalyse.y;
                    }
                    else {
                        result_string = "fail";
                        bContinue = false;
                    }

                    if (bContinue == true){

                        //. first . prepare return rect list.
                        result_rects.clear();

                        // Define the color to find (RGB: 29, 27, 24)
                        // OpenCV uses BGR color ordering but now set param in order "RGB".
                        Point3 targetUpColor = new Point3(digit_param_list.get(4).intValue(),
                                digit_param_list.get(5).intValue(), digit_param_list.get(6).intValue());
                        Point3 targetDownColor = new Point3(digit_param_list.get(7).intValue(),
                                digit_param_list.get(8).intValue(), digit_param_list.get(9).intValue());

                        int nLimitLen = digit_param_list.get(10).intValue();

                        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                        fixedVal, startVal, endVal , bVert, targetUpColor, targetDownColor, nLimitLen);

                        //. filter by limit length.
                        int nSegCnt = retSegments.size();
                        for (int i = 0; i < nSegCnt; i++){
                            Point sc = retSegments.get(i);
                            Rect rcNew = null;
                            if (bVert == true){
                                Rect rcBase = new Rect(fixedVal, (int)(sc.x), 0, (int)(sc.y - sc.x));
                                rcNew = JUtilFunctions.getOrigRectFromBaseRect(rcBase);
                            }
                            else{
                                Rect rcBase = new Rect((int)sc.x, fixedVal, (int)(sc.y - sc.x), 0);
                                rcNew = JUtilFunctions.getOrigRectFromBaseRect(rcBase);
                            }
                            result_rects.add(rcNew);
                        }

                        int resultRectCnt = result_rects.size();
                        if (bCondBranch){
                            if (bGreatCond){
                                if (resultRectCnt >= nThres)
                                    result_string = "success";
                                else
                                    result_string = "fail";
                            }
                            else {
                                if (resultRectCnt < nThres)
                                    result_string = "success";
                                else
                                    result_string = "fail";
                            }
                        }
                        else{
                            if (resultRectCnt >0 )
                                result_string = "success";
                            else
                                result_string = "fail";
                        }

                        retSegments = null;
                    }
                }
                JUtilFunctions.delay_duration(delay);
            }
            break;
            case "do_input_user_id_password": {
                int nDigitParamCnt = digit_param_list.size();
                int nMaxRequireRectCnt = 2;
                for (int i = 0; i < nDigitParamCnt; i++){
                    int nRequstIndex = digit_param_list.get(0).intValue();
                    if (nRequstIndex + 1 > nMaxRequireRectCnt){
                        nMaxRequireRectCnt = nRequstIndex + 1;
                    }
                }
                int nPrevCnt = prevAction.result_rects.size();
                if (nPrevCnt < nMaxRequireRectCnt){
                    result_string = "fail";
                }
                else {
                    int nUserIdIndex = 0, nPasswordIndex = 1;
                    if (nDigitParamCnt == 2){
                        nUserIdIndex = digit_param_list.get(0).intValue();
                        nPasswordIndex = digit_param_list.get(1).intValue();
                    }

                    Rect rc1 = prevAction.result_rects.get(nUserIdIndex);
                    Rect rc2 = prevAction.result_rects.get(nPasswordIndex);

                    Point ptUser_id = JUtilFunctions.getCenterPoint(rc1);
                    Point ptPassword = JUtilFunctions.getCenterPoint(rc2);

                    //. first, paste user_id
                    String targetString = MyAccessibilityService.mainService.loadTask.user_id;
                    JUserActions.copyTextToClipboard(MyAccessibilityService.mainService, targetString);

                    JUserActions.dispatchTap(ptUser_id.x, ptUser_id.y);
                    JUtilFunctions.delay_duration(300);
                    //. need twice times.
                    JUserActions.dispatchTap(ptUser_id.x, ptUser_id.y);
                    JUtilFunctions.delay_duration(100);

                    JUserActions.dispatchLongClick((int)ptUser_id.x, (int)ptUser_id.y);
                    JUtilFunctions.delay_duration(100);

                    //. offset point
                    Point ptOffset = JUtilFunctions.getOrigPointFromBasePoint(80, -60);
                    JUserActions.dispatchTap(ptOffset.x, ptUser_id.y + ptOffset.y);
                    JUtilFunctions.delay_duration(100);

                    //. second, paste password...
                    targetString = MyAccessibilityService.mainService.loadTask.password;
                    JUserActions.copyTextToClipboard(MyAccessibilityService.mainService, targetString);

                    JUserActions.dispatchLongClick((int)ptPassword.x, (int)ptPassword.y);
                    JUtilFunctions.delay_duration(100);
                    JUserActions.dispatchTap(ptOffset.x, ptPassword.y + ptOffset.y);
                    JUtilFunctions.delay_duration(100);

                    result_string = "success";
                }

                //. 2024-2-29.
                //. clone prev prevAction.result_rects for me...
                result_rects.clear();
                for (int i = 0; i < nPrevCnt; i++){
                    Rect rcPrev = prevAction.result_rects.get(i);
                    Rect rcNew = rcPrev.clone();
                    result_rects.add(rcNew);
                }

                JUtilFunctions.delay_duration(delay);

            }
            break;
            case "do_repeat_scroll" :{
                int nStrParamCnt = string_param_list.size();
                if (nStrParamCnt != 1){
                    result_string = "fail do_repeat_scroll";
                }
                else{
                    int nRepeatCnt = 0;
                    String strParam = string_param_list.get(0);
                    if (strParam.equals("$category")){
                        nRepeatCnt = MyAccessibilityService.mainService.loadTask.category;
                    }
                    else{
                        nRepeatCnt = Integer.parseInt(strParam);
                    }

                    //. read digit params.
                    int nDigitParamCnt = digit_param_list.size();
                    if (nDigitParamCnt != 3){
                        result_string = "fail do_repeat_scroll";
                    }
                    else{
                        double dx = digit_param_list.get(0).doubleValue();
                        double dy = digit_param_list.get(1).doubleValue();
                        double dAmount = digit_param_list.get(2).doubleValue();

                        int x = 0 , y = 0;
                        int nAmount = 0;

                        if (dx <= 1.0 && dy <= 1.0){
                            x = (int)(dx * Config.Screen_Width);
                            y = (int)(dy * Config.Screen_Height);
                        }
                        else{
                            x = (int)(dx / Config.resizeXRatio);
                            y = (int)(dy / Config.resizeYRatio);
                        }

                        if (dAmount <= 1.0)
                            nAmount = (int)(dAmount * Config.Screen_Height);
                        else
                            nAmount = (int)(dAmount / Config.resizeYRatio);

                        Point ptStart = new Point(x, y);
                        for (int i = 0; i < nRepeatCnt; i++){
                            JUserActions.scrollToLong(ptStart, nAmount);
                        }

                        result_string = "success";
                    }
                }

                JUtilFunctions.delay_duration(delay);

            }
            break;
            case "do_input_verification_code": {
                int nDigitParamCnt = digit_param_list.size();
                int nMaxRequireRectCnt = 2;
                for (int i = 0; i < nDigitParamCnt; i++){
                    int nRequstIndex = digit_param_list.get(0).intValue();
                    if (nRequstIndex + 1 > nMaxRequireRectCnt){
                        nMaxRequireRectCnt = nRequstIndex + 1;
                    }
                }
                int nPrevCnt = prevAction.result_rects.size();
                if (nDigitParamCnt != 2 || nPrevCnt < nMaxRequireRectCnt){
                    result_string = "fail";
                }
                else {
                    int nIndexforClick = 0, nIndexforRecogRegion = 1;
                    nIndexforClick = digit_param_list.get(0).intValue();
                    nIndexforRecogRegion = digit_param_list.get(1).intValue();

                    Rect rcForClick = prevAction.result_rects.get(nIndexforClick);
                    Rect rcForRecog = prevAction.result_rects.get(nIndexforRecogRegion);

                    Point ptClick = JUtilFunctions.getCenterPoint(rcForClick);
                    Point ptRecog = JUtilFunctions.getCenterPoint(rcForRecog);

                    //. if rcForRecog is empty, then it must expand...
                    Rect rcForImage = null;
                    if (rcForRecog.width == 0 || rcForRecog.height == 0){
                        rcForImage = new Rect();
                        rcForImage.x = (int)(ptRecog.x - 180);
                        rcForImage.y = (int)(ptRecog.y - 50);
                        rcForImage.width = 360; rcForImage.height = 100;
                    }
                    else{
                        rcForImage = rcForRecog;
                    }
                    //. check boundary.
                    JUtilFunctions.checkRectBoundary(rcForImage , Config.IMAGE_WIDTH, Config.IMAGE_HEIGHT);

                    //. get bitmap of image rect and convert to base64 string.
                    Mat imageForRecog = JUtilFunctions.screenshot.submat(rcForImage);
                    Bitmap bmp = Bitmap.createBitmap(imageForRecog.cols(), imageForRecog.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(imageForRecog, bmp);
                    String base64Str = JUtilFunctions.convert(bmp);

                    //. call API to get recoged text.
                    String strVCode = MyAccessibilityService.mainService.loadTask.getVerificationCode(base64Str);
                    if (strVCode != "fail"){

                        //. input verification code...
                        JUserActions.dispatchTap(ptClick.x, ptClick.y);
                        JUtilFunctions.delay_duration(300);
                        JUserActions.dispatchKeyPress(strVCode);

                        result_string = "success";
                    }
                    else{
                        result_string = "fail";
                    }
              }

              JUtilFunctions.delay_duration(delay);

            }
            break;
            case "the_end":{
                ArrayList<String> command = null;
                if (run_as_prev_success == true){
                    command = prevAction.branch_success;
                }
                else{
                    command = prevAction.branch_fail;
                }

                int nBranchParamCnt = command.size();

                //. find "the_end" action in two branches.
                if (nBranchParamCnt == 2){
                    result_string = command.get(1);
                }
                else{
                    //. invalid param...
                }
                JUtilFunctions.delay_duration(delay);
                bFinished = true;
            }
                break;
            default:
                Log.d("PPPP Unknown Action: " + type, "Finded!");
                break;

        }

        Log.d("PPPP Action: " + name, "Ended! : " + result_string);
        return bFinished;
    }

    //.*==========================================================
    //.func: build
    //.desc: make itself...
    //.
    public static JAction build(JSONObject jobject) throws JSONException {

        JAction retAction = new JAction();
        retAction.name = jobject.getString("name");
        retAction.type = jobject.getString("type");

        JSONArray jstring_param_list = jobject.getJSONArray("string_param_list");
        retAction.string_param_list = new ArrayList<String>();
        for (int j = 0; j < jstring_param_list.length(); j++) {
            retAction.string_param_list.add(jstring_param_list.getString(j));
        }

        JSONArray jdigit_param_list = jobject.getJSONArray("digit_param_list");
        retAction.digit_param_list = new ArrayList<Double>();
        for (int j = 0; j < jdigit_param_list.length(); j++) {
            retAction.digit_param_list.add(jdigit_param_list.getDouble(j));
        }

        retAction.delay = jobject.getInt("delay");

        //. passing "branch"
        JSONObject branch = jobject.getJSONObject("branch");

        JSONArray jsuccess = branch.getJSONArray("success");
        retAction.branch_success = new ArrayList<String>();
        for (int j = 0; j < jsuccess.length(); j++) {
            retAction.branch_success.add(jsuccess.getString(j));
        }
        JSONArray jfail = branch.getJSONArray("fail");
        retAction.branch_fail = new ArrayList<String>();
        for (int j = 0; j < jfail.length(); j++) {
            retAction.branch_fail.add(jfail.getString(j));
        }

        return retAction;
    }


};

//.*=============================================================
//.class: JActionList
//.desc: specific actions list
//.
class JActionList extends ArrayList<JAction> {

    public JAction find(String name) {
        JAction retAction = null;
        int nActionCnt = size();
        for (int i = 0; i < nActionCnt; i++){
            JAction act = get(i);
            if (act.name.equals(name)){
                retAction = act;
                break;
            }
        }
        return retAction;
    }

    public JAction first(){
        JAction retAction = null;
        int nActionCnt = size();
        if (nActionCnt > 0){
            retAction = get(0);
        }
        return retAction;
    }

    public JAction next(JAction act) {

        boolean bRunAsPrevSuccess;
        JAction retAction = null;
        ArrayList<String> command = null;
        if (act.result_string.equals("success")){
            command = act.branch_success;
            bRunAsPrevSuccess = true;
        }
        else if (act.result_string.equals("exception")){
            return null;
        }
        else{
            command = act.branch_fail;
            bRunAsPrevSuccess = false;
        }

        int nParamCnt = command.size();
        if (nParamCnt > 0){
            String nextActName = command.get(0);
            retAction = find(nextActName);
            if (retAction != null){
                retAction.run_as_prev_success = bRunAsPrevSuccess;
            }
        }

        return retAction;
    }
};




//.*=============================================================
//.class: JActionExecutor
//.desc: Main class of action executing
//.
public class JActionExecutor {

    public JActionList actionList;

    //. need limit time untill done
    public static int  limitTime = 90000;       //. ms, default 1.5min 60000 * 1.5

    //.*=============================================================
    //.func: build
    //.desc: important...
    //.
    public static JActionExecutor build(String jsonString){
        JActionExecutor actionExecutor = null;

        try {
            JActionList newActionList = new JActionList();

            JSONObject root = new JSONObject(jsonString);
            JSONArray action_list = root.getJSONArray("action_list");

            int nActionCnt = action_list.length();
            for (int i = 0; i < nActionCnt; i++) {
                JSONObject jobject = action_list.getJSONObject(i);
                JAction newAction = JAction.build(jobject);
                newActionList.add(newAction);
            }

            //. success parsing...
            //. so, you can create object...
            actionExecutor = new JActionExecutor();
            actionExecutor.actionList = newActionList;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return actionExecutor;
    }

    //.*=============================================================
    //.func: run
    //.desc: run sequential action list.
    //.
    public String run(){
        String result = null;

        // Start measuring elapsed time
        final long startTime = SystemClock.elapsedRealtime();

        JAction actionNow = actionList.first();
        JAction actionPrev = null;
        while(actionNow != null){

            boolean bFinished = actionNow.run(actionPrev);
            result = actionNow.result_string;
            if (bFinished == true){
                break;
            }

            // Calculate elapsed time
            final long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTime;
            if (elapsedTimeMillis > JActionExecutor.limitTime){
                result = "time is over";
                break;
            }

            actionPrev = actionNow;
            actionNow = actionList.next(actionNow);
        }

        return result;
    }


    //.*=============================================================
    //.func: clear memory
    //.desc: clear action list.
    //.
    void clear_mem(){
        int nActionCnt = actionList.size();
        for (int i = 0; i < nActionCnt; i++)
        {
            JAction action = actionList.get(i);
            action = null;
        }
        actionList.clear();
    }

}
