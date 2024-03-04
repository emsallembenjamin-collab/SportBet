package com.itau.sportsbet;

import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class JBetAction_Peak1 extends JBetAction  {

    public JLoadTask loadTask = null;

    public Rect sportsMenuRect = null;
    ArrayList<Rect> result_rects = new ArrayList<Rect>();
    float fResizeRate = 1.0f;

    JBetAction_Peak1(JLoadTask loadTask){
        this.loadTask = loadTask;
    }
    @Override
    String run() {
        String strRet = null;

        strRet = prepareSome();
        if (strRet.equals("success") == false)
            return strRet;

        strRet = gotoChannelfromDate();
        if (strRet.equals("success") == false)
            return strRet;

        strRet = findBettingCategorySection_new();
        if (strRet.equals("success") == false)
            return strRet;

        strRet = findBettingTypeSection();
        if (strRet.equals("success") == false)
            return strRet;

        strRet = completeBetting();
        if (strRet.equals("success") == false)
            return strRet;




        return strRet;
    }


    //.*================================================================
    //.func: prepareSome
    //.desc:
    private String prepareSome(){
        String result = "success";

        //JUtilFunctions.delay_duration(5000);
        return result;
    }

    //.*================================================================
    //.func: gotoChannelfromDate
    //.desc:
    private String gotoChannelfromDate(){
        String strRet = "fail gotoChannelfromDate";
        boolean bToday = false;

        // Get the current date
        Calendar currentDate = Calendar.getInstance();

        // Define the formatter for the date string
        SimpleDateFormat formatter = new SimpleDateFormat("M d");

        // Format the current date to a string
        String todayString = formatter.format(currentDate.getTime());
        if (todayString.equals(loadTask.progress_date))
            bToday = true;


        //.1. find Dirct...
        JUtilFunctions.takeScreenshot();

        Rect rcAnalyseBase = new Rect(400,180 ,140, 160);
        strRet = JUtilFunctions.findText("Yeu thich", rcAnalyseBase, fResizeRate, result_rects, 0, 0);
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
        JUtilFunctions.delay_duration(8000);

        ptCenter = null;
        return strRet;
    }


    //.*================================================================
    //.func: findLeagureSection
    //.desc:
    private String findBettingCategorySection_new(){
        String strRet = "fail findBettingCategorySection_new";

        String searchKey = loadTask.league_name;

        int limit = 30;

        //. for find color bar...
        Point3 targetUpColor = new Point3(237,243,250);
        Point3 targetDownColor = new Point3(237,243,250);
        int nLimitLen = 30;
        int fixedVal = 470;
        int startVal = 50;
        int endVal = 800;

        int nStartY = 300;
        int nEndY = 0;

        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(searchKey);string_param_list.add("0");

        boolean bFindSectionHeader = false;
        while(limit-- >0 ){
            JUtilFunctions.takeScreenshot();

            //. first, find color bar RGB(237,243,250);
            ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                    fixedVal, startVal, endVal , true, targetUpColor, targetDownColor, nLimitLen);

            int nSegCnt = retSegments.size();
            for (int i = 0; i < nSegCnt; i++){
                Point sectionHead = retSegments.get(i);
                Rect rcAnalyseBase = new Rect(0, (int)sectionHead.x, fixedVal, (int)(sectionHead.y - sectionHead.x));
                strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list,
                        rcAnalyseBase, fResizeRate, result_rects, 0, 0);
                if (strRet.equals("success")){
                    bFindSectionHeader = true;

                    //. scroll prop amounts. header's top is 300...
                    if (sectionHead.y > nStartY){
                        int nScrollAmount = (int)sectionHead.y - nStartY;
                        Point scrollPoint  = new Point(270/ Config.resizeYRatio, 800/ Config.resizeYRatio);
                        JUserActions.scrollToUp(scrollPoint, (int)(nScrollAmount / Config.resizeYRatio));
                    }

                    break;
                }
            }
            retSegments = null;

            if (bFindSectionHeader){
                break;
            }
            else{
                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));
            }
        }

        string_param_list = null;

        if (bFindSectionHeader == false){
            strRet = "fail findBettingCategorySection_new";
        }
        else{
            //. find action.
            limit = 3;
            boolean bFindNextSection = false;
            boolean bFindTeams = false;
            while(limit-- >0 ){
                JUtilFunctions.takeScreenshot();

                //. find next section.
                ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                        fixedVal, nStartY, endVal , true, targetUpColor, targetDownColor, nLimitLen);

                int nSegCnt = retSegments.size();
                if (nSegCnt > 0){
                    nEndY = (int)retSegments.get(0).x;
                    bFindNextSection = true;
                }
                else{
                    nEndY = endVal;
                }
                retSegments = null;

                //. do ocr.
                Rect rcAnalyseBase = new Rect(0,nStartY, 200, nEndY - nStartY);
                ArrayList<String> string_param_list_forTeams = new ArrayList<String>();

                if (loadTask.corner_kick == 0){
                    string_param_list_forTeams.add(loadTask.team1);string_param_list_forTeams.add("0");
                    string_param_list_forTeams.add(loadTask.team2);string_param_list_forTeams.add("0");
                }
                else{
                    string_param_list_forTeams.add(loadTask.team1 + "Tong");string_param_list_forTeams.add("0");
                    string_param_list_forTeams.add(loadTask.team2 + "Tong");string_param_list_forTeams.add("0");
                }

                strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list_forTeams,
                        rcAnalyseBase, fResizeRate, result_rects, 0, 0);
                if(strRet.equals("success") && result_rects.get(0).width > 0 && result_rects.get(1).width > 0){
                    bFindTeams = true;
                    break;
                }

                if (bFindNextSection)
                    break;

                JUserActions.scrollUpPage((int)(500 / Config.resizeYRatio));
            }

            if(bFindTeams){
                Rect rc = result_rects.get(0);
                Point ptCenter = JUtilFunctions.getCenterPoint(rc);
                JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
                JUtilFunctions.delay_duration(5000);
            }
            else{
                strRet = "fail findBettingCategorySection";
            }
        }

        return strRet;
    }

    //.*================================================================
    //.func: findBettingTypeSection
    //.desc:
    private String findBettingTypeSection() {
        String strRet = "fail findBettingTypeSection";

        String searchKey = loadTask.betTypeCategory;

        int limit = 1;

        //.1. some scroll up...
        JUserActions.scrollUpPage((int)(250 / Config.resizeYRatio));
        JUtilFunctions.delay_duration(200);

        //. 2. for find color bar...
        Point3 targetUpColor = new Point3(240,240,240);
        Point3 targetDownColor = new Point3(240,240,240);
        int nLimitLen = 30;
        int fixedVal = 470;
        int startVal = 100;
        int endVal = 850;

        int nStartY = 250;
        int nEndY = 0;

        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(searchKey);string_param_list.add("0");

        boolean bFindSectionHeader = false;
        while(limit-- >0 ){
            JUtilFunctions.takeScreenshot();

            //. first, find color bar RGB(240,240,240);
            ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                    fixedVal, startVal, endVal , true, targetUpColor, targetDownColor,nLimitLen);

            int nSegCnt = retSegments.size();
            for (int i = 0; i < nSegCnt; i++){
                Point sectionHead = retSegments.get(i);
                Rect rcAnalyseBase = new Rect(0, (int)sectionHead.x, fixedVal, (int)(sectionHead.y - sectionHead.x));
                strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list,
                        rcAnalyseBase, fResizeRate, result_rects, 0, 0);
                if (strRet.equals("success")){
                    bFindSectionHeader = true;


                    //. scroll prop amounts. header's top is 250...
                    if (sectionHead.y > nStartY){
                        int nScrollAmount = (int)sectionHead.y - nStartY;
                        Point scrollPoint = new Point(270/Config.resizeYRatio, 800/Config.resizeYRatio);
                        JUserActions.scrollToUp(scrollPoint, (int)(nScrollAmount / Config.resizeYRatio));
                    }
                    break;
                }
            }
            retSegments = null;

            if (bFindSectionHeader){
                break;
            }
            else{
                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));
            }
        }

        string_param_list = null;

        if (bFindSectionHeader == false){
            strRet = "fail findBettingTypeSection";
        }
        else{
            JUtilFunctions.takeScreenshot();

            if (loadTask.betTypeCategory.equals("Keo chap") || loadTask.betTypeCategory.equals("Tai/Xiu")) {

                boolean bFindOK = false;

                ArrayList<String> string_param_list_forDetail = new ArrayList<String>();
                int nTotalSearch = 6;
                int nSecHeight = 100;
                int nOverlapHeight = 30;
                int nY = nStartY - 50;

                string_param_list_forDetail.add(loadTask.betTarget);string_param_list_forDetail.add("0");
                string_param_list_forDetail.add(loadTask.betMark);string_param_list_forDetail.add("0");
                for (int i = 0; i < nTotalSearch; i++){
                    Rect rcAnalyseBase = new Rect(0, nY, Config.IMAGE_WIDTH, nSecHeight);
                    strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list_forDetail, rcAnalyseBase, fResizeRate, result_rects, 0, 0);
                    if (strRet.equals("success") && result_rects.get(0).width > 0 && result_rects.get(1).width > 0){

                        Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(1));
                        JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
                        JUtilFunctions.delay_duration(1000);

                        bFindOK = true;
                        break;
                    }
                    nY += (nSecHeight - nOverlapHeight);
                }
                if (bFindOK){
                    strRet = "success";
                }
                else{
                    strRet = "fail findBettingTypeSection";
                }
            }
            else{
                strRet = "fail findBettingTypeSection";
            }

        }
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
        Point3 targetUpColor = new Point3(54,192,101);
        Point3 targetDownColor = new Point3(54,192,101);
        int nLimitLen = 50;
        int fixedVal = 400, startVal = 100, endVal = 400;

        ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                fixedVal, startVal, endVal , true, targetUpColor, targetDownColor,nLimitLen);
        if (retSegments.size() == 1){
            nStartY_TitleBar = (int)retSegments.get(0).x; nEndY_TitleBar = (int)retSegments.get(0).y;

            //. 2. find bet now button...
            Rect rcAnalyseBase = new Rect(260, 800, 270, 160);
            strRet = JUtilFunctions.findText(Config.bet_btn, rcAnalyseBase, fResizeRate,
                    result_rects, 0, 0);
            if (strRet.equals("success")){
                ptBetNowBnt_Center = JUtilFunctions.getCenterPoint(result_rects.get(0));

                targetUpColor = new Point3(255,255,255);
                targetDownColor = new Point3(255,255,255);
                nLimitLen = 45;
                fixedVal = 300; startVal = nEndY_TitleBar + 10; endVal = 450;

                ArrayList<Point> retSegments1 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                        fixedVal, startVal, endVal , true, targetUpColor, targetDownColor,nLimitLen);
                if (retSegments1.size() >= 1){
                    nStartY_EditBar = (int)retSegments1.get(0).x; nEndY_EditBar = (int)retSegments1.get(0).y;

                    //. find 2 color bars.
                    targetUpColor = new Point3(222,239,255);
                    targetDownColor = targetUpColor;
                    nLimitLen = 35;
                    fixedVal = 290; startVal = nEndY_EditBar + 10; endVal = nEndY_EditBar + 160;

                    ArrayList<Point> retSegments2 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                            fixedVal, startVal, endVal , true, targetUpColor, targetDownColor,nLimitLen);

                    targetUpColor = new Point3(255,234,212);
                    targetDownColor = targetUpColor;
                    nLimitLen = 35;
                    fixedVal = 270; startVal = nEndY_EditBar + 100; endVal = nEndY_EditBar + 350;

                    ArrayList<Point> retSegments3 = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                            fixedVal, startVal, endVal , true, targetUpColor, targetDownColor,nLimitLen);

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



                }
                retSegments1 = null;
            }
        }
        retSegments = null;
        return strRet;
    }


}


