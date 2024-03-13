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
        JUtilFunctions.disableSuperuserGranteMsg();
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
        String result_string = "unknown result";

        isBetingRunning =true;

        boolean bHasTask = loadTask.hasTask();
        if (bHasTask){

            JActionExecutor loginActionExecutor = loadTask.load_siteActionScenario();
            if (loginActionExecutor != null){
                //. start really...

                result_string = loginActionExecutor.run(null);
                Log.d("PPP AccessibilityService", "Login Finished: " + result_string);

                // all done
                loginActionExecutor.clear_mem();
                loginActionExecutor = null;

                if (result_string.equals("success")){

                    JBetAction betActionExecutor = JBetAction.createObject(loadTask);

                    if (betActionExecutor != null){
                        result_string = betActionExecutor.run();
                        Log.d("PPP AccessibilityService", "Bet Finished: " + result_string);
                        betActionExecutor = null;
                    }
                    else{
                        result_string = "error: don't prepare betAction executor";
                    }
                }
                else{
                    result_string = "error parsing login Action Scenario";
                }

            }
            else{
                //. parsing error.
            }
        }
        else {
            //. no task. sleep at home...
            result_string = "Now have no task";
        }
        loadTask.reportResult(result_string);

        accumlate++;
        Log.d("PPPP SportsBet Service", "finish one iteration! " + accumlate);

        isBetingRunning = false;
        // Reschedule the task
        //pgh for test.
        //handler.postDelayed(this, interval);
    }
}
