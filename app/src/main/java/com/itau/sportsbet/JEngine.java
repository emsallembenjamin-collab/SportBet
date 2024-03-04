package com.itau.sportsbet;

import android.os.Looper;
import android.util.Log;

import com.itau.sportsbet.CommandAPI.APIResponse;
import com.itau.sportsbet.CommandAPI.APIStatus;
import com.itau.sportsbet.CommandAPI.CommandAPI;
import com.itau.sportsbet.CommandAPI.CommandRequest;

import android.os.Handler;

public class JEngine implements Runnable{
    public JLoadTask loadTask;
    Handler handler = new Handler(Looper.getMainLooper());
    APIStatus apiStatus = new APIStatus();
    CommandAPI commandAPI = new CommandAPI();
    boolean isBetingRunning = false;
    public JEngine(JLoadTask loadTask){
        this.loadTask = loadTask;
    }
    @Override
    public void run() {

        apiStatus.jEngine  = this;
        if(isBetingRunning == false){
            commandAPI.callAPI(apiStatus);
            Log.d("Game Routine Running", "Loop End");
            apiStatus.reset();
        }
        handler.postDelayed(this, 5000);
    }

    public void startBetting () {
        int accumlate = 0;
        // Code to be executed periodically
        Log.d("PPPP SportsBet Service", "Started! ");
        isBetingRunning =true;

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
        isBetingRunning = false;
        accumlate++;
        Log.d("PPPP SportsBet Service", "finish one iteration! " + accumlate);
        // Reschedule the task
        //pgh for test.
        //handler.postDelayed(this, interval);
    }
}
