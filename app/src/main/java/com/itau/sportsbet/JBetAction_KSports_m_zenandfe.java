package com.itau.sportsbet;

import static com.itau.sportsbet.Config.NeighborCond2Targets.e_FarHorizNeighborCond;
import static com.itau.sportsbet.Config.NeighborCond2Targets.e_UpDownDenseNeighborCond;
import static com.itau.sportsbet.Config.StrCompMethod.e_ExactEqual;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeNonAlphanumeric;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeSpace;
import static com.itau.sportsbet.Config.TextDetMode.e_NormalTxtDet;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class JBetAction_KSports_m_zenandfe extends JBetAction  {

    public Rect sportsMenuRect = null;
    boolean bFindedLeagueSection = false;

    JBetAction_KSports_m_zenandfe(JLoadTask loadTask){
        this.loadTask = loadTask;
    }
    @Override
    String run() {
        String strRet = null;

        Log.d("PPPPP", "Start gotoChannelfromDate.");
        strRet = gotoChannelfromDate();
        Log.d("PPPPP", "gotoChannelfromDate return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;


        Log.d("PPPPP", "Start findBettingCategorySection.");
        strRet = findBettingCategorySection();
        Log.d("PPPPP", "findBettingCategorySection return: " + strRet);
        if (strRet.equals("success") == false) {
            //. 2024-3-6
            //. once again. because hot matchss.
            if (bFindedLeagueSection){
                //. try to find another matched league section.
                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));
                strRet = findBettingCategorySection();
                if (strRet.equals("success") == false) {
                    return strRet;
                }
            }

        }

        Log.d("PPPPP", "Start findBettingTypeSection.");
        strRet = findBettingTypeSection();
        Log.d("PPPPP", "findBettingTypeSection return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;


        Log.d("PPPPP", "Start completeBetting.");
        strRet = completeBetting();
        Log.d("PPPPP", "completeBetting return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;

        return strRet;
    }

    //.*================================================================
    //.func: gotoChannelfromDate
    //.desc:
    private String gotoChannelfromDate(){

        String strRet = null;
        boolean bToday = false;

        String todayString = JUtilFunctions.getTodayString();
        if (todayString.equals(loadTask.progress_date))
            bToday = true;

        //.1. find Dirct...
        JUtilFunctions.takeScreenshot();

        Rect rcAnalyseBase = new Rect(400,180 ,140, 160);
        strRet = JUtilFunctions.findText("Yeu thich", rcAnalyseBase, fResizeRate, result_rects, e_removeSpace, e_NormalTxtDet);
        if (strRet.equals("success") == false)
            return strRet;

        Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(0));
        ptCenter.x = 54 / Config.resizeXRatio;
        int nXOffset = (int)(95 / Config.resizeYRatio);
        if (bToday){
            ptCenter.x += nXOffset;
        }
        else{
            ptCenter.x += 2 * nXOffset;
        }

        //. click button.
        JUserActions.dispatchTap(ptCenter.x, ptCenter.y);

        //.2024-3-6
        //. new validation structure...
        String jsonString = "[\"colorbar_det\", \"less\", \"1\", \"450\",\"400\",\"450\",\"900\", \"219\",\"224\",\"228\", \"219\",\"224\",\"228\", \"300\" ]";
        boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
        if (bTimeOver){
            strRet = "fail gotoChannelfromDate";
        }
        else{
            strRet = "success";
        }
        ptCenter = null;

        return strRet;
    }


    //.*================================================================
    //.func: findLeagureSection
    //.desc:
    private String findBettingCategorySection(){
        String strRet = null;

        //. for find color bar...
        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        colorBarParam.targetUpColor = new Point3(237,243,250);
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 30;
        colorBarParam.fixedVal = 470;
        colorBarParam.startVal = 50;
        colorBarParam.endVal = 850;

        JFuncParams_FindSectionIncluding2Targets param = new JFuncParams_FindSectionIncluding2Targets();
        param.sectionTarget = loadTask.league_name;
        param.target1 = loadTask.team1;
        param.strCompMethod1 = e_ExactEqual;
        param.strPreprocessMethod1 = e_removeSpace;
        param.target2 = loadTask.team2;
        param.strCompMethod2 = e_ExactEqual;
        param.strPreprocessMethod2 = e_removeSpace;
        param.tryScrollCnt = 30;
        param.nAnalyseWidth = 200;
        param.neighborCond2Targets = e_UpDownDenseNeighborCond; //. Up/ down layout...
        param.nextSectionInfo = colorBarParam;

        Point ptOutClickPos = new Point();
        boolean bFinded = JUtilFunctions.findSectionIncluding2Targets(param, ptOutClickPos);
        bFindedLeagueSection = param.bFindedLeagueSection;
        if (bFinded == false){
            strRet = "fail findSectionIncluding2Targets";
            return strRet;
        }

        //. do click.
        JUserActions.dispatchTap(ptOutClickPos.x, ptOutClickPos.y);

        //. new validation structure...
        String jsonString = "[\"colorbar_det\", \"less\", \"1\", \"450\",\"200\",\"450\",\"900\", \"219\",\"224\",\"228\", \"219\",\"224\",\"228\", \"300\" ]";
        boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
        if (bTimeOver){
            strRet = "fail enter betting room";
        }
        else{
            strRet = "success";
        }

        return strRet;
    }

    //.*================================================================
    //.func: findBettingTypeSection
    //.desc:
    private String findBettingTypeSection() {

        String strRet = null;

        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        //. for find color bar...
        colorBarParam.targetUpColor = new Point3(240,240,240);
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 30;
        colorBarParam.fixedVal = 470;
        colorBarParam.startVal = 50;
        colorBarParam.endVal = 850;

        JFuncParams_FindSectionIncluding2Targets param = new JFuncParams_FindSectionIncluding2Targets();
        param.sectionTarget = loadTask.betTypeCategory;
        param.target1 = loadTask.betTarget;
        param.strCompMethod1 = e_ExactEqual;
        param.strPreprocessMethod1 = e_removeSpace;
        param.target2 = loadTask.betMark;
        param.strCompMethod2 = e_ExactEqual;
        param.strPreprocessMethod2 = e_removeNonAlphanumeric;
        param.tryScrollCnt = 3;
        param.nAnalyseWidth = Config.IMAGE_WIDTH;
        param.neighborCond2Targets = e_FarHorizNeighborCond; //. Up/ down layout...
        param.nextSectionInfo = colorBarParam;


        Point ptOutClickPos = new Point();
        boolean bFinded = JUtilFunctions.findSectionIncluding2Targets(param, ptOutClickPos);
        if (bFinded == false){
            strRet = "fail findBettingTypeSection";
            return strRet;
        }

        //. do click.
        JUserActions.dispatchTap(ptOutClickPos.x, ptOutClickPos.y);
        JUtilFunctions.delay_duration(500);

        strRet = "success";

        return strRet;
    }

    //.*================================================================
    //.func: completeBetting
    //.desc:
    private String completeBetting() {
        String strRet = "fail completeBetting";

        //. decide some values by Image processing
        int nStartY_TitleBar = 0 ,nEndY_TitleBar = 0;
        int nStartY_EditBar = 0 ,nEndY_EditBar = 0;
        Point [] ptNumbers = new Point[10];
        for(int i = 0 ; i<10; i++){
            ptNumbers[i] = new Point();
        }
        Point ptBetNowBnt_Center = null;


        JUtilFunctions.takeScreenshot();

        //. 1. find green title bar...
        JFuncParams_ColorBar param = new JFuncParams_ColorBar();
        param.targetUpColor = new Point3(54,192,101);
        param.targetDownColor = param.targetUpColor;
        param.nLimitLen = 50;
        param.bVert = true;
        param.fixedVal = 400; param.startVal = 100; param.endVal = 400;

        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);
        if (retSegments.size() == 1){
            nStartY_TitleBar = (int)retSegments.get(0).x; nEndY_TitleBar = (int)retSegments.get(0).y;

            //. 2. find bet now button...
            //. 2024-3-9
            //. why? msg "Superuser granted to..." is overfit button, so...
            //. attention, ocr in bottom area, avoid...

            /*
            Rect rcAnalyseBase = new Rect(260, 800, 270, 160);
            strRet = JUtilFunctions.findText(Config.bet_btn, rcAnalyseBase, fResizeRate,
                    result_rects, e_removeSpace, e_NormalTxtDet);
            if (strRet.equals("success")){
            */

            //. so I do it that find color bar...
            param.targetUpColor = new Point3(69,137,200);
            param.targetDownColor = param.targetUpColor;
            param.nLimitLen = 45;
            param.bVert = true;
            param.fixedVal = 490; param.startVal = 730; param.endVal = 950;

            ArrayList<Point> retSegments5 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);
            if (retSegments5.size() == 1){

                // ptBetNowBnt_Center = JUtilFunctions.getCenterPoint(result_rects.get(0));
                Point ptTmp = retSegments5.get(0);
                ptBetNowBnt_Center = new Point(param.fixedVal, (ptTmp.x + ptTmp.y) / 2);

                param.targetUpColor = new Point3(255,255,255);
                param.targetDownColor = new Point3(255,255,255);
                param.nLimitLen = 45;
                param.fixedVal = 300; param.startVal = nEndY_TitleBar + 10; param.endVal = 450;

                ArrayList<Point> retSegments1 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);
                if (retSegments1.size() >= 1){
                    nStartY_EditBar = (int)retSegments1.get(0).x; nEndY_EditBar = (int)retSegments1.get(0).y;

                    //. find 2 color bars.
                    param.targetUpColor = new Point3(222,239,255);
                    param.targetDownColor = param.targetUpColor;
                    param.nLimitLen = 35;
                    param.fixedVal = 290; param.startVal = nEndY_EditBar + 10; param.endVal = nEndY_EditBar + 160;

                    ArrayList<Point> retSegments2 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);

                    param.targetUpColor = new Point3(255,234,212);
                    param.targetDownColor = param.targetUpColor;
                    param.nLimitLen = 35;
                    param.fixedVal = 270; param.startVal = nEndY_EditBar + 100; param.endVal = nEndY_EditBar + 350;

                    ArrayList<Point> retSegments3 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, param);

                    if (retSegments2.size() == 1 && retSegments3.size() == 1){

                        int nY1 = (int)retSegments2.get(0).y;
                        int nY2 = (int)(retSegments3.get(0).x - 10 / Config.resizeYRatio);
                        int nMidY = (nY1 + nY2) / 2;
                        int nMidY1 = (nMidY + nY1) / 2;
                        int nMidY2 = (nMidY + nY2) / 2;
                        int nCellWidth = (int)(Config.Screen_Width / 6);

                        int nStartX = nCellWidth / 2;
                        for (int i = 1; i <= 6; i++){
                            ptNumbers[i].x = nStartX + nCellWidth * (i-1);
                            ptNumbers[i].y = nMidY1;
                        }
                        for (int i = 7; i <= 10; i++){
                            int nIdx = i;
                            if (i == 10)
                                nIdx = 0;

                            ptNumbers[nIdx].x = nStartX + nCellWidth * (i-7);
                            ptNumbers[nIdx].y = nMidY2;
                        }

                        //. run all process.
                        //. first clear in edit box.
                        Point ptClear = new Point((Config.IMAGE_WIDTH - 40) / Config.resizeXRatio,
                                (nStartY_EditBar + nEndY_EditBar) / 2 / Config.resizeYRatio);
                        JUserActions.dispatchTap(ptClear.x, ptClear.y);
                        JUtilFunctions.delay_duration(100);

                        //. next. input bet amount...
                        for (int i = 0; i < loadTask.betAmount.length(); i++) {
                            char ch = loadTask.betAmount.charAt(i);

                            //. convert ch to int.
                            int k = ch - '0';
                            JUserActions.dispatchTap(ptNumbers[k].x, ptNumbers[k].y);
                            JUtilFunctions.delay_duration(100);
                        }

                        //. last. bet now button click.
//                        JUserActions.dispatchTap(ptBetNowBnt_Center.x, ptBetNowBnt_Center.y);
                        JUtilFunctions.delay_duration(100);

                        strRet = "success";

                    }

                    retSegments2 = null;
                    retSegments3 = null;

                }
                retSegments1 = null;
            }
        }
        retSegments = null;
        return strRet;
    }


}


