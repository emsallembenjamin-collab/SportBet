package com.itau.sportsbet;

import static com.itau.sportsbet.Config.StrCompMethod.e_ExactEqual;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeSpace;
import static com.itau.sportsbet.Config.TextDetMode.e_NormalTxtDet;

import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.util.ArrayList;

//.*=================================================================
//.class: JActionValidator
//.desc: root class for validation action...
public class JActionValidator {

    public JAction action = null;
    public int      startIdxforJsonStringArray = 0;
    JActionValidator(JAction action){
        this.action = action;
    }

    //. must overriding for child classes.
    public boolean check_forCond() {return true;};
    public boolean build(JSONArray jsonArray) throws JSONException {return true;};

    public static JActionValidator createObject(JAction action, JSONArray jsonArray) throws JSONException {

        JActionValidator retValidator = null;
        int startIdxforJsonStringArray = 0;

        int elementCnt = jsonArray.length();
        if (elementCnt > 0){
            String strTag = jsonArray.getString(0);
            boolean bOnceTrueFlag = true;
            L_LOOP: while(bOnceTrueFlag){
                bOnceTrueFlag = false;

                switch(strTag) {
                    case "color_px": {
                        retValidator = new JActionValidator_ColorPx(action);
                    }
                    break;
                    case "line_det": {
                        retValidator = new JActionValidator_LineDet(action);
                    }
                    break;
                    case "colorbar_det": {
                        retValidator = new JActionValidator_ColorbarDet(action);
                    }
                    break;
                    case "ocr": {
                        retValidator = new JActionValidator_Ocr(action);
                    }
                    break;
                    case "$category": {
                        //. 2024-3-9
                        int nIters = MyAccessibilityService.mainService.loadTask.category;  //. first 0.
                        int nSkipCnt = 0;
                        for (int i = 0; i < nIters; i++) {
                            String strSubTag = jsonArray.getString(1);
                            switch (strSubTag) {
                                case "color_px": {
                                    nSkipCnt += 7;
                                }
                                break;
                                case "line_det": {
                                    nSkipCnt += 3;
                                }
                                break;
                                case "colorbar_det": {
                                    nSkipCnt += 14;
                                }
                                break;
                                case "ocr": {
                                    nSkipCnt += 7;
                                }
                                break;
                            }
                        }

                        startIdxforJsonStringArray = nSkipCnt + 1;
                        strTag = jsonArray.getString(nSkipCnt + 1);
                        bOnceTrueFlag = true;
                        continue L_LOOP;
                    }
                    default:
                        break;
                }


            }
        }
        else{
            //. create default action...
            retValidator = new JActionValidator(action);
        }

        if (retValidator != null)
            retValidator.startIdxforJsonStringArray = startIdxforJsonStringArray;

        return retValidator;
    }

    //.*===================================================================
    //.func: check
    //.desc: main entry point for validate action...
    public boolean check(){
        boolean bFinish = false;

        final long startTime = SystemClock.elapsedRealtime();

        boolean bInitFlag = true;
        while(true){
            if (action.waitPageload == true && MyAccessibilityService.mainService.bPageLoadFlag == false){
                JUtilFunctions.delay_duration(100);
            }
            else{
                JUtilFunctions.delay_duration(200);
                boolean bCheckResult = check_forCond();
                if (bInitFlag){
                    JUtilFunctions.delay_duration(action.delay);
                    bInitFlag = false;
                }
                if (bCheckResult){
                    if (action.type.equals("puseudo") == false){
                        action.executor.last_result_string = "success: " + action.name;
                    }
                    break;
                }
            }

            // Calculate elapsed time
            final long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTime;
            if (elapsedTimeMillis > action.time_limit){
                if (action.type.equals("puseudo") == false){
                    action.executor.last_result_string = "time is over: " + action.name;
                }
                bFinish = true;
                break;
            }
        }

        return bFinish;
    }
};


//.*=================================================================
//.class: JActionValidator_ColorPx
//.desc: confirm pixel's color value...
//.      ex: [\"color_px\", \"neg\", \"100\", \"100\",   \"255\",\"255\",\"255\"]"
//.         param Cnt = 7;
class JActionValidator_ColorPx extends JActionValidator{
    public boolean bNegCond = false;
    public Point ptPos = null;
    public Point3 validateColor = null;

    JActionValidator_ColorPx(JAction action){
        super(action);
    }

    public boolean build(JSONArray jsonArray) throws JSONException {
        boolean bRet = false;

        int elementCnt = jsonArray.length();
        int nRemainCnt = elementCnt - startIdxforJsonStringArray;
        if (nRemainCnt >= 7){
            String strNeg = jsonArray.getString(startIdxforJsonStringArray + 1);
            if (strNeg.equals("neg")){
                bNegCond = true;
            }
            ptPos = new Point(jsonArray.getDouble(startIdxforJsonStringArray + 2), jsonArray.getDouble(startIdxforJsonStringArray + 3));
            validateColor = new Point3(jsonArray.getDouble(startIdxforJsonStringArray + 4), jsonArray.getDouble(startIdxforJsonStringArray + 5), jsonArray.getDouble(startIdxforJsonStringArray + 6));
            bRet = true;
        }

        return bRet;
    }

    public boolean check_forCond() {
        boolean bCheck = false;

        JUtilFunctions.takeScreenshot();
        double[] pixelsVals = JUtilFunctions.screenshot.get((int)ptPos.y, (int)ptPos.x);
        if (bNegCond){
            bCheck = (pixelsVals[0] != validateColor.x) || (pixelsVals[1] != validateColor.y) || (pixelsVals[2] != validateColor.z);
        }
        else{
            bCheck = (pixelsVals[0] == validateColor.x) && (pixelsVals[1] == validateColor.y) && (pixelsVals[2] == validateColor.z);
        }
        return bCheck;
    }
};


//.*=================================================================
//.class: JActionValidator_LineDet
//.desc: detect lines, and decide using it's count...
//.      ex: [\"line_det\", \"great\", \"10\"]"
//.         param Cnt = 3;
class JActionValidator_LineDet extends JActionValidator {

    //. for line det.
    public boolean bGreatCond = false;
    public int nLineDetThres = 0;

    JActionValidator_LineDet(JAction action){
        super(action);
    }

    public boolean build(JSONArray jsonArray) throws JSONException {
        boolean bRet = false;

        int elementCnt = jsonArray.length();
        int nRemainCnt = elementCnt - startIdxforJsonStringArray;
        if (nRemainCnt >= 3){
            String strNeg = jsonArray.getString(startIdxforJsonStringArray + 1);
            if (strNeg.equals("great")){
                bGreatCond = true;
            }
            nLineDetThres = jsonArray.getInt(startIdxforJsonStringArray + 2);
            bRet = true;
        }

        return bRet;
    }

    public boolean check_forCond() {
        boolean bCheck = false;

        JUtilFunctions.takeScreenshot();
        Mat lines = JUtilFunctions.detectLines(JUtilFunctions.screenshot, 30, 30, 30, 1);
        int nLineCnt = lines.rows();
        if (bGreatCond){
            bCheck = (nLineCnt >= nLineDetThres);
        }
        else{
            bCheck = (nLineCnt < nLineDetThres);
        }

        return bCheck;
    }
};


//.*=================================================================
//.class: JActionValidator_ColorbarDet
//.desc: detect colorbars, and decide using it's count...
//. json type is ex: "[\"colorbar_det\", \"great\", \"1\", \"470\",\"400\",\"470\",\"900\",    \"70\",\"75\",\"88\", \"70\",\"75\",\"88\", \"20\" ]"
//.     param Cnt = 14;
//.
class JActionValidator_ColorbarDet extends JActionValidator {

    //. for line det.
    public boolean bGreatCond = false;
    public int nBarDetThres = 0;

    JFuncParams_ColorBar param = null;

    JActionValidator_ColorbarDet(JAction action){
        super(action);
    }

    //.*==============================================================
    //.func: build
    //.desc: ;
    //.
    //
    public boolean build(JSONArray jsonArray) throws JSONException {
        boolean bRet = false;

        int elementCnt = jsonArray.length();
        int nRemainCnt = elementCnt - startIdxforJsonStringArray;
        if (nRemainCnt >= 14){
            String strNeg = jsonArray.getString(startIdxforJsonStringArray + 1);
            if (strNeg.equals("great")){
                bGreatCond = true;
            }
            nBarDetThres = jsonArray.getInt(startIdxforJsonStringArray + 2);
            Rect rcForAnalyse = new Rect(jsonArray.getInt(startIdxforJsonStringArray + 3), jsonArray.getInt(startIdxforJsonStringArray + 4),
                    jsonArray.getInt(startIdxforJsonStringArray + 5),jsonArray.getInt(startIdxforJsonStringArray + 6));
            rcForAnalyse.width = rcForAnalyse.width - rcForAnalyse.x;
            rcForAnalyse.height = rcForAnalyse.height - rcForAnalyse.y;

            param = new JFuncParams_ColorBar();

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
                return false;
            }

            // Define the color to find (RGB: 29, 27, 24)
            // OpenCV uses BGR color ordering but now set param in order "RGB".
            param.targetUpColor = new Point3(jsonArray.getInt(startIdxforJsonStringArray + 7), jsonArray.getInt(startIdxforJsonStringArray + 8), jsonArray.getInt(startIdxforJsonStringArray + 9));
            param.targetDownColor = new Point3(jsonArray.getInt(startIdxforJsonStringArray + 10), jsonArray.getInt(startIdxforJsonStringArray + 11), jsonArray.getInt(startIdxforJsonStringArray + 12));
            param.nLimitLen = jsonArray.getInt(startIdxforJsonStringArray + 13);

            bRet = true;
        }

        return bRet;
    }

    public boolean check_forCond() {
        boolean bCheck = false;

        JUtilFunctions.takeScreenshot();

        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);

        //. save to result_rects.
        int nSegCnt = retSegments.size();
        if (bGreatCond){
            bCheck = (nSegCnt >= nBarDetThres);
        }
        else{
            bCheck = (nSegCnt < nBarDetThres);
        }

        return bCheck;
    }
};

//.*=================================================================
//.class: JActionValidator_Ocr
//.desc: detect target string in specified region, and decide if it exists...
//.      json type: "[\"ocr\", \"have\", \"Đ.NHẬP\", \"0\",\"430\",\"160\",\"530\"]"
//.     param Cnt = 7;
class JActionValidator_Ocr extends JActionValidator {

    //. for line det.
    public boolean bHaveCond = false;
    public String  targetStr = null;
    public Rect rcForAnalyse = new Rect(0,0,0,0);

    JActionValidator_Ocr(JAction action){
        super(action);
    }

    //.*==============================================================
    //.func: build
    //.desc: json type is ex:  "validator_param_list"  : ["ocr", "have", "Đ.NHẬP", "0","430","160","530"],
    //.
    public boolean build(JSONArray jsonArray) throws JSONException {
        boolean bRet = false;

        int elementCnt = jsonArray.length();
        int nRemainCnt = elementCnt - startIdxforJsonStringArray;
        if (nRemainCnt >= 7){
            String strNeg = jsonArray.getString(startIdxforJsonStringArray + 1);
            if (strNeg.equals("have")){
                bHaveCond = true;
            }
            targetStr = jsonArray.getString(startIdxforJsonStringArray + 2);

            rcForAnalyse.x = jsonArray.getInt(startIdxforJsonStringArray + 3);
            rcForAnalyse.y = jsonArray.getInt(startIdxforJsonStringArray + 4);
            rcForAnalyse.width = jsonArray.getInt(startIdxforJsonStringArray + 5);
            rcForAnalyse.height = jsonArray.getInt(startIdxforJsonStringArray + 6);
            rcForAnalyse.width = rcForAnalyse.width - rcForAnalyse.x;
            rcForAnalyse.height = rcForAnalyse.height - rcForAnalyse.y;

            bRet = true;
        }

        return bRet;
    }

    public boolean check_forCond() {
        boolean bCheck = false;

        JUtilFunctions.takeScreenshot();

        String strRet = JUtilFunctions.findText(targetStr, rcForAnalyse, 1.0f, action.result_rects, e_removeSpace, e_NormalTxtDet);
        if (strRet.equals("success")){
            if (bHaveCond){
                bCheck = true;
            }
        }
        else {
            if (!bHaveCond){
                bCheck = true;
            }
        }

        return bCheck;
    }
};


