package com.itau.sportsbet;

import android.app.ActivityManager;
import android.util.Log;
import android.view.KeyEvent;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

import java.util.ArrayList;

public class JBetAction_Euro1 extends JBetAction {

    public JLoadTask loadTask = null;
    public Rect sportsMenuRect = null;
    ArrayList<Rect> result_rects = new ArrayList<Rect>();
    float fResizeRate = 1.0f;

    JBetAction_Euro1(JLoadTask loadTask){
        this.loadTask = loadTask;
    }

    // Implementation of the abstract method
    @Override
    String run(){
        String strRet = null;

        strRet = prepareSome();
        if (strRet.equals("success") == false)
            return strRet;

        //.1. click menu "Thể thao"...

        strRet = openMenu();
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(200);

        //. 2.
        /// find football
        strRet = findStringFromImage(loadTask.sports_type, 1, 0);
        if (strRet.equals("success") == false)
            return strRet;
        Point ptCenterFootball = JUtilFunctions.getCenterPoint(result_rects.get(0));
        JUserActions.dispatchTap(ptCenterFootball.x, ptCenterFootball.y);
        JUtilFunctions.delay_duration(3000);

        strRet = openMenu();
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(200);

        //. 3
        strRet = findCountry (loadTask.country_name);
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(1000);
        strRet = openMenu();
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(200);

        //. 4
        strRet = findFootballLeague(loadTask.league_name);
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(2000);

        //. 5
        //.
        strRet = openBettingDetail();
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(2000);

        strRet = findBettingCategorySection_new();
        if (strRet.equals("success") == false)
            return strRet;
        JUtilFunctions.delay_duration(200);

        Log.d("Function", "completeBet");
        strRet = completeBet();
        if (strRet.equals("success") == false)
            return strRet;

        strRet = "success";

        return strRet;
    }


    //.*================================================================
    //.func: prepareSome
    //.desc:
    private String prepareSome(){
        String result = "success";

        JUtilFunctions.delay_duration(5000);
        return result;
    }


    //.*================================================================
    //.func: openMenu _sports
    //.desc:
    private String openMenu() {

        String strRet = "success";
        if(sportsMenuRect == null){

            JUtilFunctions.takeScreenshot();

            Rect rcBase = new Rect(0,Config.IMAGE_HEIGHT - 200 ,200, 200);
            ArrayList<String> string_param_list = new ArrayList<String>();
            string_param_list.add(Config.sports_viet);
            string_param_list.add("0");

            strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list, rcBase, fResizeRate, result_rects, 0,0);
            if (strRet.equals("success") == false)
                return strRet;

            sportsMenuRect = result_rects.get(0);
        }

        Point ptCenterSportsBtn = JUtilFunctions.getCenterPoint(sportsMenuRect);
        JUserActions.dispatchTap(ptCenterSportsBtn.x, ptCenterSportsBtn.y);
        JUtilFunctions.delay_duration(200);
        ptCenterSportsBtn = null;

        return strRet;
    }

    private String findStringFromImage(String key, int nPreprocessMethodForOcrString, int nTextDetectMode){

        String strRet = "fail_findStringFromImage: " + key;

        Rect rcAnalyseBase = new Rect(0,0,Config.IMAGE_WIDTH, Config.IMAGE_HEIGHT);
        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(key);
        string_param_list.add("0");

        try{
            int limit = 20;
            while(true){
                JUtilFunctions.takeScreenshot();

                strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list,
                        rcAnalyseBase, fResizeRate, result_rects, nPreprocessMethodForOcrString, nTextDetectMode);
                if(strRet.equals("success") || limit-- <= 0 ){
                    break;
                }
                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));
                JUtilFunctions.delay_duration(2);
            }
        }catch (Exception e){

        }

        return strRet;
    }

    private String findStringFromImage(ArrayList<String> keyList, int nPreprocessMethodForOcrString, int nTextDetectMode){

        String strRet = "fail_findStringFromImage: " + keyList.get(0);

        int limit = 7;
        Rect rcAnalyseBase = new Rect(0,0,Config.IMAGE_WIDTH, Config.IMAGE_HEIGHT);

        while(true){
            JUtilFunctions.takeScreenshot();

            strRet = JUtilFunctions.getTextAreaFromOcr(keyList,
                    rcAnalyseBase, fResizeRate, result_rects, nPreprocessMethodForOcrString, nTextDetectMode);
            if(strRet.equals("success") || limit-- <= 0 ){
                break;
            }
            JUserActions.scrollUpPage((int)(Config.vscroll_unit * Config.resizeYRatio));
            JUtilFunctions.delay_duration(2);
        }

        return strRet;
    }


    private String findCountry (String country) {
        String strRet = "fail findCountry: " + country;

        ArrayList<String> keyList = new ArrayList<String>();
        keyList.add(country);keyList.add("0");
        keyList.add(Config.menu_more_countries);;keyList.add("0");
        keyList.add(Config.menu_fewer_countries);;keyList.add("0");

        strRet = findStringFromImage(keyList, 1, 1);

        if(strRet.equals("success")){
            if(result_rects.get(0).width != 0){
                Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(0));
                JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
                JUtilFunctions.delay_duration(100);
            }else if(result_rects.get(1).width != 0){
                Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(1));
                JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
                JUtilFunctions.delay_duration(100);

                return findCountry(country);
            }else if(result_rects.get(2).width != 0){
                //. don't find...
                strRet = "fail findCountry: " + country;
            }
        }

        return strRet;
    }

    private String  findFootballLeague(String leagueName){

        String strRet = "fail findFootballLeague: " + leagueName;

        Rect rcAnalyseBase = new Rect(0,0,Config.IMAGE_WIDTH, Config.IMAGE_HEIGHT);
        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(leagueName);string_param_list.add("0");

        int limit = 3;
        while(limit-- > 0){
            JUtilFunctions.takeScreenshot();
            strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list,
                    rcAnalyseBase, fResizeRate, result_rects, 1, 0);
            if(strRet.equals("success") == false){
                // Util.writeLogToFile(this.getApplicationContext(), "Failed to find string " + soccerUnion);
                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));
                continue;
            }

            break;
        }

        if(strRet.equals("success")){
            Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(0));
            JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
            JUtilFunctions.delay_duration(400);
        }

        return strRet;
    }

    private String openBettingDetail(){

        String strRet = "fail openBettingDetail";

        String club1 = loadTask.team1;
        String club2 = loadTask.team2;

        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(club1);string_param_list.add("0");
        string_param_list.add(club2);string_param_list.add("0");

        int limit = 8;

        while(limit-- >0){
            JUtilFunctions.takeScreenshot();

            strRet = findStringFromImage(string_param_list, 0, 0);

            boolean bOK = false;
            if (strRet.equals("success")){
                Rect rc1 = result_rects.get(0);
                Rect rc2 = result_rects.get(1);
                if (rc1.width != 0 && rc2.width != 0 && Math.abs(rc2.y - rc1.y)<100){
                    bOK = true;
                }
            }
            if(!bOK ){
                JUserActions.scrollUpPage((int)(Config.vscroll_unit / Config.resizeYRatio));
                strRet = "fail openBettingDetail";
                continue;
            }

            Point ptCenter = JUtilFunctions.getCenterPoint(result_rects.get(0));
            JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
            strRet = "success";
            break;
        }

        return strRet;
    }

    private String findBettingCategorySection_new(){

        String strRet = "fail findBettingCategorySection";

        String searchKey = null;
        if (loadTask.betTypeCategory.equals("Tai/Xiu"))
            searchKey = "Cuoc Tai/Xiu toan tran";
        else if (loadTask.betTypeCategory.equals("Keo chap"))
            searchKey = "Cuoc chap Chau A toan tran";

        int limit = 8;

        //. for find color bar...
        Point3 targetUpColor = new Point3(70,75,88);
        Point3 targetDownColor = new Point3(70,75,88);
        int nLimitLen = 30;
        int fixedVal = 470;
        int startVal = 150;
        int endVal = 880;
        int nStartY = 300;
        int nEndY = 0;

        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(searchKey);string_param_list.add("0");

        boolean bFindSectionHeader = false;
        while(limit-- >0 ){
            JUtilFunctions.takeScreenshot();

            //. first, find color bar RGB(70,75,88);
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

                    //. and then, if it is expanded?
                    int x = 20;
                    int y = (int)(sectionHead.y + 20);
                    double[] pixelsVals = JUtilFunctions.screenshot.get(y, x);
                    if (pixelsVals[0] >= 235 && pixelsVals[1] >= 235 && pixelsVals[2] >= 235){
                        //. in case expanded. ok.
                    }
                    else{
                        //. one click...
                        Rect rcScreen = JUtilFunctions.getOrigRectFromBaseRect(rcAnalyseBase);
                        Point ptCenter = JUtilFunctions.getCenterPoint(rcScreen);
                        JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
                        JUtilFunctions.delay_duration(500);
                    }

                    //. scroll prop amounts. header's top is 350...
                    if (sectionHead.y > nStartY){
                        int nScrollAmount = (int)sectionHead.y - nStartY;
                        JUserActions.scrollUpPage((int)(nScrollAmount / Config.resizeYRatio));
                    }
                    pixelsVals = null;
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

        if (bFindSectionHeader == false){
            strRet = "fail findBettingCategorySection";
        }
        else{
            //. find action.
            //. find action.
            limit = 3;
            boolean bFindNextSection = false;
            while(limit-- >0 ){
                JUtilFunctions.takeScreenshot();

                //. find next section.
                ArrayList<Point> retSegments = JUtilFunctions.findContinuousSegments(JUtilFunctions.screenshot,
                        fixedVal, nStartY, endVal , true, targetUpColor, targetDownColor,nLimitLen);

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
                Rect rcAnalyseBase = new Rect(0,nStartY, Config.IMAGE_WIDTH, nEndY - nStartY);
                ArrayList<String> string_param_list_forBetDetail = new ArrayList<String>();
                String betDetail = loadTask.betTarget + " " + loadTask.betMark;
                string_param_list_forBetDetail.add(betDetail);string_param_list_forBetDetail.add("0");

                strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list_forBetDetail,
                        rcAnalyseBase, fResizeRate, result_rects, 0, 0);
                if(strRet.equals("success") || bFindNextSection){
                   break;
                }

                JUserActions.scrollUpPage((int)(300 / Config.resizeYRatio));
            }

            if(strRet.equals("success")){
                Rect rc = result_rects.get(0);
                Point ptCenter = JUtilFunctions.getCenterPoint(rc);
                JUserActions.dispatchTap(ptCenter.x, ptCenter.y);
            }
            else{
                strRet = "fail findBettingCategorySection";
            }
        }

        return strRet;
    }



    private void deleteContentofInput(int len) {
        for(int i = 0; i<len ; i++){
            JUserActions.dispatchOneKeyPress(KeyEvent.KEYCODE_DEL);
        }
    }

    private String completeBet(){

        String strRet = "fail";

        ArrayList<String> string_param_list = new ArrayList<String>();

        JUtilFunctions.delay_duration(2000);
        String bet_amount  = loadTask.betAmount;
        String leagueName = loadTask.league_name;
///////////////////////bet amount
        Rect rcAnalyseBase = new Rect(0, 255,  Config.IMAGE_WIDTH, 500);
        JUtilFunctions.takeScreenshot();

        string_param_list.add(leagueName);string_param_list.add("0");
        strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list, rcAnalyseBase, fResizeRate, result_rects, 0, 0);
        if (strRet.equals("success") == false){
            return strRet;
        }

        Rect leagueNameArea = result_rects.get(0);

        // Util.writeLogToFile(this.getApplicationContext(), "League name " + leagueName + leagueNameArea.rect.toString() );

        JUtilFunctions.delay_duration(2000);
        Rect amountRect = new Rect(Config.IMAGE_WIDTH/2,  leagueNameArea.y  , Config.IMAGE_WIDTH/2-1, 100 );
        string_param_list.clear();
        string_param_list.add(Config.input_unit);string_param_list.add("0");
        strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list, amountRect, fResizeRate, result_rects, 0, 0);
        if (strRet.equals("success") == false){
            return strRet;
        }

        Rect inputUnitRes = result_rects.get(0);
        Point ptCenter = JUtilFunctions.getCenterPoint(inputUnitRes);

        // Util.writeLogToFile(this.getApplicationContext(), "Bet Amount " +  inputUnitRes.rect.toString() );
        JUserActions.dispatchTap(ptCenter.x - 70,  ptCenter.y );
        JUtilFunctions.delay_duration(1500);
        deleteContentofInput(8);
        JUserActions.dispatchKeyPress(bet_amount);
        JUtilFunctions.delay_duration(1000);
//    Finding bet button
        JUserActions.scrollToUp(new Point(Config.Screen_Width/2, Config.Screen_Height-Config.CalcYSize(150) ), Config.CalcYSize(300));
        JUtilFunctions.delay_duration(500);
/// click bet button

        Rect betBtnRect = new Rect(0, Config.IMAGE_HEIGHT - 180, Config.IMAGE_WIDTH- 150, 100 );

        JUtilFunctions.takeScreenshot();

        string_param_list.clear();
        string_param_list.add(Config.bet_btn);string_param_list.add("0");
        strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list, betBtnRect, fResizeRate, result_rects, 0, 0);
        if (strRet.equals("success") == false){
            return strRet;
        }

        JUtilFunctions.delay_duration(1000);
        Rect betBtnRes = result_rects.get(0);
        ptCenter = JUtilFunctions.getCenterPoint(betBtnRes);

        JUserActions.dispatchTap(ptCenter.x - 70,  ptCenter.y );
        JUtilFunctions.delay_duration(5000);

        return strRet;
    }

}
