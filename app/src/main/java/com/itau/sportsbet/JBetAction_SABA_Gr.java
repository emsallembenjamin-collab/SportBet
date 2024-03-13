package com.itau.sportsbet;

import static com.itau.sportsbet.Config.IgnorePartMode.e_IgnoreMode1;
import static com.itau.sportsbet.Config.NeighborCond2Targets.e_FarHorizNeighborCond;
import static com.itau.sportsbet.Config.NeighborCond2Targets.e_FarVerticalNeighborCond;
import static com.itau.sportsbet.Config.NeighborCond2Targets.e_TableTypeNeighborCond;
import static com.itau.sportsbet.Config.NeighborCond2Targets.e_UpDownDenseNeighborCond;
import static com.itau.sportsbet.Config.OcrPattern.e_DigitOnly;
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

public class JBetAction_SABA_Gr extends JBetAction {

    public Rect     sportsMenuRect = null;
    public Point3   ptBackColor = new Point3(27,27,36);
    public Point3   ptBetPannelColor = new Point3(39,39,50);

    JBetAction_SABA_Gr(JLoadTask loadTask){
        this.loadTask = loadTask;
    }

    // Implementation of the abstract method
    @Override
    String run(){
        String strRet = null;

        //. -1.
        Log.d("PPPPP", "Start findBackColor.");
        strRet = findBackColor();
        Log.d("PPPPP", "findBackColor return: " + strRet);
        if (strRet.equals("success") == false)
            return strRet;

        //. 0. if prev tried betting, remove it
        Log.d("PPPPP", "Start clickSportButton.");
        strRet = clickSportButton();
        Log.d("PPPPP", "clickSportButton return: " + strRet);
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

    //.*==========================================================================
    //.0. first : findBackColor.
    //.
    //.
    String findBackColor() {

        String strRet = "fail findBackColor";

        JUtilFunctions.takeScreenshot();

        //. first. we need wait until complete opening webpage...
        String jsonString = "[\"contour_det\", \"great\", \"30\",  \"0\", \"400\",\"500\",\"850\"]";
        boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
        if (bTimeOver){
            strRet = "fail findBackColor: timeOver(contour_det)";
            return strRet;

        }

        JUtilFunctions.delay_duration(3000);

        JUtilFunctions.takeScreenshot();

        int baseY = Config.IMAGE_HEIGHT / 2 + 50;
        double[] pixelsVals = JUtilFunctions.screenshot.get(baseY, 2);
        ptBackColor.x = pixelsVals[0];
        ptBackColor.y = pixelsVals[1];
        ptBackColor.z = pixelsVals[2];

        for (int x = 2; x < 50; x++){
            pixelsVals = JUtilFunctions.screenshot.get(baseY, x);
            if (pixelsVals[0] != ptBackColor.x || pixelsVals[1] != ptBackColor.y || pixelsVals[2] != ptBackColor.z){
                ptBetPannelColor.x = pixelsVals[0];
                ptBetPannelColor.y = pixelsVals[1];
                ptBetPannelColor.z = pixelsVals[2];

                strRet = "success";
                break;
            }
        }

        return strRet;

    }

    //.*==========================================================================
    //.0. first : click sports button.
    //.
    //.
    String clickSportButton(){
        String strRet = null;

        JUtilFunctions.delay_duration(3000);

        JUtilFunctions.takeScreenshot();

        Rect rcAnalyseBase = new Rect(40,100, 270, 200);
        JParamsForTextDet textDetParam = JParamsForTextDet.fromInteger(1);
        strRet = JUtilFunctions.findText("Truc tiep", 0, rcAnalyseBase, result_rects, textDetParam);
        if (strRet.equals("success")){
            Point ptCenterSports = JUtilFunctions.getCenterPoint(result_rects.get(0));
            ptCenterSports.x += (int)(140 / Config.resizeXRatio);
            JUserActions.dispatchTap(ptCenterSports.x, ptCenterSports.y);
            JUtilFunctions.delay_duration(2000);

            // String jsonString = "[\"colorbar_det\", \"great\", \"2\", \"470\",\"300\",\"470\",\"900\",    \"39\",\"39\",\"50\", \"39\",\"39\",\"50\", \"20\" ]";
            String jsonString = String.format("[\"colorbar_det\", \"great\", \"2\", \"470\",\"400\",\"470\",\"900\",    \"%d\",\"%d\",\"%d\", \"%d\",\"%d\",\"%d\", \"20\" ]",
                    (int)ptBetPannelColor.x, (int)ptBetPannelColor.y, (int)ptBetPannelColor.z, (int)ptBetPannelColor.x, (int)ptBetPannelColor.y, (int)ptBetPannelColor.z);

            boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
            if (bTimeOver){
                strRet = "fail clickSportButton: timeOver(colorbar_det)";
            }
            else{
                strRet = "success";
            }
            JUtilFunctions.delay_duration(1000);
            ptCenterSports = null;
        }
        else{
            strRet = "fail clickSportButton: dont find Truc tiep";
        }
        rcAnalyseBase = null;
        return strRet;
    }

    //.*================================================================
    //.func: gotoChannelfromDate
    //.desc: two category, today and early.
    private String gotoChannelfromDate(){

        String strRet = null;
        boolean bToday = false;

        String todayString = JUtilFunctions.getTodayString();
        if (todayString.equals(loadTask.progress_date))
            bToday = true;

        JUtilFunctions.takeScreenshot();

        //.1. click Hom nay button...
        Rect rcAnalyseBase = new Rect(40,240, 160, 240);
        JParamsForTextDet textDetParam = JParamsForTextDet.fromInteger(1);
        strRet = JUtilFunctions.findText("Hom nay", 0, rcAnalyseBase, result_rects, textDetParam);
        if (strRet.equals("success")){

            //. in case and today. no need do more.
            if (bToday == true){
                strRet = "success";
                return strRet;
            }

            Point ptMenuDate = JUtilFunctions.getCenterPoint(result_rects.get(0));
            JUserActions.dispatchTap(ptMenuDate.x, ptMenuDate.y);
            JUtilFunctions.delay_duration(2000);

            ptMenuDate.y += (int)(270 / Config.resizeYRatio);
            JUserActions.dispatchTap(ptMenuDate.x, ptMenuDate.y);
            JUtilFunctions.delay_duration(3000);
            // String jsonString = "[\"colorbar_det\", \"great\", \"2\", \"470\",\"300\",\"470\",\"900\",    \"39\",\"39\",\"50\", \"39\",\"39\",\"50\", \"20\" ]";
            String jsonString = String.format("[\"colorbar_det\", \"great\", \"2\", \"470\",\"400\",\"470\",\"900\",    \"%d\",\"%d\",\"%d\", \"%d\",\"%d\",\"%d\", \"20\" ]",
                    (int)ptBetPannelColor.x, (int)ptBetPannelColor.y, (int)ptBetPannelColor.z, (int)ptBetPannelColor.x, (int)ptBetPannelColor.y, (int)ptBetPannelColor.z);

            boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
            if (bTimeOver){
                strRet = "fail gotoChannelfromDate: timeOver(colorbar_det1)";
                return strRet;
            }
            else{
                //. 2024-3-11
                //. if not today...
                //. select correct date button.

                JUtilFunctions.delay_duration(1000);
                JUtilFunctions.takeScreenshot();

                Rect rcAnalyseBaseForDate = new Rect(0,350, 270, 170);
                //. first, find tomorrow.
                String strTomorrowDate = JUtilFunctions.getTomorrowString();
                strRet = JUtilFunctions.findText(strTomorrowDate, 0, rcAnalyseBaseForDate, result_rects, textDetParam);
                if (strRet.equals("success")){
                    Point ptCenterTomorrow = JUtilFunctions.getCenterPoint(result_rects.get(0));
                    Point ptBaseForScrollLeft = new Point(Config.Screen_Width - 50, ptCenterTomorrow.y);
                    int nScrollCnt = 2;
                    boolean bFindDate = false;
                    while(nScrollCnt-- >0 ) {
                        strRet = JUtilFunctions.findText(loadTask.progress_date, 0, rcAnalyseBaseForDate, result_rects, textDetParam);
                        if (strRet.equals("success")){
                            bFindDate = true;
                            break;
                        }
                        else{
                            JUserActions.scrollToLeft(ptBaseForScrollLeft, Config.Screen_Width - 100, 5);
                        }
                    }

                    if (bFindDate == false){
                        //. error for find date...
                        strRet = "fail gotoChannelfromDate: dont fine date";
                        return strRet;
                    }

                    Point ptDate = JUtilFunctions.getCenterPoint(result_rects.get(0));
                    JUserActions.dispatchTap(ptDate.x, ptDate.y);
                    JUtilFunctions.delay_duration(3000);
                    bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
                    if (bTimeOver){
                        strRet = "fail gotoChannelfromDate: timeover(colorbar2)";
                    }
                    else{
                        strRet = "success";
                        //JUtilFunctions.delay_duration(1000);
                    }
                }
                else{
                    //. error.
                    strRet = "fail gotoChannelfromDate: dont fine tomorrow date";
                }

            }
        }
        rcAnalyseBase = null;

        return strRet;
    }

    //.*================================================================
    //.func: findLeagureSection
    //.desc:
    private String findBettingCategorySection(){
        String strRet = null;

        //.2 for find color bar...
        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        colorBarParam.targetUpColor = ptBackColor;
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 10;
        colorBarParam.fixedVal = 470;
        colorBarParam.startVal = 50;
        colorBarParam.endVal = 850;

        JFuncParams_FindSectionIncluding2Targets param = new JFuncParams_FindSectionIncluding2Targets();
        param.sectionTarget = loadTask.league_name;
        param.secTargetIntOcrParam = 4;

        //. important.
        //. in case this, because section and betting borad color are same, so we use border color...
        param.secWidthforUsingBorder = 55;

        param.target1 = loadTask.team1;
        param.target1OcrParam.strCompMethod = e_PermitIncluding;
        param.target1OcrParam.strPreprocessMethod = e_removeSpace;
        param.target2 = loadTask.team2;
        param.target2OcrParam.strCompMethod = e_PermitIncluding;
        param.target2OcrParam.strPreprocessMethod = e_removeSpace;

        param.tryScrollCnt = 30;
        param.nAnalyseWidth = Config.IMAGE_WIDTH;
        param.neighborCond2Targets = e_UpDownDenseNeighborCond; //. Up/ down layout...
        param.nextSectionInfo = colorBarParam;
        param.ptBetPannelBackUpColor = new Point3(56,255,243);
        param.ptBetPannelBackDownColor = new Point3(39,150,150);
        param.rcDecideforCollapseCond = new Rect(-450, 10, 100,40);
        param.ptPosClickforExpanding = new Point(-200, 50);

        //. in SABA, there is separators same Section per tournament.
        int nTryCnt = 10;
        Point ptOutClickPos = new Point();
        boolean bFindTarget = false;
        Point ptFindSecPos = new Point();
        while(nTryCnt-- > 0){
            //. 1. find league Section and expand it.
            boolean bFindSeciton = JUtilFunctions.findSectionandExpanding(param, ptFindSecPos);
            if (bFindSeciton == true){
                //. 2. find final targets...
                param.bScrollPosChanged = false;
                boolean bFinded = JUtilFunctions.findSectionIncluding2Targets(param, ptOutClickPos);
                if (bFinded == true){
                    bFindTarget = true;
                    break;
                }
                if (param.bScrollPosChanged == false){
                    int nScrolls = (int)(ptFindSecPos.y - colorBarParam.startVal / Config.resizeYRatio) + 100;
                    Point ptBottom = JUtilFunctions.getOrigPointFromBasePoint(new Point(Config.IMAGE_WIDTH / 2, Config.IMAGE_HEIGHT - 100));
                    JUserActions.scrollToLong(ptBottom, nScrolls);
                }
            }
            else{
                //. stop it.
                break;
            }
        }

        if (bFindTarget == true){
            //. do click.
            JUserActions.dispatchTap(ptOutClickPos.x, ptOutClickPos.y);
            JUtilFunctions.delay_duration(2000);

            // String jsonString = "[\"colorbar_det\", \"great\", \"2\", \"470\",\"400\",\"470\",\"900\",    \"39\",\"39\",\"50\", \"39\",\"39\",\"50\", \"20\" ]";
            String jsonString = String.format("[\"colorbar_det\", \"great\", \"2\", \"470\",\"400\",\"470\",\"900\",    \"%d\",\"%d\",\"%d\", \"%d\",\"%d\",\"%d\", \"20\" ]",
                    (int)ptBetPannelColor.x, (int)ptBetPannelColor.y, (int)ptBetPannelColor.z, (int)ptBetPannelColor.x, (int)ptBetPannelColor.y, (int)ptBetPannelColor.z);

            boolean bTimeOver = JUtilFunctions.checkValidation(jsonString, 30000, 2000);
            if (bTimeOver){
                strRet = "fail enter betting room";
            }
            else {
                JUtilFunctions.delay_duration(1000);
                strRet = "success";
            }
        }
        else{
            strRet = "fail findBettingCategorySection: can't find targets Betting.";
        }

        return strRet;
    }

    //.*================================================================
    //.func: findBettingTypeSection
    //.desc:
    private String findBettingTypeSection() {

        String strRet = null;

        JUtilFunctions.delay_duration(2000);

        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        //. for find color bar...
        colorBarParam.targetUpColor = ptBackColor;
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 10;
        colorBarParam.fixedVal = 420;   //. no 470, Up arrow appears.
        colorBarParam.startVal = 50;
        colorBarParam.endVal = 850;

        JFuncParams_FindSectionIncluding2Targets param = new JFuncParams_FindSectionIncluding2Targets();

        String searchKey = null;
        if (loadTask.betTypeCategory.equals(Config.betType_TaiXiu)) {
            searchKey = "Toan Tran - Tai/Xiu";
            param.neighborCond2Targets = e_TableTypeNeighborCond;
        }
        else if (loadTask.betTypeCategory.equals(Config.betType_Handicap)) {
            searchKey = "Toan Tran - Cuoc Chap";
            param.neighborCond2Targets = e_FarVerticalNeighborCond;
        }
        else{
            strRet = "Invalid betTypeCategory";
            return strRet;
        }
        param.sectionTarget = searchKey;
        param.secTargetIntOcrParam = 0;

        param.secWidthforUsingBorder = 70;
        param.target1 = loadTask.betTarget;
        param.target1OcrParam.strCompMethod = e_ExactEqual;
        param.target1OcrParam.strPreprocessMethod = e_removeSpace;


        param.target2 = loadTask.betMark;
        param.target2OcrParam.ocrPattern = e_DigitOnly;
        param.target2OcrParam.strCompMethod = e_ExactEqual;
        param.target2OcrParam.strPreprocessMethod = e_removeSpace;
        param.target2OcrParam.fResizeRate = 1.0f;
        param.target2OcrParam.bNeedMoreContrast = true;

        param.tryScrollCnt = 3;
        param.nAnalyseWidth = Config.IMAGE_WIDTH;
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


    //.*========================================================================
    //.func: completeBet
    //.desc:
    //.
    private String completeBet(){

        String strRet = null;

        JUtilFunctions.delay_duration(1000);

        //. first, find edit ctrl.

        JUtilFunctions.takeScreenshot();

        //. first find edit ctrl.
        JFuncParams_ColorBar colorBarParam = new JFuncParams_ColorBar();
        //. for find color bar...
        colorBarParam.targetUpColor = ptBackColor;
        colorBarParam.targetDownColor = colorBarParam.targetUpColor;
        colorBarParam.nLimitLen = 10;
        colorBarParam.fixedVal = 280;
        colorBarParam.startVal = 500;
        colorBarParam.endVal = 950;
        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, colorBarParam);
        int nSecCnt = retSegments.size();
        if (nSecCnt == 0){
            strRet = "fail completeBet: cant find InputBox";
            return strRet;
        }

        //. 2, click it.
        Point ptCenterBase = new Point(colorBarParam.fixedVal, (retSegments.get(nSecCnt-1).x + retSegments.get(nSecCnt-1).y) / 2);
        Point ptCenter = JUtilFunctions.getOrigPointFromBasePoint(ptCenterBase);
        JUserActions.dispatchTap(ptCenter.x,  ptCenter.y );
        JUtilFunctions.delay_duration(2000);

        //. 3. scroll up fully.
        Point pBase = new Point(270, 900);
        Point pBottom = JUtilFunctions.getOrigPointFromBasePoint(pBase);
        JUserActions.scrollToLong(pBottom, (int)(500 / Config.resizeYRatio));
        JUtilFunctions.delay_duration(2000);

        JUtilFunctions.takeScreenshot();
        //. find buttons.
        Point [] ptNumbers = new Point[10];
        for(int i = 0 ; i<10; i++){
            ptNumbers[i] = new Point();
        }

        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add("5");string_param_list.add("5");
        string_param_list.add("8");string_param_list.add("5");
        Rect rcAnalyseBase = new Rect(0, 500, 450, 400);

        JParamsForTextDet textDetParam = JParamsForTextDet.fromInteger(1);
        String strDigitPannelFind = JUtilFunctions.getTextAreaFromOcr(string_param_list,
                rcAnalyseBase, result_rects, textDetParam);
        if (strDigitPannelFind.equals("success") == false){
            strRet = "fail completeBet: cant digitPannels 1";
            return strRet;
        }

        Rect rc5 = result_rects.get(0);
        Rect rc8 = result_rects.get(1);
        if (rc5.width == 0 || rc8.width == 0){
            strRet = "fail completeBet: cant digitPannels 2";
            return strRet;
        }

        Point pt5 = JUtilFunctions.getCenterPoint(rc5);
        Point pt8 = JUtilFunctions.getCenterPoint(rc8);

        int nCellWidth = (int)(Config.Screen_Width / 4);
        int nCellHeight = (int)((pt8.y - pt5.y));

        int nX = (int)(Config.Screen_Width / 8);
        int nY = (int)(pt5.y - nCellHeight);
        for (int i = 1; i <= 10; i++){
            int nXStride = (i - 1) % 3;
            int nYStride = (i - 1) / 3;
            int idx = i;
            if (idx == 10)
                idx = 0;
            ptNumbers[idx].x = nX + nXStride * nCellWidth;
            ptNumbers[idx].y = nY + nYStride * nCellHeight;
        }

        //. next. input bet amount...
        for (int i = 0; i < loadTask.betAmount.length(); i++) {
            char ch = loadTask.betAmount.charAt(i);

            //. convert ch to int.
            int k = ch - '0';
            JUserActions.dispatchTap(ptNumbers[k].x, ptNumbers[k].y);
            JUtilFunctions.delay_duration(200);
        }

        //. scroll back...
        Point pBase1 = new Point(270, ptNumbers[1].y * Config.resizeYRatio);
        Point pUp1 = JUtilFunctions.getOrigPointFromBasePoint(pBase1);
        JUserActions.scrollToLong(pUp1, -(int)(500 / Config.resizeYRatio));
        JUtilFunctions.delay_duration(1000);

        //. find bet_btn "Đặt cược".
        JUtilFunctions.takeScreenshot();

        //. again find edit box...
        ArrayList<Point> retSegments1 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot, colorBarParam);
        nSecCnt = retSegments1.size();
        if (nSecCnt != 1){
            strRet = "fail completeBet: cant find InputBox 2";
            return strRet;
        }

        Point ptBetNowBase = new Point(colorBarParam.fixedVal + 150, (retSegments1.get(0).x + retSegments1.get(0).y) / 2);
        Point ptBetNow = JUtilFunctions.getOrigPointFromBasePoint(ptBetNowBase);

        // JUserActions.dispatchTap(ptBetNow.x,  ptBetNow.y );
        JUtilFunctions.delay_duration(2000);

        strRet = "success";

        return strRet;
    }

}
