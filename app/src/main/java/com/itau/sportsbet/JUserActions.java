package com.itau.sportsbet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;



public class JUserActions {

    public static void scrollUpPage (int h) {
        Log.d("Scroll Up", "Scroll Up");
        Point p1 = new Point(50, h);
        Point p2 = new Point(50, 1);
        touchEventLong(p1, p2);
    }
    public static void scrollDownPage(int startY, int h) {
        Point p1 = new Point(50, startY);
        Point p2 = new Point(50, startY + h);
        touchEvent(p1, p2);
    }

    public static void scrollToUp(Point p, int h){
        Point p2 = new Point(p.x , p.y-h);
        touchEvent(p, p2);
    }
    public static void scrollToDown(Point p, int h){
        Point p2 = new Point(p.x, p.y + h );
        touchEvent(p, p2);
    }

    public static void scrollToLong(Point p, int h){
        Point p2 = new Point(p.x , p.y-h);
        touchEventLong(p, p2);
    }

    public static void scrollToLeft(Point p, int w){
        Point p2 = new Point(p.x - w, p.y);
        touchEventLong(p, p2, 700);
    }
    public static void scrollToLeft(Point p, int w, int t){
        Point p2 = new Point(p.x - w, p.y);
        touchEventLong(p, p2, t * 100);
    }
    public static void scrollToRight(Point p, int w){
        Point p2 = new Point(p.x + w, p.y);
        touchEventLong(p, p2, 1100);
    }
    public static void touchEvent(Point p1, Point p2){
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("/system/bin/input swipe " + p1.x + " " + p1.y + " "+ p2.x + " " + p2.y+  " \n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void touchEventLong(Point p1, Point p2, int time){
        try {
            Process process = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("/system/bin/input swipe " + p1.x + " " + p1.y + " "+ p2.x + " " + p2.y+  "  " + time + " \n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void touchEventLong(Point p1, Point p2){
        touchEventLong(p1, p2, 1000);
    }


    public static void dispatchTap(double x, double y) {

        Process process = null;
        DataOutputStream os = null;
        try {
            Log.d("Click action", "Click Action");
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("/system/bin/input tap " + x + " " + y + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        finally {
            try {

                if (os != null) {
                    os.close();
                }

                if (process != null) {
                    process.destroy();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static void dispatchLongClick(int x, int y) {
        Point p = new Point(x, y);
        touchEventLong(p, p, 2000);
    }
    public static void copyTextToClipboard(Context context, String text) {
        // Get the Clipboard Manager
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData object holding the text
        ClipData clip = ClipData.newPlainText("label", text);

        // Set the ClipData to the Clipboard
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    public static void copyTextToClipboardfromWorkThread(Context context, String text) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Get the Clipboard Manager
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                // Create a ClipData object holding the text
                ClipData clip = ClipData.newPlainText("label", text);

                // Set the ClipData to the Clipboard
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }
            }
        });
    }


    public static void dispatchKeyPress(String text) {

        try {
            // Get runtime to execute shell command
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            String _text = text.toUpperCase();
            int index = 0;
            for (char ch : _text.toCharArray()) {
                char _ch = text.charAt(index++);

                if (Character.isUpperCase(_ch)) {
                    // Simulate pressing the SHIFT key
                    os.writeBytes("input keyevent " + KeyEvent.KEYCODE_SHIFT_RIGHT + "\n");
                    os.flush();
                }
                int keyCode = convertCharToKeyCode(ch); // Implement this method based on your needs
                if (keyCode != -1) {
                    os.writeBytes("input keyevent " + keyCode + "\n");
                    os.flush();
                }

                if (Character.isUpperCase(_ch)) {
                    // Simulate pressing the SHIFT key
                    os.writeBytes("input keyevent " + KeyEvent.KEYCODE_SHIFT_LEFT + "\n");
                }
            }
            os.flush();
            os.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void dispatchOneKeyPress(int keyCode){
        try {
            // Get runtime to execute shell command
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("input keyevent " + keyCode + "\n");
            os.flush();
            os.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int convertCharToKeyCode(char ch){
        try{
            return KeyEvent.class.getField("KEYCODE_" + ch).getInt(null);
        }catch(Exception e){
            return 0;
        }
    }

    public static void deleteContentofInput(int len) {
        for(int i = 0; i<len ; i++){
            dispatchOneKeyPress(KeyEvent.KEYCODE_DEL);
            JUtilFunctions.delay_duration(50);
        }
    }


}
