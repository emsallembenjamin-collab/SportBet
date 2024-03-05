package com.itau.sportsbet;

import android.os.SystemClock;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;

import java.util.ArrayList;

public class JActionValidator {

    public JAction action = null;
    public boolean bSetColorValidator = false;
    public boolean bNegCond = false;
    public Point ptPos = null;
    public Point3 validateColor = null;


    //. for line det.
    public boolean bSetLineDetValidator = false;
    public boolean bGreatCond = false;
    public int nLineDetThres = 0;



    JActionValidator(JAction action){
        this.action = action;
    }

    public boolean build(JSONArray jsonArray) throws JSONException {
        boolean bRet = false;

        int elementCnt = jsonArray.length();
        if (elementCnt > 0){
            String strTag = jsonArray.getString(0);
            switch(strTag){
                case "color_px":{
                    if (elementCnt == 7){
                        bSetColorValidator = true;
                        String strNeg = jsonArray.getString(1);
                        if (strNeg.equals("neg")){
                            bNegCond = true;
                        }
                        ptPos = new Point(jsonArray.getDouble(2), jsonArray.getDouble(3));
                        validateColor = new Point3(jsonArray.getDouble(4), jsonArray.getDouble(5), jsonArray.getDouble(6));
                        bRet = true;
                    }
                }
                break;
                case "line_det":{
                    if (elementCnt == 3){
                        bSetLineDetValidator = true;
                        String strNeg = jsonArray.getString(1);
                        if (strNeg.equals("great")){
                            bGreatCond = true;
                        }
                        nLineDetThres = jsonArray.getInt(2);
                        bRet = true;
                    }
                }
                break;
                default:
                    break;

            }
        }

        return bRet;
    }

    public boolean check_forCond() {
        boolean bCheck = false;
        if (bSetColorValidator){
            JUtilFunctions.takeScreenshot();
            double[] pixelsVals = JUtilFunctions.screenshot.get((int)ptPos.y, (int)ptPos.x);
            if (bNegCond){
                bCheck = (pixelsVals[0] != validateColor.x) || (pixelsVals[1] != validateColor.y) || (pixelsVals[2] != validateColor.z);
            }
            else{
                bCheck = (pixelsVals[0] == validateColor.x) && (pixelsVals[1] == validateColor.y) && (pixelsVals[2] == validateColor.z);
            }
        }
        else if (bSetLineDetValidator){
            JUtilFunctions.takeScreenshot();
            Mat lines = JUtilFunctions.detectLines(JUtilFunctions.screenshot, 30, 30, 30, 1);
            int nLineCnt = lines.rows();
            if (bGreatCond){
                bCheck = (nLineCnt >= nLineDetThres);
            }
            else{
                bCheck = (nLineCnt < nLineDetThres);
            }
        }
        else{
            bCheck = true;
        }
        return bCheck;

    }

    public boolean check(){
        boolean bFinish = false;

        final long startTime = SystemClock.elapsedRealtime();

        boolean bInitFlag = true;
        while(true){
            if (MyAccessibilityService.mainService.bPageLoadFlag == false){
                JUtilFunctions.delay_duration(100);
            }
            else{
                if (bInitFlag){
                    JUtilFunctions.delay_duration(action.delay);
                    bInitFlag = false;
                }

                JUtilFunctions.delay_duration(100);
                if (check_forCond()){
                    action.executor.last_result_string = "success: " + action.name;
                    break;
                }
            }

            // Calculate elapsed time
            final long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTime;
            if (elapsedTimeMillis > action.time_limit){
                action.executor.last_result_string = "time is over: " + action.name;
                bFinish = true;
                break;
            }
        }

        return bFinish;
    }

}
