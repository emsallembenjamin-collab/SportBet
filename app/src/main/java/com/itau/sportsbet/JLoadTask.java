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
    public String country_vie_name;
    public String league_name;
    public String league_vie_name;
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
        boolean bHasConfig = false;

        //. now for test, use a test json file in assets.
        String jsonConfigString = Assets.read_betconfig_json_from_file();
        String jsonTaskString = Assets.read_bettask_json_from_file();

        if (jsonConfigString != null && jsonTaskString !=null){

            //. parsing...
            bHasConfig = parse_bet_config(jsonConfigString);
            bHasTask = parse_bet_task(jsonTaskString);
        }
        else{
            Log.d("PPPP fail read_bettask_json_from_file", "error");
        }
        return bHasTask && bHasConfig;
    }

    //.*=============================================================
    //.func: loadActionScenario
    //.desc: load action script file and create actionExecutor object.
    //.
    public JActionExecutor loadActionScenario(){

        JActionExecutor actionExecutor = null;

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
    public boolean parse_bet_config(String jsonString){
        boolean bRet = false;
        try {
            // Parse JSON string
            JSONObject jsonObject = new JSONObject(jsonString);

            // Extract data from JSON object
            user_id = jsonObject.getString("username");
            password = jsonObject.getString("password");
            site = jsonObject.getString("site_url");
            bRet = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bRet;
    }

    public boolean parse_bet_task(String jsonString){
        boolean bRet = false;

        try {
            // Parse JSON string
            JSONObject jsonObject = new JSONObject(jsonString);
            Log.d("JsonString_bettask", "jsonString");
            //. 2024-2-24
            category = jsonObject.getInt("category");
            sports_type = jsonObject.getString("sports_type");
            country_name = jsonObject.getString("country_name");
            country_vie_name = jsonObject.getString("country_vie_name");
            league_name = jsonObject.getString("league_name");
            league_vie_name = jsonObject.getString("league_vie_name");
            progress_date = jsonObject.getString("progress_date");
            team1 = jsonObject.getString("team1");
            team2 = jsonObject.getString("team2");
            betTypeCategory = jsonObject.getString("betTypeCategory");
            betTarget = jsonObject.getString("betTarget");
            betMark = jsonObject.getString("betMark");
            betAmount = jsonObject.getString("betAmount");
            bRet = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bRet;
    }



}
