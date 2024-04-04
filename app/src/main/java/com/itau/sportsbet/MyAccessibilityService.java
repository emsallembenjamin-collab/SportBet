package com.itau.sportsbet;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.opencv.core.Size;

public class MyAccessibilityService extends AccessibilityService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    public JLoadTask loadTask = new JLoadTask();
    public static MyAccessibilityService mainService;

    private static final String TAG = "MyAccessibilityService";

    public boolean  bPageLoadFlag = false;



    @Override
    public void onServiceConnected() {
	    // Set the type of events that this service wants to listen to. Others
	    // aren't passed to this service.
        // Set up the initial delay and interval

        Log.d("PPPPP", "onServiceConnected occurred.");
        // System.out.println("onServiceConnected");

        super.onServiceConnected();
        MyAccessibilityService.mainService = this;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;
        info.eventTypes=AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);


        //.for some init.
        doInit();

        // Creating a work thread
        Thread workThread = new Thread(new Runnable() {
            int  accumlate = 0;

            public String do_test(String strTag){
                String result_string = "unknown result";

                // Perform work here asynchronously
                while (true) {
                    // Code to be executed periodically
                    Log.d("PPPP SportsBet Service", "Started! ");

                    boolean bHasTask = loadTask.hasTask(strTag);
                    if (bHasTask){
                        JActionExecutor loginActionExecutor = loadTask.load_siteActionScenario();
                        if (loginActionExecutor != null){

                            //pgh for test.

                            result_string = loginActionExecutor.run(null);
                            Log.d("PPP AccessibilityService", "Login Finished: " + result_string);
                            //. all done.
                            loginActionExecutor.clear_mem();
                            loginActionExecutor = null;


                            // result_string = "success";

                            //. if success in login...
                            if (result_string.equals("success")){
                                //. next, start betting action scenarios...
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
                        }
                        else{
                            //. parsing error.
                            result_string = "error parsing login Action Scenario";
                        }
                    }
                    else {
                        //. no task. sleep at home...
                        result_string = "Now have no task";
                    }

                    loadTask.reportResult(result_string);

                    accumlate++;
                    Log.d("PPPP SportsBet Service", "finish one iteration! " + accumlate);
                    break;
                }

                return result_string;
            }
            @Override
            public void run() {

                JUtilFunctions.disableSuperuserGranteMsg();

                //do_test("1");ee67805.com
                //do_test("2");
                do_test("3");
            }
        });

        workThread.start(); // Start the work thread

    }



    // Override methods for handling accessibility events
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Handle accessibility events here
        // For demonstration purposes, we'll generate a touch event when a specific package is in the foreground
        // Handle accessibility events here
        if (event == null) {
            return;
        }



        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SELECTED) {

            //Log.d("PPP AccessibilityService", "Event: " + event.toString());
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            // Check if the event is from Chrome
            boolean isFromChrome = "com.android.chrome".equals(packageName) ||
                    "com.chrome.beta".equals(packageName) ||
                    "com.chrome.dev".equals(packageName) ||
                    "com.chrome.canary".equals(packageName);

            if (isFromChrome) {
                String className = event.getClassName() != null ? event.getClassName().toString() : "";
                if (className.equals("android.widget.ProgressBar")) {
                    int nCurrentVal = event.getCurrentItemIndex();
                    // Log.d("PPP AccessibilityService", "ProgressBar Event: " + nCurrentVal);

                    if (nCurrentVal == 100) {
                        Log.d("PPP AccessibilityService", "Event: All Done");
                        bPageLoadFlag = true;
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        // This method is called when the service is interrupted

        // Remove the runnable when the activity is destroyed to prevent memory leaks
        Log.d("SportsBet Service", "onInterrupt!");
        // handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroy() {
        int aa = 100;
        Log.d("SportsBet Service", "onDestroy!");
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.d("SportsBet Service", "onUnbind!");
        return false;
    }


    //////////////////////////////////////////////////////////
    //. for some init process.
    protected void doInit() {

        Assets.doInit(this);

        //. extract tessmodel data...
        Assets.extractTessData(this);

        //. for test. extract some config json files.
        Assets.extractAllConfigAssets(this);

        //. Init openCV and tesseract...
        JUtilFunctions.doInit();


        Size szScreen = getScreenSize();
        Log.d("SportsBet Service", "onServiceConnected! " + szScreen.width + "x" + szScreen.height);

    }

    //////////////////////////////////////////////////////////
    //. some util functions.
    public Size getScreenSize(){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Size scrSize = new Size(metrics.widthPixels, metrics.heightPixels);

        Config.Screen_Width = (int)scrSize.width;
        Config.Screen_Height = (int)scrSize.height;

        Config.resizeXRatio = (float)Config.IMAGE_WIDTH / Config.Screen_Width;
        Config.resizeYRatio = (float)Config.IMAGE_HEIGHT / Config.Screen_Height;
        return scrSize;
    }



}
