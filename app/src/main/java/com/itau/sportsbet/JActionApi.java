package com.itau.sportsbet;


import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_DEL;
import static com.itau.sportsbet.Config.IgnorePartMode.e_IgnoreMode1;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeSpace;
import static com.itau.sportsbet.Config.TextDetMode.e_NormalTxtDet;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.util.ArrayList;


//.*=============================================================
//.func: JAction_Puseudo
//.desc: purpose: only create validator without action script
//.
class JAction_Puseudo extends JAction{

    JAction_Puseudo(String name, int timeLimit, int delay){
        this.name = name;
        this.type = "puseudo";
        this.time_limit = timeLimit;
        this.delay = delay;
    }
    @Override
    public boolean run_internel(JAction prevAction){
        //. always success.
        result_string = "success";
        return false;
    }
};

//.*=============================================================
//.func: JAction_Do_Calc
//.desc:
//.
class JAction_Do_Calc extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){

        boolean bInvalidParam = false;
        int strparamCnt = string_param_list.size();
        if (strparamCnt == 0){
            bInvalidParam = true;
        }
        else{
            String calc_type = string_param_list.get(0);
            switch(calc_type){
                case "rect_growth": {

                    int digitCnt = digit_param_list.size();
                    if (digitCnt != 6 || strparamCnt != 2){
                        bInvalidParam = true;
                        break;
                    }
                    Rect rcWork = JUtilFunctions.parseRectParam(digit_param_list,0);
                    int x_unit = digit_param_list.get(4).intValue();
                    int y_unit = digit_param_list.get(5).intValue();

                    float growth = 0;
                    String strSecondParam = string_param_list.get(1);
                    if (strSecondParam.equals("$category")){
                        growth = MyAccessibilityService.mainService.loadTask.category;
                    }
                    else{
                        growth = Float.parseFloat(strSecondParam);
                    }

                    rcWork.x += (int)(growth * x_unit);
                    rcWork.y += (int)(growth * y_unit);
                    JUtilFunctions.changeToOrigRectFromBaseRect(rcWork);

                    result_rects.clear();
                    result_rects.add(rcWork);
                }
                break;
                default:
                    break;
            }
        }

        if (bInvalidParam){
            result_string = "Invalid Param";
            executor.last_result_string = "Invalid Param: " + name;
            return true;
        }
        else{
            result_string = "success";
            executor.last_result_string = "success do_calc: " + name;
        }

        return false;
    }
};



//.*=============================================================
//.func: JAction_RunWebBrowser
//.desc:
//.
class JAction_RunWebBrowser extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){
        String site_url = string_param_list.get(0);
        JUtilFunctions.launchChrome(MyAccessibilityService.mainService, site_url);
        //. always success.
        result_string = "success";
        executor.last_result_string = "success run browser: " + name;
        return false;
    }
};

//.*=============================================================
//.func: JAction_Do_Ocr
//.desc:
//.
class JAction_Do_Ocr extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){
        //. first, get target region.
        int nDigitCnt = digit_param_list.size();
        if (nDigitCnt != 5){
            //. invalid param...
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            //. must finish all process...
            return true;
        }

        //. do screen shot...
        JUtilFunctions.takeScreenshot();

        //. first . get analyse Rect
        Rect rcForAnalyse = JUtilFunctions.parseRectParam(digit_param_list, 0);

        //. second. get sub rect and mat.
        Mat analyseAreaMat = JUtilFunctions.screenshot.submat(rcForAnalyse);

        //. get text detector...
        ArrayList<Rect> rcTexts = new ArrayList<Rect>();
        int nTextRegionCnt = JUtilFunctions.textNormalDetector.do_detect(analyseAreaMat, rcTexts, e_IgnoreMode1, e_NormalTxtDet);
        if (nTextRegionCnt > 0){
            float fResizeRate = digit_param_list.get(4).floatValue();

            //. get original text rect and image.
            Rect rcForOcr = JUtilFunctions.getOrigRectFromBaseRect(rcForAnalyse);
            Mat ocrAreaMat = JUtilFunctions.originScreenShot.submat(rcForOcr);
            for (int k = 0; k < nTextRegionCnt; k++){
                Rect rc = rcTexts.get(k);
                JUtilFunctions.changeToOrigRectFromBaseRect(rc);
            }

            result_string = JUtilFunctions.find_bestMatched_rectList_fromOcr(ocrAreaMat, rcTexts, result_rects, string_param_list);
            //. must do offset operation.
            if (result_string.equals("success")){
                JUtilFunctions.offsetRectList(result_rects, rcForOcr.x, rcForOcr.y);
                executor.last_result_string = "success do_ocr: " + name;
            }
            else{
                result_string = "fail do_ocr: " + name;
                executor.last_result_string = result_string;
            }
        }
        else{
            result_string = "fail do_ocr: Text Detector fail " + name;
            executor.last_result_string = result_string;
            // return true;
        }
        return false;
    }
};


//.*=============================================================
//.func: JAction_Do_Click
//.desc:
//.
class JAction_Do_Click extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){

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
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            //. must finish all process...
            return true;
        }
        else{
            for (int i = 0; i < nRepeatCnt; i++){
                JUserActions.dispatchTap(pt.x, pt.y);
                JUtilFunctions.delay_duration(100);
            }
            result_string = "success";
            executor.last_result_string = "success do_click: " + name;
        }

        return false;
    }
};


//.*=============================================================
//.func: JAction_Do_Find_ColorBar
//.desc:
//.
class JAction_Do_Find_ColorBar extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){

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
                result_string = "Invalid Param: " + name;
                executor.last_result_string = result_string;
                //. must finish all process.
                return true;
            }
        }


        int nDigitCnt = digit_param_list.size();
        if (nDigitCnt != 11){
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            //. must finish all process.
            return true;
        }

        JUtilFunctions.takeScreenshot();

        //. first . get analyse Rect
        Rect rcForAnalyse = JUtilFunctions.parseRectParam(digit_param_list, 0);

        JFuncParams_ColorBar param = new JFuncParams_ColorBar();
        param.bVert = false;
        if (rcForAnalyse.width == 0) {
            param.bVert = true;
            param.startVal = rcForAnalyse.y;
            param.endVal = rcForAnalyse.y + rcForAnalyse.height;
            param.fixedVal = rcForAnalyse.x;
        }
        else if (rcForAnalyse.height == 0) {
            param.bVert = false;
            param.startVal = rcForAnalyse.x;
            param.endVal = rcForAnalyse.x + rcForAnalyse.width;
            param.fixedVal = rcForAnalyse.y;
        }
        else {
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            //. must finish all process.
            return true;
        }

        //. first . prepare return rect list.
        result_rects.clear();

        // Define the color to find (RGB: 29, 27, 24)
        // OpenCV uses BGR color ordering but now set param in order "RGB".
        param.targetUpColor = new Point3(digit_param_list.get(4).intValue(),
                digit_param_list.get(5).intValue(), digit_param_list.get(6).intValue());
        param.targetDownColor = new Point3(digit_param_list.get(7).intValue(),
                digit_param_list.get(8).intValue(), digit_param_list.get(9).intValue());

        param.nMinLen = digit_param_list.get(10).intValue();

        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);

        //. save to result_rects.
        int nSegCnt = retSegments.size();
        for (int i = 0; i < nSegCnt; i++){
            Point sc = retSegments.get(i);
            Rect rcNew = null;
            if (param.bVert == true){
                Rect rcBase = new Rect(param.fixedVal, (int)(sc.x), 0, (int)(sc.y - sc.x));
                rcNew = JUtilFunctions.getOrigRectFromBaseRect(rcBase);
            }
            else{
                Rect rcBase = new Rect((int)sc.x, param.fixedVal, (int)(sc.y - sc.x), 0);
                rcNew = JUtilFunctions.getOrigRectFromBaseRect(rcBase);
            }
            result_rects.add(rcNew);
        }

        int resultRectCnt = result_rects.size();
        if (bCondBranch){
            if (bGreatCond){
                if (resultRectCnt >= nThres) {
                    result_string = "success";
                    executor.last_result_string = "success do_find_colorbar: " + name;
                }
                else {
                    result_string = "fail";
                    executor.last_result_string = "fail do_find_colorbar: " + name;
                }
            }
            else {
                if (resultRectCnt < nThres) {
                    result_string = "success";
                    executor.last_result_string = "success do_find_colorbar: " + name;
                }
                else {
                    result_string = "fail";
                    executor.last_result_string = "fail do_find_colorbar: " + name;
                }
            }
        }
        else{
            if (resultRectCnt >0 ) {
                result_string = "success";
                executor.last_result_string = "success do_find_colorbar: " + name;
            }
            else {
                result_string = "fail";
                executor.last_result_string = "fail do_find_colorbar: " + name;
            }
        }

        retSegments = null;

        return false;
    }
};

//.*=============================================================
//.func: JAction_Do_Input_Id_Password
//.desc: using system popup menu- Paste.
//.
class JAction_Do_Input_Id_Password extends JAction{

    public int nUserIdIndex = 0;
    public int nPasswordIndex = 1;
    public Point ptUser_id = null;
    public Point ptPassword = null;

    //. 2024-3-14
    //. in some sites.
    //. userID & password input window moved when clicked edit ctrl.
    //. because, browser shows notification bar in the bottom and some errors...
    //. so we must recalc userID & password window position per step click...
    public boolean recalcEditCtrlsPosition(){

        JUtilFunctions.delay_duration(500);
        //. in case, prevAction must not null.
        JAction myPrevAction = prevAction;
        boolean bRecalc = myPrevAction.run_internel(myPrevAction.prevAction);
        setEditCtrlPos();

        return bRecalc;
    }

    public boolean parseParams(){

        int nDigitParamCnt = digit_param_list.size();
        int nMaxRequireRectCnt = 2;
        for (int i = 0; i < nDigitParamCnt; i++){
            int nRequstIndex = digit_param_list.get(i).intValue();
            if (nRequstIndex + 1 > nMaxRequireRectCnt){
                nMaxRequireRectCnt = nRequstIndex + 1;
            }
        }
        int nPrevCnt = prevAction.result_rects.size();
        if (nPrevCnt < nMaxRequireRectCnt){
            return false;
        }
        if (nDigitParamCnt == 2){
            nUserIdIndex = digit_param_list.get(0).intValue();
            nPasswordIndex = digit_param_list.get(1).intValue();
        }

        return true;
    }

    public void setEditCtrlPos(){

        Rect rc1 = prevAction.result_rects.get(nUserIdIndex);
        Rect rc2 = prevAction.result_rects.get(nPasswordIndex);

        ptUser_id = JUtilFunctions.getCenterPoint(rc1);
        ptPassword = JUtilFunctions.getCenterPoint(rc2);
        ptPassword.x -= (150 / Config.resizeXRatio);
    }





    @Override
    public boolean run_internel(JAction prevAct){

        boolean bParsingParam = parseParams();
        if (bParsingParam == false){
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            //. must finish all process.
            return true;
        }
        setEditCtrlPos();

        //. first, paste user_id
        String targetString = MyAccessibilityService.mainService.loadTask.user_id;
        // JUserActions.copyTextToClipboard(MyAccessibilityService.mainService, targetString);
        JUserActions.copyTextToClipboardfromWorkThread(MyAccessibilityService.mainService, targetString);
        //JUtilFunctions.delay_duration(1000);

        JUserActions.dispatchTap(ptUser_id.x, ptUser_id.y);
        recalcEditCtrlsPosition();
        //. need twice times.
        JUserActions.dispatchTap(ptUser_id.x, ptUser_id.y);
        recalcEditCtrlsPosition();

        //. 2024-3-6. clear previous id
        JUserActions.deleteContentofInput(Config.max_userid_password_len);
        JUtilFunctions.delay_duration(1000);

        recalcEditCtrlsPosition();
        JUserActions.dispatchLongClick((int)ptUser_id.x, (int)ptUser_id.y);
        recalcEditCtrlsPosition();

        //. offset point
        Point ptOffset = JUtilFunctions.getOrigPointFromBasePoint(100, -60);
        JUserActions.dispatchTap(ptOffset.x, ptUser_id.y + ptOffset.y);
        recalcEditCtrlsPosition();


        ////////////////////////////////////////////////////////////
        //. second, paste password...
        targetString = MyAccessibilityService.mainService.loadTask.password;
        // JUserActions.copyTextToClipboard(MyAccessibilityService.mainService, targetString);
        JUserActions.copyTextToClipboardfromWorkThread(MyAccessibilityService.mainService, targetString);
        JUtilFunctions.delay_duration(1000);

        JUserActions.dispatchTap(ptPassword.x, ptPassword.y);
        recalcEditCtrlsPosition();

        //. 2024-3-6. clear previous id
        JUserActions.deleteContentofInput(Config.max_userid_password_len);
        JUtilFunctions.delay_duration(1000);

        recalcEditCtrlsPosition();
        JUserActions.dispatchLongClick((int)ptPassword.x, (int)ptPassword.y);
        recalcEditCtrlsPosition();

        JUserActions.dispatchTap(ptOffset.x, ptPassword.y + ptOffset.y);

        //. 2024-2-29.
        //. clone prev prevAction.result_rects for me...
        result_rects.clear();
        int nPrevCnt = prevAct.result_rects.size();
        for (int i = 0; i < nPrevCnt; i++){
            Rect rcPrev = prevAction.result_rects.get(i);
            Rect rcNew = rcPrev.clone();
            result_rects.add(rcNew);
        }

        result_string = "success";
        executor.last_result_string = "success do_input_id_password: " + name;
        return false;
    }
};



//.*=============================================================
//.func: JAction_Do_Repeat_Scroll
//.desc: do scroll repeatly...
//.
class JAction_Do_Repeat_Scroll extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){

        int nStrParamCnt = string_param_list.size();
        if (nStrParamCnt != 1){
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            return true;
        }

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
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            return true;
        }

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
            JUtilFunctions.delay_duration(100);
        }

        result_string = "success";
        executor.last_result_string = "success do_repeat_scroll: " + name;
        return false;
    }
};


//.*=============================================================
//.func: JAction_Do_Input_VerifiCode
//.desc:
//.
class JAction_Do_Input_VerifiCode extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){

        int nDigitParamCnt = digit_param_list.size();
        int nMaxRequireRectCnt = 2;
        for (int i = 0; i < nDigitParamCnt; i++){
            int nRequstIndex = digit_param_list.get(i).intValue();
            if (nRequstIndex + 1 > nMaxRequireRectCnt){
                nMaxRequireRectCnt = nRequstIndex + 1;
            }
        }
        int nPrevCnt = prevAction.result_rects.size();
        if (nDigitParamCnt != 2 || nPrevCnt < nMaxRequireRectCnt){
            result_string = "Invalid Param: " + name;
            executor.last_result_string = result_string;
            return true;
        }

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

        //. start action.
        //. first click edit.
        JUserActions.dispatchTap(ptClick.x, ptClick.y);
        JUtilFunctions.delay_duration(1000);

        //. maybe will show verification image...
        JUtilFunctions.takeScreenshot();
        Mat imageForRecog = JUtilFunctions.screenshot.submat(rcForImage);
        //. get text detector...
        ArrayList<Rect> rcTexts = new ArrayList<Rect>();
        int nTextRegionCnt = JUtilFunctions.textNormalDetector.do_detect(imageForRecog, rcTexts, e_IgnoreMode1, e_NormalTxtDet);
        if (nTextRegionCnt == 0){
            result_string = "fail Operation: " + name;
            executor.last_result_string = result_string;
            return true;
        }
        //. I believe our ocr and text detector...
        //. I use first rect.
        float fResizeRate = 2.0f;
        Rect rcTarget = rcTexts.get(0);
        Rect rcOriginal = JUtilFunctions.getOrigRectFromBaseRect(rcForImage);
        Rect rcForOcr = JUtilFunctions.getOrigRectFromBaseRect(rcTarget);
        JUtilFunctions.offsetRect(rcForOcr, rcOriginal.x, rcOriginal.y);

        String ocrStr = JUtilFunctions.readStringbyOcrfromFullImage(JUtilFunctions.originScreenShot, rcForOcr, fResizeRate);
        //. input serial text.
        JUserActions.dispatchKeyPress(ocrStr);
        result_string = "success";
        executor.last_result_string = "success do_input_verifiCode: " + name;

        return false;
    }
};


//.*=============================================================
//.func: JAction_FindClose_Ad
//.desc:
//.
class JAction_FindClose_Ad extends JAction{

    //. 2024-3-13
    //. customize ad types.
    public int type = 0;

    //. for 0.  white cross (X) mark in circle. in the middle and bottom position...
    public int thresBorder = 100;
    public int thresCircle = 50;
    public int minRadius = 15;
    public int maxRadius = 25;
    public Point3 borderColor = new Point3(255,255, 255);

    public Rect rcAnalyse = new Rect(220, 540, 100, 400);

    //. for 1. white cross (X) mark in right top part, but background image is shadowed...
    Point3  colorNormalBack = null;
    Point   pixelforBase = null;




    boolean bInitParams = false;
    Point   ptFindAdCenter = new Point();

    public boolean parseParamFromConfirmList(ArrayList<String> master_Confirmproc_params){
        boolean bInvalidParam = false;

        type = Integer.parseInt(string_param_list.get(0));

        switch(type){
            case 0:{
                int digitParamCnt = digit_param_list.size();
                if (digitParamCnt != 7){
                    bInvalidParam = true;
                }
                else{
                    thresBorder = digit_param_list.get(0).intValue();
                    thresCircle = digit_param_list.get(1).intValue();
                    minRadius = digit_param_list.get(2).intValue();
                    maxRadius = digit_param_list.get(3).intValue();

                    borderColor.x = digit_param_list.get(4).intValue();
                    borderColor.y = digit_param_list.get(5).intValue();
                    borderColor.z = digit_param_list.get(6).intValue();

                }
            }
            break;
            case 1:{
                int digitParamCnt = digit_param_list.size();
                if (digitParamCnt != 0){
                    bInvalidParam = true;
                }
                else{
                    int R = Integer.parseInt(master_Confirmproc_params.get(2));
                    int G = Integer.parseInt(master_Confirmproc_params.get(3));
                    int B = Integer.parseInt(master_Confirmproc_params.get(4));
                    int pX = Integer.parseInt(master_Confirmproc_params.get(5));
                    int pY = Integer.parseInt(master_Confirmproc_params.get(6));

                    colorNormalBack = new Point3(R, G, B);
                    pixelforBase = new Point(pX, pY);
                }
            }
            break;
        }
        return bInvalidParam;
    }

    public boolean findAd(){

        boolean bFindAd = false;
        JUtilFunctions.takeScreenshot();

        switch(type){
            case 0:{
                Mat workMat = JUtilFunctions.screenshot.submat(rcAnalyse);
                Mat detResult = JUtilFunctions.detectCircles(workMat, rcAnalyse, thresBorder, thresCircle, minRadius, maxRadius);

                int findCircleCnt = detResult.cols();
                if (findCircleCnt == 1){
                    double[] circle = detResult.get(0, 0);
                    ptFindAdCenter.x = Math.round(circle[0]);
                    ptFindAdCenter.y = Math.round(circle[1]);
                    circle = null;
                    bFindAd = true;
                }
            }
            break;
            case 1:{
                double[] pixelsVals = JUtilFunctions.screenshot.get((int)pixelforBase.y, (int)pixelforBase.x);
                if (pixelsVals[0] != colorNormalBack.x || pixelsVals[1] != colorNormalBack.y || pixelsVals[2] != colorNormalBack.z){
                    bFindAd = true;
                }
            }
            break;
        }

        return bFindAd;
    };

    public void closeAdWindow(){
        switch(type){
            case 0:{
                //. click proc...
                Point ptOrg = JUtilFunctions.getOrigPointFromBasePoint(ptFindAdCenter);
                JUserActions.dispatchTap(ptOrg.x, ptOrg.y);
            }
            break;
            case 1:{
                Point ptBase = new Point(Config.IMAGE_WIDTH / 2, Config.IMAGE_HEIGHT - 20);
                Point ptOrg = JUtilFunctions.getOrigPointFromBasePoint(ptBase);
                JUserActions.dispatchTap(ptOrg.x, ptOrg.y);
                ptBase = null;
                ptOrg = null;
            }
            break;
        }

    }

    @Override
    public boolean run_internel(JAction prevAction){

        JUtilFunctions.delay_duration(3000);

        boolean bFindAd = true;
        while(bFindAd) {
            bFindAd = findAd();
            if (bFindAd) {
                closeAdWindow();
                JUtilFunctions.delay_duration(3000);
            }
        }

        result_string = "success";
        executor.last_result_string = "success: " + name;
        return false;
    }
};



//.*=============================================================
//.func: JAction_TheEnd
//.desc:
//.
class JAction_TheEnd extends JAction{

    @Override
    public boolean run_internel(JAction prevAction){
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
            executor.last_result_string = result_string;
        }
        else{
            //. invalid param...
        }

        return true;
    }
};