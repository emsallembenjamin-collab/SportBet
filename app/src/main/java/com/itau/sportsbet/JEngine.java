package com.itau.sportsbet;

import android.os.Looper;
import android.util.Log;

import com.itau.sportsbet.CommandAPI.APIResponse;
import com.itau.sportsbet.CommandAPI.APIStatus;
import com.itau.sportsbet.CommandAPI.CommandAPI;
import com.itau.sportsbet.CommandAPI.CommandRequest;

import android.os.Handler;

public class JEngine implements Runnable{

    public JLoadTask loadTask = new JLoadTask();
    Handler handler = new Handler(Looper.getMainLooper());
    APIStatus apiStatus = new APIStatus();
    CommandAPI commandAPI = new CommandAPI();
    @Override
    public void run() {
        Log.d("Game Routine Running", "Loop Start");
        commandAPI.callAPI(apiStatus);
        while(apiStatus.getFinished() == false){
            JUtilFunctions.delay_duration(100);
        }
        boolean api_success = true;
        APIResponse apiResponse = apiStatus.getApiResponse();
        if(apiResponse.commandType.equals(APIResponse.CMD_CONFIG)){
            if(apiResponse.data != null && apiResponse.actionScenario != null){
                Assets.save_betconfig_json_from_file(apiResponse.data);
                Assets.save_action_scenario_from_file(apiResponse.actionScenario);
            }else
                api_success = false;
        }else if(apiResponse.commandType.equals(APIResponse.CMD_GAME)){
            if(apiResponse.data != null){
                Assets.save_bettask_json_from_file(apiResponse.data);
                startBetting();
            }else {
                api_success = false;
            }
        }

        if(api_success != true){
            // Setting config and command failed
            // Send report

        }
        Log.d("Game Routine Running", "Loop End");
        apiStatus.reset();
        handler.postDelayed(this, 5000);
    }
    public void startBetting () {
        int accumlate = 0;
        // Code to be executed periodically
        Log.d("PPPP SportsBet Service", "Started! ");

        boolean bHasTask = loadTask.hasTask();
        if (bHasTask){
            JActionExecutor actionExecutor = loadTask.loadActionScenario();
            if (actionExecutor != null){
                //. start really...
                String bet_result_string = null;
                String login_result = actionExecutor.run();
//                        String login_result = "success";
                if (login_result.equals("success")){
                    JBetAction pBetAction = JBetAction.createObject(loadTask);
                    if (pBetAction != null){
                        bet_result_string = pBetAction.run();
                    }
                    else{
                        bet_result_string = "fail_unknown_site";
                    }
                }
                else{
                    bet_result_string = "fail_login";
                }
                loadTask.reportResult(bet_result_string);
                //. all done.
                actionExecutor.clear_mem();
                actionExecutor = null;
            }
            else{
                //. parsing error.
            }
        }
        else {
            //. no task. sleep at home...
        }

        accumlate++;
        Log.d("PPPP SportsBet Service", "finish one iteration! " + accumlate);
        // Reschedule the task
        //pgh for test.
        //handler.postDelayed(this, interval);
    }
}
