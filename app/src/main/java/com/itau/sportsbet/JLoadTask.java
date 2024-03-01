package com.itau.sportsbet;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


//.*===========================================================
//.class: JLoadTask
//.desc: communication with bet_server and loading task infos...
public class JLoadTask {

    public String has_bet_task;
    public String user_id;
    public String password;
    public String site;
    public String update_site_info;

    //. 2024-2-24
    public int category = 0;
    public String sports_type;
    public String country_name;
    public String league_name;
    public String progress_date;
    public String team1;
    public String team2;
    public int corner_kick = 0;
    public String betTypeCategory;

    public String betTarget;
    public String betMark;
    public String betAmount;



    //.*=============================================================
    //.func: hasTask
    //.desc: communication to server and download "bet_task.json" data...
    //.
    public boolean hasTask(){
        boolean bHasTask = false;

        //. communication to server and download "bet_task.json" data...
        //. do cording later...
        //. String jsonString = do_read_from_server...


        //. now for test, use a test json file in assets.
        String jsonString = Assets.read_bettask_json_from_file();

        if (jsonString != null){
            //. parsing...
            parse_bet_task(jsonString);
            bHasTask = has_bet_task.equals("yes");
            Log.d("PPPP read and parse bet_task file", "has_bet_task=" + has_bet_task);
        }
        else{
            Log.d("PPPP fail read_bettask_json_from_file", "error");
        }
        return bHasTask;
    }

    //.*=============================================================
    //.func: loadActionScenario
    //.desc: load action script file and create actionExecutor object.
    //.
    public JActionExecutor loadActionScenario(){

        JActionExecutor actionExecutor = null;

        //. first, update avaliable?
        if (update_site_info.equals("true")){
            boolean bDownloaded = downloadActionScenario(site);
            if (bDownloaded == false){
                return null;
            }
        }

        //. second, open local actionScenarioFile
        String jsonString = Assets.read_action_scenario_from_file(site);

        if (jsonString != null){
            //. parsing...
            actionExecutor = JActionExecutor.build(jsonString);
            if (actionExecutor != null){
                Log.d("PPPP build actionExcecutor", "success");
            }
            else{
                Log.d("PPPP build actionExcecutor", "fail");
            }
        }
        else{
            Log.d("PPPP fail read_action_scenario_from_file", "error");
        }

        return actionExecutor;
    }

    //.*=============================================================
    //.func: loadActionScenario
    //.desc: load action script file and create actionExecutor object.
    //.
    public boolean downloadActionScenario(String siteName) {
        boolean bRet = false;

        return bRet;
    }


    //.*=============================================================
    //.func: reportResult
    //.desc: report betting result to server.
    //.
    public boolean reportResult(String result) {
        boolean bRet = false;

        return bRet;
    }

    //.*=============================================================
    //.func: getVerificationCode
    //.desc: send image for verification (base64Str) to server and receive response.
    //.
    public String getVerificationCode(String base64Str) {
        String strRet = "fail";

        return strRet;
    }



    public boolean parse_bet_task(String jsonString){
        boolean bRet = false;

        try {
            // Parse JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract data from JSON object
            has_bet_task = jsonObject.getString("has_bet_task");
            user_id = jsonObject.getString("user_id");
            password = jsonObject.getString("password");
            Config.IMAGE_WIDTH = jsonObject.getInt("full_width");
            Config.IMAGE_HEIGHT = jsonObject.getInt("full_height");
            site = jsonObject.getString("site");
            update_site_info = jsonObject.getString("update_site_info");
            JActionExecutor.limitTime = jsonObject.getInt("limitTime");

            //. 2024-2-24
            category = jsonObject.getInt("category");
            sports_type = jsonObject.getString("sports_type");
            country_name = jsonObject.getString("country_name");
            league_name = jsonObject.getString("league_name");
            progress_date = jsonObject.getString("progress_date");
            team1 = jsonObject.getString("team1");
            team2 = jsonObject.getString("team2");
            betTypeCategory = jsonObject.getString("betTypeCategory");
            betTarget = jsonObject.getString("betTarget");
            betMark = jsonObject.getString("betMark");
            betAmount = jsonObject.getString("betAmount");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bRet;
    }



}
