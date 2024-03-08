package com.itau.sportsbet;

import static com.itau.sportsbet.Config.StrCompMethod.e_ExactEqual;
import static com.itau.sportsbet.Config.StrCompMethod.e_PermitIncluding;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeNonAlphanumeric;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeNonLetters;
import static com.itau.sportsbet.Config.StrPreprocessMethod.e_removeSpace;
import static com.itau.sportsbet.Config.TextDetMode.e_HasMarkFront;
import static com.itau.sportsbet.Config.TextDetMode.e_NormalTxtDet;

import android.app.ActivityManager;
import android.util.Log;
import android.view.KeyEvent;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.util.ArrayList;

public class JBetAction_ESports_prod20091_bti extends JBetAction {

    public Rect sportsMenuRect = null;

    JBetAction_ESports_prod20091_bti(JLoadTask loadTask){
        this.loadTask = loadTask;
    }

    // Implementation of the abstract method
    @Override
    String run(){
        String strRet = null;

        //. 0. if prev tried betting, remove it
        Log.d("PPPPP", "Start removePrevBetState.");
        strRet = removePrevBetState();
        Log.d("PPPPP", "removePrevBetState return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;

        //. 1. go channel for date
        Log.d("PPPPP", "Start gotoChannelfromDate.");
        strRet = gotoChannelfromDate();
        Log.d("PPPPP", "gotoChannelfromDate return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;

        //. 2 find bettingSection and view bet window...
        Log.d("PPPPP", "Start findBettingCategorySection.");
        strRet = findBettingCategorySection();
        Log.d("PPPPP", "findBettingCategorySection return: " + strRet);
        if (strRet.equals("success") == false) {
            return strRet;
        }

        //. 3
        Log.d("PPPPP", "Start findBettingTypeSection.");
        strRet = findBettingTypeSection();
        Log.d("PPPPP", "findBettingTypeSection return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;


        //. 4
        Log.d("PPPPP", "Start completeBet.");
        strRet = completeBet();
        if (strRet.equals("success") == false)
            return strRet;

        strRet = "success";

        return strRet;
    }

    //.*================================================================
    //.func: removePrevBetState
    //.desc: if prev tried Betting exist, remove it...
    private String removePrevBetState() {
        String strRet = null;

        JUtilFunctions.takeScreenshot();

        Rect rcAnalyseBase = new Rect(200,870, 150, 85);
        strRet = JUtilFunctions.findText("Phieu cuoc", rcAnalyseBase, fResizeRate, result_rects, e_removeSpace, e_NormalTxtDet);
        if (strRet.equals("success")){
            Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(0));
            Rect rcWork = new Rect((int)ptCenter.x, (int)ptCenter.y - 70, 50, 70);
            Point3 color = new Point3(242, 0, 0);
            boolean bHasRedColor = JUtilFunctions.hasSpecialColorPointInRegion(JUtilFunctions.screenshot, rcWork, color);
            if (bHasRedColor){
                JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
                JUtilFunctions.delay_duration(500);

                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));

                Point ptRemove = new Point(505 / Config.resizeXRatio, 815 / Config.resizeYRatio);
                JUserActions.dispatchTap(ptRemove.x, ptRemove.y);
                JUtilFunctions.delay_duration(300);
            }

            strRet = "success";
        }

        return strRet;
    }



    //.*================================================================
    //.func: gotoChannelfromDate
    //.desc: my assume: date is today or tomorrow...
    private String gotoChannelfromDate(){

        String strRet = null;
        boolean bToday = false;

        String todayString = JUtilFunctions.getTodayString();
        if (todayString.equals(loadTask.progress_date))
            bToday = true;

        JUtilFunctions.takeScreenshot();

        //.1. find "Som (Earyly)" button...
        Rect rcAnalyseBase = new Rect(0,400 ,180, 90);
        strRet = JUtilFunctions.findText("Truc tiep", rcAnalyseBase, fResizeRate, result_rects, e_removeSpace, e_NormalTxtDet);
        if (strRet.equals("success") == false)
            return strRet;

        //.2. click "Som (Early)" button...
        Point ptCenterEarly = JUtilFunctions.getCenterPoint(result_rects.get(0));
        ptCenterEarly.x += (int)(270 / Config.resizeXRatio);
        JUserActions.dispatchTap(ptCenterEarly.x, ptCenterEarly.y);
        //. until wait for validation...
        String jsonString = "[\"ocr\", \"have\", \"Giải đấu\", \"40\",\"630\",\"170\",\"710\"]";
        boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
        if (bTimeOver){
            strRet = "fail gotoChannelfromDate";
        }
        else{
            //. 2. find "Hom nay(Today)" button
            rcAnalyseBase.x = 0; rcAnalyseBase.y = 550; rcAnalyseBase.width = 130; rcAnalyseBase.height = 100;
            strRet = JUtilFunctions.findText("Hom nay", rcAnalyseBase, fResizeRate, result_rects, e_removeSpace, e_NormalTxtDet);
            if (strRet.equals("success")){
                Point ptCenterToday = JUtilFunctions.getCenterPoint(result_rects.get(0));
                if (bToday == false){
                    int nXOffset = (int)(125 / Config.resizeYRatio);
                    ptCenterToday.x += nXOffset;
                }

                //. 3. click. date button.
                JUserActions.dispatchTap(ptCenterToday.x, ptCenterToday.y);
                //. until wait for validation...
                bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
                if (bTimeOver){
                    strRet = "fail gotoChannelfromDate";

                }
                else{
                    strRet = "success";

                }
                ptCenterToday = null;
            }
        }
        ptCenterEarly = null;

        return strRet;
    }


    //.*================================================================
    //.func: findLeagureSection
    //.desc:
    private String findBettingCategorySection(){
        String strRet = null;

        //. for find color bar...
        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        colorBarParam.targetUpColor = new Point3(70,75,88);
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 30;
        colorBarParam.fixedVal = 470;
        colorBarParam.startVal = 50;
        colorBarParam.endVal = 850;

        JFuncParams_FindSectionIncluding2Targets param = new JFuncParams_FindSectionIncluding2Targets();
        param.sectionTarget = loadTask.league_name;
        param.eSecTargetComMethod = e_PermitIncluding;
        param.target1 = loadTask.team1;
        param.strCompMethod1 = e_ExactEqual;
        param.strPreprocessMethod1 = e_removeSpace;
        param.target2 = loadTask.team2;
        param.strCompMethod2 = e_ExactEqual;
        param.strPreprocessMethod2 = e_removeSpace;
        param.tryScrollCnt = 30;
        param.nAnalyseWidth = Config.IMAGE_WIDTH;
        param.neighborCond2Targets = 0; //. Up/ down layout...
        param.nextSectionInfo = colorBarParam;
        param.ptBetPannelBackColor = new Point3(255,255,255);

        //. 1. find league Section and expand it.
        boolean bFindSeciton = JUtilFunctions.findSectionandExpanding(param);
        if (bFindSeciton == false){
            strRet = "fail findSection";
            return strRet;
        }

        //. 2. find final targets...
        Point ptOutClickPos = new Point();
        boolean bFinded = JUtilFunctions.findSectionIncluding2Targets(param, ptOutClickPos);
        if (bFinded == false){
            strRet = "fail findSectionIncluding2Targets";
            return strRet;
        }

        //. do click.
        JUserActions.dispatchTap(ptOutClickPos.x, ptOutClickPos.y);

        //. new validation structure...
        String jsonString = "[\"ocr\", \"have\", \"Chinh\", \"200\",\"650\",\"360\",\"800\"]";
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
        colorBarParam.targetUpColor = new Point3(70,75,88);
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 30;
        colorBarParam.fixedVal = 470;
        colorBarParam.startVal = 300;
        colorBarParam.endVal = 850;

        JFuncParams_FindSectionIncluding2Targets param = new JFuncParams_FindSectionIncluding2Targets();

        String searchKey = null;
        if (loadTask.betTypeCategory.equals(Config.betType_TaiXiu))
            searchKey = "Cuoc Tai/Xiu toan tran";
        else if (loadTask.betTypeCategory.equals(Config.betType_Handicap))
            searchKey = "Cuoc chap Chau A toan tran";
        else{
            strRet = "Invalid betTypeCategory";
            return strRet;
        }
        param.sectionTarget = searchKey;
        param.eSecTargetComMethod = e_PermitIncluding;

        //. in this case. two target must added.
        String strTarget = loadTask.betTarget + " " + loadTask.betMark;
        param.target1 = loadTask.betTarget;
        param.strCompMethod1 = e_ExactEqual;
        param.strPreprocessMethod1 = e_removeSpace;
        param.target2 = null;   //. must null
        param.tryScrollCnt = 5;
        param.nAnalyseWidth = Config.IMAGE_WIDTH;
        param.neighborCond2Targets = 1; //. horz layout...
        param.nextSectionInfo = colorBarParam;

        Point ptOutClickPos = new Point();
        boolean bFinded = JUtilFunctions.findSectionIncluding2Targets(param, ptOutClickPos);
        if (bFinded == false){
            strRet = "fail findBettingTypeSection";
            return strRet;
        }

        //. do click.
        JUserActions.dispatchTap(ptOutClickPos.x, ptOutClickPos.y);
        JUtilFunctions.delay_duration(1000);

        strRet = "success";

        return strRet;
    }



    private String completeBet(){

        String strRet = null;

        JUtilFunctions.takeScreenshot();

        //. first find edit ctrl.
        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        //. for find color bar...
        colorBarParam.targetUpColor = new Point3(255,255,255);
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 30;
        colorBarParam.fixedVal = 320;
        colorBarParam.startVal = 250;
        colorBarParam.endVal = 500;
        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, colorBarParam);
        int nSecCnt = retSegments.size();
        if (nSecCnt != 1){
            strRet = "fail find InputBox: completeBet";
            return strRet;
        }

        //. 2, click it.
        Point ptCenter = new Point(colorBarParam.fixedVal, (retSegments.get(0).x + retSegments.get(0).y) / 2);
        JUserActions.dispatchTap(ptCenter.x,  ptCenter.y );
        JUtilFunctions.delay_duration(100);
        JUserActions.deleteContentofInput(8);
        JUserActions.dispatchKeyPress(loadTask.betAmount);
        JUtilFunctions.delay_duration(1000);

//    Finding bet button
        JUserActions.scrollToUp(new Point(Config.Screen_Width/2, Config.Screen_Height-Config.CalcYSize(150) ), Config.CalcYSize(300));
        JUtilFunctions.delay_duration(200);
/// click bet button

        JUtilFunctions.takeScreenshot();
        Rect rcAnalyseBase = new Rect(100, 750, 250, 110 );
        strRet = JUtilFunctions.findText("Đat cuoc", rcAnalyseBase, fResizeRate, result_rects, e_removeSpace, e_NormalTxtDet);
        if (strRet.equals("success")) {
            Point ptCenterBetBtn = JUtilFunctions.getCenterPoint(result_rects.get(0));
            //pgh JUserActions.dispatchTap(ptCenterBetBtn.x,  ptCenterBetBtn.y );
            JUtilFunctions.delay_duration(1000);
        }

        retSegments = null;

        return strRet;
    }

}
