package com.itau.sportsbet;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {

    public static String rootDir;

    @NonNull
    public static File getLocalDir(@NonNull Context context) {
        return context.getFilesDir();
    }
     @NonNull
    public static String getLocalDataPath(@NonNull Context context) {
        return getLocalDir(context).getAbsolutePath();
    }
    public static String getTessDataPath() {
        String tessDataPath = rootDir + File.separator + "tessdata";
        return tessDataPath;
    }
    public static String getConfigDataPath() {
        String configDataPath = rootDir + File.separator + "config";
        return configDataPath;
    }

    public static String getTestSavedImagePath(int nNum) {
        String testSavedImagePath = rootDir + File.separator + nNum + ".png";
        return testSavedImagePath;
    }

    public static String doInit(@NonNull Context context) {

        Log.d("Assets", "doInit! ");
        rootDir = getLocalDataPath(context);

        //. make tessdata and config sub directories...
        String tessDataPath = getTessDataPath();
        File tessDir = new File(tessDataPath);
        if (!tessDir.exists() && !tessDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + tessDir);
        }

        String configDataPath = getConfigDataPath();
        File configDir = new File(configDataPath);
        if (!configDir.exists() && !configDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + configDir);
        }
        return rootDir;
    }

    public static boolean extractTessData(@NonNull Context context) {
        boolean bRet = false;
        String tessDataPath = getTessDataPath();

        //. create sub directory "tessdata".
        File tessDir = new File(tessDataPath);
        if (!tessDir.exists() && !tessDir.mkdir()) {
            throw new RuntimeException("Can't create directory " + tessDir);
        }

        final File targetFile;
        targetFile = new File(tessDir, Config.OCR_MODEL_NAME);

        if (!targetFile.exists()) {
            AssetManager am = context.getAssets();
            copyFile(am, Config.OCR_MODEL_NAME, targetFile);
            bRet = true;
        }

        return bRet;
    }

    public static void extractAllConfigAssets(@NonNull Context context) {
        AssetManager am = context.getAssets();

        //. create sub directory "tessdata".
        File configDir = new File(getConfigDataPath());

        // Extract all assets to our local directory.
        // All *.traineddata into "tessdata" subdirectory, other files into root.
        try {
            for (String assetName : am.list("")) {
                if (assetName.endsWith(".json")) {
                    final File targetFile;
                    targetFile = new File(configDir, assetName);

                    // if (!targetFile.exists()) {
                    copyFile(am, assetName, targetFile);
                    // }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyFile(@NonNull AssetManager am, @NonNull String assetName,
                                 @NonNull File outFile) {
        try (
                InputStream in = am.open(assetName);
                OutputStream out = new FileOutputStream(outFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String read_bettask_json_from_file(String strTag){
        String betTaskJsonFilePath = getConfigDataPath() + File.separator + "bet_task" + strTag + ".json";
        String json = null;
        try (
                InputStream in = new FileInputStream(betTaskJsonFilePath);

        ) {

            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static String read_action_scenario_from_file(String siteName){
        String actionScenarioFilePath = getConfigDataPath() + File.separator + siteName + ".json";
        String json = null;
        try (
                InputStream in = new FileInputStream(actionScenarioFilePath);

        ) {

            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}
