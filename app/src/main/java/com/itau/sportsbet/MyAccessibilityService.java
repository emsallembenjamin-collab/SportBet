package com.itau.sportsbet;


import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import org.opencv.core.Size;

public class MyAccessibilityService extends AccessibilityService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    public JLoadTask loadTask = new JLoadTask();
    public static MyAccessibilityService mainService;



    @Override
    public void onServiceConnected() {
	    // Set the type of events that this service wants to listen to. Others
	    // aren't passed to this service.
        // Set up the initial delay and interval

        super.onServiceConnected();
        MyAccessibilityService.mainService = this;

        //.for some init.
        doInit();


        long initialDelay = 1000; // milliseconds
        long interval = 2000; // milliseconds

        // Create a runnable to be executed periodically
        runnable = new JEngine();

        // Schedule the initial execution of the runnable
        handler.postDelayed(runnable, initialDelay);
    }


    // Override methods for handling accessibility events
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Handle accessibility events here
        // For demonstration purposes, we'll generate a touch event when a specific package is in the foreground
        // if ("com.example.targetapp".equals(event.getPackageName())) { }

    }
    @Override
    public void onInterrupt() {
        // This method is called when the service is interrupted

        // Remove the runnable when the activity is destroyed to prevent memory leaks
        Log.d("SportsBet Service", "onInterrupt!");
        handler.removeCallbacks(runnable);
    }


    //////////////////////////////////////////////////////////
    //. for some init process.
    protected void doInit() {

        Assets.doInit(this);

        //. extract tessmodel data...
        Assets.extractTessData(this);

        //. for test. extract some config json files.
//        Assets.extractAllConfigAssets(this);

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

    //.*==========================================================================
    //. paste text...
    private void pasteTextFromClipboard(AccessibilityNodeInfo nodeInfo) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    String textToPaste = item.getText().toString();
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToPaste);
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                }
            }
        }
    }



}
