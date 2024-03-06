package com.itau.sportsbet;

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
    JActionValidator(JAction action){
        this.action = action;
    }

    //. must overriding for child classes.
    public boolean check_forCond() {return true;};
    public boolean build(JSONArray jsonArray) throws JSONException {return true;};

    public static JActionValidator createObject(JAction action, JSONArray jsonArray) throws JSONException {

        JActionValidator retValidator = null;

        int elementCnt = jsonArray.length();
        if (elementCnt > 0){
            String strTag = jsonArray.getString(0);
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
                default:
                    break;
            }
        }
        else{
            //. create default action...
            retValidator = new JActionValidator(action);
        }

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
                if (bInitFlag){
                    JUtilFunctions.delay_duration(action.delay);
                    bInitFlag = false;
                }

                JUtilFunctions.delay_duration(100);
                if (check_forCond()){
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
        if (elementCnt == 7){
            String strNeg = jsonArray.getString(1);
            if (strNeg.equals("neg")){
                bNegCond = true;
            }
            ptPos = new Point(jsonArray.getDouble(2), jsonArray.getDouble(3));
            validateColor = new Point3(jsonArray.getDouble(4), jsonArray.getDouble(5), jsonArray.getDouble(6));
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
        if (elementCnt == 3){
            String strNeg = jsonArray.getString(1);
            if (strNeg.equals("great")){
                bGreatCond = true;
            }
            nLineDetThres = jsonArray.getInt(2);
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
class JActionValidator_ColorbarDet extends JActionValidator {

    //. for line det.
    public boolean bGreatCond = false;
    public int nBarDetThres = 0;

    public Rect rcForAnalyse = new Rect(0,0,0,0);

    JFuncParams_ColorBar param = null;

    JActionValidator_ColorbarDet(JAction action){
        super(action);
    }

    //.*==============================================================
    //.func: build
    //.desc: json type is ex: {\"validator_param_list\"  : ["colorbar_det", "great", "2", "100","0","100","300", "50","50","50", "20","20","20", "limitLen" ]}
    //. "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}"
    public boolean build(JSONArray jsonArray) throws JSONException {
        boolean bRet = false;

        int elementCnt = jsonArray.length();
        if (elementCnt == 14){
            String strNeg = jsonArray.getString(1);
            if (strNeg.equals("great")){
                bGreatCond = true;
            }
            nBarDetThres = jsonArray.getInt(2);
            Rect rcForAnalyse = new Rect(jsonArray.getInt(3), jsonArray.getInt(4), jsonArray.getInt(5),jsonArray.getInt(6));
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
            param.targetUpColor = new Point3(jsonArray.getInt(7), jsonArray.getInt(8), jsonArray.getInt(9));
            param.targetDownColor = new Point3(jsonArray.getInt(10), jsonArray.getInt(11), jsonArray.getInt(12));
            param.nLimitLen = jsonArray.getInt(13);

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
