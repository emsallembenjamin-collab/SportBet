package com.itau.sportsbet;


import static com.itau.sportsbet.Config.DoConfirmMode.e_AfterDone;
import static com.itau.sportsbet.Config.DoConfirmMode.e_Before;
import static com.itau.sportsbet.Config.DoConfirmMode.e_BeforeValidator;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;

//.*=============================================================
//.class: JAction
//.desc: specific one action
//.
abstract class JAction {

    public JActionExecutor  executor = null;
    public String name;
    public String type;
    public ArrayList<String> string_param_list;
    public ArrayList<Double> digit_param_list;
    public ArrayList<String> confirmproc_param_list;
    public int time_limit = 0;
    public int delay = 0;
    public boolean waitPageload = false;

    public ArrayList<String> branch_success;
    public ArrayList<String> branch_fail;
    public JActionValidator validator = null;
    public JAction  confirmProc_firstAction = null;
    public Config.DoConfirmMode  do_confirm_mode = e_Before;

    //. important. which param used? mine or prev result?
    boolean run_as_prev_success = true;
    //. memory result after execute action
    //. sure remember. in result_rects, must original coord x,y
    public ArrayList<Rect> result_rects = new ArrayList<Rect>();
    //. note. result_rects-> is original image coord size...
    public String result_string;

    //. public 2024-3-13
    public boolean parseParamFromConfirmList(ArrayList<String> master_Confirmproc_params) {return true;};

    //.*=========================================================
    //.func: create object...
    //.
    //.*==========================================================
    //.func: build
    //.desc: make itself...
    //.
    public static JAction createObject(JSONObject jobject, JActionExecutor actionExecutor) throws JSONException {

        JAction retAction = null;

        String name = jobject.getString("name");
        String type = jobject.getString("type");

        switch(type){
            case "go_website":
                retAction = new JAction_RunWebBrowser();
                break;
            case "do_ocr":
                retAction = new JAction_Do_Ocr();
                break;
            case "do_click":
                retAction = new JAction_Do_Click();
                break;
            case "do_find_color_bar":
                retAction = new JAction_Do_Find_ColorBar();
                break;
            case "do_input_user_id_password":
                retAction = new JAction_Do_Input_Id_Password();
                break;
            case "do_repeat_scroll":
                retAction = new JAction_Do_Repeat_Scroll();
                break;
            case "do_input_verification_code":
                retAction = new JAction_Do_Input_VerifiCode();
                break;
            case "do_calc":
                retAction = new JAction_Do_Calc();
                break;
            case "find_close_ad":
                retAction = new JAction_FindClose_Ad();
                break;
            case "the_end":
                retAction = new JAction_TheEnd();
                break;
            default:
                break;

        }

        if (retAction != null){
            retAction.executor = actionExecutor;
            retAction.name = name;
            retAction.type = type;

            JSONArray jstring_param_list = jobject.getJSONArray("string_param_list");
            retAction.string_param_list = new ArrayList<String>();
            for (int j = 0; j < jstring_param_list.length(); j++) {
                retAction.string_param_list.add(jstring_param_list.getString(j));
            }

            JSONArray jdigit_param_list = jobject.getJSONArray("digit_param_list");
            retAction.digit_param_list = new ArrayList<Double>();
            for (int j = 0; j < jdigit_param_list.length(); j++) {
                retAction.digit_param_list.add(jdigit_param_list.getDouble(j));
            }

            JSONArray jconfirmproc_param_list = jobject.getJSONArray("confirmproc_param_list");
            retAction.confirmproc_param_list = new ArrayList<String>();
            for (int j = 0; j < jconfirmproc_param_list.length(); j++) {
                retAction.confirmproc_param_list.add(jconfirmproc_param_list.getString(j));
            }


            retAction.time_limit = jobject.getInt("time_limit");
            retAction.delay = jobject.getInt("delay");
            retAction.waitPageload = jobject.getBoolean("waitPageload");


            //. passing "branch"
            JSONObject branch = jobject.getJSONObject("branch");

            JSONArray jsuccess = branch.getJSONArray("success");
            retAction.branch_success = new ArrayList<String>();
            for (int j = 0; j < jsuccess.length(); j++) {
                retAction.branch_success.add(jsuccess.getString(j));
            }
            JSONArray jfail = branch.getJSONArray("fail");
            retAction.branch_fail = new ArrayList<String>();
            for (int j = 0; j < jfail.length(); j++) {
                retAction.branch_fail.add(jfail.getString(j));
            }

            //. 2024-3-4.
            if (retAction.time_limit > 0){
                JSONArray validator_param_list = jobject.getJSONArray("validator_param_list");
                JActionValidator validator = JActionValidator.createObject(retAction, validator_param_list);
                boolean bValidParam = validator.build(validator_param_list);
                if (bValidParam){
                    retAction.validator = validator;
                }
                else{
                    validator = null;
                }
            }

        }

        return retAction;
    }


    //.*==========================================================
    //.func: run
    //.desc: run one action...
    //.
    public boolean run(JAction prevAction){
        boolean bFinished = false;
        Log.d("PPPP Action: ", "Started! :" + name);

        MyAccessibilityService.mainService.bPageLoadFlag = false;

        //. 2024-3-7
        //. process confirm proc...
        if (confirmProc_firstAction != null && do_confirm_mode == e_Before){
            executor.run(confirmProc_firstAction);
        }


        bFinished = run_internel(prevAction);

        if (confirmProc_firstAction != null && do_confirm_mode == e_BeforeValidator){
            executor.run(confirmProc_firstAction);
        }

        if (validator != null && bFinished == false){
            bFinished = validator.check();
        }
        else{
            // JUtilFunctions.delay_duration(delay);
        }
        JUtilFunctions.delay_duration(delay);

        //.2024-3-7
        //. process confirm proc...
        if (bFinished == false && confirmProc_firstAction != null && do_confirm_mode == e_AfterDone){
            executor.run(confirmProc_firstAction);
        }

        Log.d("PPPP Action: ", "Ended! : " + name + ": " + result_string);
        return bFinished;
    }

    //.*==========================================================
    //.func: run
    //.desc: run one action...
    //.
    public abstract boolean run_internel(JAction prevAction);

};

//.*=============================================================
//.class: JActionList
//.desc: specific actions list
//.
class JActionList extends ArrayList<JAction> {

    public JAction find(String name) {
        JAction retAction = null;
        int nActionCnt = size();
        for (int i = 0; i < nActionCnt; i++){
            JAction act = get(i);
            if (act.name.equals(name)){
                retAction = act;
                break;
            }
        }
        return retAction;
    }

    public JAction first(){
        JAction retAction = null;
        int nActionCnt = size();
        if (nActionCnt > 0){
            retAction = get(0);
        }
        return retAction;
    }

    public JAction next(JAction act) {

        boolean bRunAsPrevSuccess;
        JAction retAction = null;
        ArrayList<String> command = null;
        if (act.result_string.equals("success")){
            command = act.branch_success;
            bRunAsPrevSuccess = true;
        }
        else if (act.result_string.equals("exception")){
            return null;
        }
        else{
            command = act.branch_fail;
            bRunAsPrevSuccess = false;
        }

        int nParamCnt = command.size();
        if (nParamCnt > 0){
            String nextActName = command.get(0);
            retAction = find(nextActName);
            if (retAction != null){
                retAction.run_as_prev_success = bRunAsPrevSuccess;
            }
        }

        return retAction;
    }
};




//.*=============================================================
//.class: JActionExecutor
//.desc: Main class of action executing
//.
public class JActionExecutor {

    public JActionList actionList;
    public String       last_result_string = "unknown result";

    //. need limit time until done
    public static int   limitTime = Config.default_limitTime;       //. ms, default 1.5min 60000 * 1.5

    //.*=============================================================
    //.func: build
    //.desc: important...
    //.
    public static JActionExecutor build(String jsonString){
        JActionExecutor actionExecutor = null;
        JActionList newActionList = null;

        try {
            actionExecutor = new JActionExecutor();
            newActionList = new JActionList();
            actionExecutor.actionList = newActionList;

            JSONObject root = new JSONObject(jsonString);
            JSONArray action_list = root.getJSONArray("action_list");

            int nActionCnt = action_list.length();
            for (int i = 0; i < nActionCnt; i++) {
                JSONObject jobject = action_list.getJSONObject(i);
                JAction newAction = JAction.createObject(jobject, actionExecutor);
                newActionList.add(newAction);
            }

            //. 2024-3-7
            //. new intro confirm processing.
            actionExecutor.prepare_confirmProcessing();

        } catch (Exception e) {
            e.printStackTrace();

            actionExecutor = null;
            newActionList = null;
        }

        return actionExecutor;
    }

    //.*=============================================================
    //.func: prepare_confirmProcessing
    //.desc:
    //. 2024-3-7
    //. new intro confirm process...
    public int prepare_confirmProcessing(){
        int nRet = 0;

        int nActionCnt = actionList.size();
        for (int i = 0; i < nActionCnt; i++){
            JAction action = actionList.get(i);
            int nConfirmParamCnt = action.confirmproc_param_list.size();
            if (nConfirmParamCnt >= 2){
                String strFirstConfirmProcActName = action.confirmproc_param_list.get(0);
                action.confirmProc_firstAction = actionList.find(strFirstConfirmProcActName);
                String confirmProc_do_mode = action.confirmproc_param_list.get(1);
                int nConfirmMode = Integer.parseInt(confirmProc_do_mode);
                action.do_confirm_mode = Config.DoConfirmMode.fromInteger(nConfirmMode);

                //. 2024-3-13
                boolean b = action.confirmProc_firstAction.parseParamFromConfirmList(action.confirmproc_param_list);

                nRet++;
            }
        }

        return nRet;
    }

    //.*=============================================================
    //.func: run
    //.desc: run sequential action list.
    //.
    public String run(JAction actionNow){

        // Start measuring elapsed time
        final long startTime = SystemClock.elapsedRealtime();

        if (actionNow == null){
            actionNow = actionList.first();
        }
        JAction actionPrev = null;
        while(actionNow != null){

            boolean bFinished = actionNow.run(actionPrev);
            if (bFinished == true){
                Log.d("PPP AccessibilityService", "finished from: " + actionNow.name);
                break;
            }

            // Calculate elapsed time
            final long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTime;
            if (elapsedTimeMillis > JActionExecutor.limitTime){
                last_result_string = "time is over";
                Log.d("PPP AccessibilityService", "finished from: " + last_result_string);
                break;
            }

            actionPrev = actionNow;
            actionNow = actionList.next(actionNow);
        }

        return last_result_string;
    }


    //.*=============================================================
    //.func: clear memory
    //.desc: clear action list.
    //.
    void clear_mem(){
        int nActionCnt = actionList.size();
        for (int i = 0; i < nActionCnt; i++)
        {
            JAction action = actionList.get(i);
            action = null;
        }
        actionList.clear();
    }

}
