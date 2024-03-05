package com.itau.sportsbet;

import static com.googlecode.tesseract.android.TessBaseAPI.OEM_LSTM_ONLY;
import static com.googlecode.tesseract.android.TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

public class JUtilFunctions {

    public static TessBaseAPI tess;
    public static TextNormalDetector textNormalDetector;

    public static Mat originScreenShot;
    public static Mat screenshot;

    //.*===========================================================================
    public static boolean doInit(){
        boolean bRet = false;

        //. first init openCV library.
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization.");
            return false;
        }

        //. second init tesseract...
        tess = new TessBaseAPI();
        // Create TessBaseAPI instance (this internally creates the native Tesseract instance)
        if (!tess.init(Assets.rootDir, "vie", OEM_LSTM_ONLY)) { // could be multiple languages, like "eng+deu+fra"
            tess.recycle();
            Log.d("PPPP Init Tesseract", "Fail!");
            return false;
        }
        tess.setPageSegMode(PSM_SINGLE_BLOCK);

        //. third. create text detector...
        textNormalDetector = new TextNormalDetector(MyAccessibilityService.mainService);

        //. four. some create mats.
        originScreenShot = new Mat();
        screenshot = new Mat();

        bRet = true;
        return bRet;
    }

    //.*===========================================================================
    public static double min(double x, double y) {
        return (x > y) ? y : x;
    }

    public static double max(double x, double y) {
        return (x > y) ? x : y;
    }

    public static double min4(double x1, double x2, double x3, double x4) {
        return min(min(x1, x2), min(x3, x4));
    }

    public static double max4(double x1, double x2, double x3, double x4) {
        return max(max(x1, x2), max(x3, x4));
    }

    //.*==================================================================
    //. launchChrome.
    //.
    public static void launchChrome(Context context, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so let's try with the default browser
            intent.setPackage(null);
            context.startActivity(intent);
        }
    }

    //.*================================================================================
    public static void delay_duration(int duration){
        try{
            Thread.sleep(duration);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Rect getOrigRectFromBaseRect(Rect rcBase){
        Rect rcOrig = new Rect((int)(rcBase.x / Config.resizeXRatio),
                (int)(rcBase.y / Config.resizeYRatio),
                (int)(rcBase.width / Config.resizeXRatio),
                (int)(rcBase.height / Config.resizeYRatio));

       return rcOrig;
    }

    public static void changeToOrigRectFromBaseRect(Rect rcBase){

        rcBase.x = (int)(rcBase.x / Config.resizeXRatio);
        rcBase.y = (int)(rcBase.y / Config.resizeYRatio);
        rcBase.width = (int)(rcBase.width / Config.resizeXRatio);
        rcBase.height = (int)(rcBase.height / Config.resizeYRatio);
    }

    public static void offsetRectList(ArrayList<Rect> rcList, int offsetX, int offsetY){
        int result_rect_cnt = rcList.size();
        for (int i = 0; i < result_rect_cnt; i++){
            Rect rc = rcList.get(i);
            rc.x += offsetX;
            rc.y += offsetY;
        }
    }

    public static void offsetRect(Rect rcTarget, int offsetX, int offsetY){
        rcTarget.x += offsetX;
        rcTarget.y += offsetY;
    }

    public static Point getOrigPointFromBasePoint(double x, double y){
        Point pt = new Point((x / Config.resizeXRatio), (y / Config.resizeYRatio));
        return pt;
    }


    //.*===============================================================================
    public static Bitmap extractArea(Bitmap src, Rect rect){
        Bitmap bitmap = Bitmap.createBitmap(src, rect.x, rect.y, Math.min(rect.width, src.getWidth()-rect.x),  Math.min(rect.height, src.getHeight()- rect.y));
        return bitmap;
    }
    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",")  + 1),
                Base64.DEFAULT
        );
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        String base64Image =Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        base64Image = base64Image.replace("\n", "").replace("\r", ""); // Remove newlines and carriage returns
        return base64Image;
    }
    public static Mat ConvertBmpToMat(Bitmap bitmap){
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat, true);
        if(mat.channels() == 4){
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        }

        return mat;
    }

    public static boolean areSameScreens(Mat mat1, Mat mat2) {

        try{
            // Convert to grayscale
            Mat grayMat1 = new Mat();
            Mat grayMat2 = new Mat();

            Imgproc.cvtColor(mat1, grayMat1, Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(mat2, grayMat2, Imgproc.COLOR_RGB2GRAY);

            if(grayMat1.width() != grayMat2.width()  ||  grayMat1.height() != grayMat2.height()){
                return false;
            }
            // Find the absolute difference
            Mat diff = new Mat();
            Core.absdiff(grayMat1, grayMat2, diff);

            // Thresholding the difference to filter out minor changes
            Mat thresh = new Mat();
            Imgproc.threshold(diff, thresh, 10, 255, Imgproc.THRESH_BINARY);

            // Count non-zero pixels
            int nonZeroCount = Core.countNonZero(thresh);

            // Release memory
            grayMat1.release();
            grayMat2.release();
            diff.release();
            thresh.release();

            // Return true if changes are below a certain threshold (this number might need adjusting)
            Log.d("NoneZeroCount" , "" + nonZeroCount);
            return nonZeroCount < 4000; // Assuming 1000 is the threshold of "small changes"
        }catch (Exception e){
            return false;
        }
    }
    public static String removeDiacriticalMarks(String text) {
        if(text == null ) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static Rect ExtendRect(Rect srcRect , int offset, int limitCols, int limitRows){
        Rect rcRet = new Rect(srcRect.x - offset,srcRect.y - offset,
                srcRect.width + 2 * offset,srcRect.height + 2 * offset);
        rcRet.x = Math.max(rcRet.x, 0);
        rcRet.y = Math.max(rcRet.y, 0);

        int right = Math.min(rcRet.x + rcRet.width, limitCols);
        int bottom = Math.min(rcRet.y + rcRet.height, limitRows);
        rcRet.width = right - rcRet.x;
        rcRet.height = bottom - rcRet.y;

        return rcRet;
    }

    public static void checkRectBoundary(Rect srcRect , int nLimitX, int nLimitY){
        if (srcRect.x < 0)
            srcRect.x = 0;
        if (srcRect.y < 0)
            srcRect.y = 0;
        if (srcRect.x + srcRect.width >= nLimitX)
            srcRect.width = nLimitX - srcRect.x;
        if (srcRect.y + srcRect.height >= nLimitY)
            srcRect.height = nLimitY - srcRect.y;
    }

    public static void writeLogToFile(Context context, String logText) {

        //pgh for test
        /*
        // Filename for the log
        String filename = "app_log.txt";
        Log.d("Log", logText);
        // Get the external storage directory for this app's private files
        File file = new File(context.getExternalFilesDir(null), filename);

        try {
            // Open a file output stream for writing, with append mode
            FileOutputStream fos = new FileOutputStream(file, true);

            // Write the log text with a newline character
            fos.write((logText + "\n").getBytes());

            // Close the file output stream
            fos.close();
        } catch (IOException e) {
            // Log an error message if something goes wrong
            Log.e("WriteLog", "Error writing to log file", e);
        }
        */
    }

    public static boolean fuzyStringCompare(String src, String dest){
        if(src.length() < dest.length()){
            return false;
        }
        dest = dest.replaceAll(" ", "");
        src = src.replaceAll(" ", "");
//        String _src = src.toLowerCase();
//        String _dest = dest.toLowerCase();
        for(int offset = 0; offset <= src.length() - dest.length(); offset++){
            String __src = src.substring(offset);
            if(fuzyStringCompareOffset(__src, dest)){
                return  true;
            }
        }
        return false;
    }

    public static boolean fuzyStringCompareOffset(String src, String dest){
        try{
            int disCount = 0;
            if(src.length() < dest.length()){
                return false;
            }
            for(int index = 0; index< dest.length(); index ++){
                char s_c = src.charAt(index);
                char d_c = dest.charAt(index);
                if(s_c != d_c ){
                    if(s_c== 'o'){
                        if(d_c == 'a') {
                            continue;
                        }
                    }
                    if(s_c == 'a'){
                        if(d_c == '0' || d_c == 'o' || d_c=='d'){
                            continue;
                        }
                        return false;
                    }
                    if(s_c == 'd') {
                        if(d_c == 'a' || d_c == 'o' || d_c=='đ')
                            continue;
                        else
                            return false;
                    }
                    if(s_c == 'l'){
                        if(d_c == '1' || d_c == 'i' || d_c == ']'){
                            continue;
                        }else return false;
                    }
                    if(s_c == '1'){
                        if(d_c == 'l' || d_c == 'i' ){
                            continue;
                        }
                    }
                    if(s_c == 'i'){
                        if(d_c == 'l' || d_c == 'j') {
                            continue;
                        }
                    }
                    if(s_c == 't'){
                        if(d_c =='1' )
                            continue;
                    }
                    if(s_c == 'Đ'){
                        if(d_c == 'D')
                            continue;
                    }
                    disCount++;
                    return false;
                }
            }
            if(dest.length()> 10){
                if(disCount *10  > 2 * dest.length()){
                    return false;
                }
            }else if(disCount >3){
                return false;
            }else if(dest.length() <4 ){
                if(disCount > 0) return false;
            }
            return true;
        }catch (Exception e){
            return  false;
        }
    }

    public static void SaveMatFile (Mat mat, Context context){
        try {
            // Specify your image's file path
            File file = new File(context.getExternalFilesDir(null), "" + Math.random()* 10000+ "_detect.png");
            Log.d("File dir", context.getExternalFilesDir(null).getAbsolutePath());
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);


            Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bmp);

            // Compress the bitmap and write it to the file
            // You can choose the format (PNG or JPEG) and quality
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // 100 is max quality

            // Don't forget to flush and close the output stream
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap takeScreenshot() {

        Process process = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            process = Runtime.getRuntime().exec("su");

            outputStream = new DataOutputStream(process.getOutputStream());
            inputStream = process.getInputStream();

            // Execute the screencap command and capture its output
            outputStream.writeBytes("/system/bin/screencap -p\n");
            outputStream.flush();

            // Read the output into a bitmap
            Bitmap _bmpScreenshot = BitmapFactory.decodeStream(inputStream);
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
            // save Bitmap to member of sa
            Utils.bitmapToMat(_bmpScreenshot, JUtilFunctions.originScreenShot, true);
            Imgproc.resize(JUtilFunctions.originScreenShot, JUtilFunctions.screenshot, new Size(), Config.resizeXRatio, Config.resizeYRatio);
            JUtilFunctions.writeLogToFile(MyAccessibilityService.mainService, "Take Screen shot");
            return _bmpScreenshot;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JUtilFunctions.writeLogToFile(MyAccessibilityService.mainService, "Take Screen shot Faileds " + e.getMessage());
            // Handle exceptions
        }

        finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                if (process != null) process.destroy();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle IOException
            }
        }

        return null;
    }


    public static Point getCenterPoint(Rect rc){
        Point pt = new Point(rc.x + rc.width / 2, rc.y + rc.height / 2);
        return pt;
    }

    //.*=================================================================================
    //.func: findContinuousSegments
    //.desc:
    //.
    public static Mat detectLines(Mat image, int cannyThres1, int threshold, int minLineLength, int maxLineGap){

        // Convert the image to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Detect edges using the Canny edge detector
        Mat edges = new Mat();
        Imgproc.Canny(gray, edges, cannyThres1, cannyThres1 * 3);

        // Apply Hough transform to detect line segments
        Mat lines = new Mat();
        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, threshold, minLineLength, maxLineGap);

        return lines;
    }


    //.*=================================================================================
    //.func: findContinuousSegments
    //.desc:
    //.
    public static ArrayList<Point> findContinuousSegments(Mat image,
                                                           int fixedVal, int startVal, int endVal , boolean bVert,
                                                          Point3 targetUpColor, Point3 targetDownColor, int nLimitLen) {
        ArrayList<Point> continuousSegments = new ArrayList<Point>();

        int currentSegmentStart = -1;

        // Iterate through the vertical line
        for (int v = startVal; v < endVal; v++) {

            double[] pixelsVals = null;
            if (bVert){
                pixelsVals = image.get(v, fixedVal);
            }
            else{
                pixelsVals = image.get(fixedVal, v); // Get the color of the current pixel
            }

            // Check if the current pixel color matches the target color
            if ((pixelsVals[0] >= targetDownColor.x) && (pixelsVals[0] <= targetUpColor.x) &&
                    (pixelsVals[1] >= targetDownColor.y) && (pixelsVals[1] <= targetUpColor.y) &&
                    (pixelsVals[2] >= targetDownColor.z) && (pixelsVals[2] <= targetUpColor.z))
            {
                // If this is the start of a new segment, record its position
                if (currentSegmentStart == -1) {
                    currentSegmentStart = v;
                }
            } else {
                // If we were in a segment and found a different color, record the segment
                if (currentSegmentStart != -1) {
                    continuousSegments.add(new Point(currentSegmentStart, v - 1));
                    currentSegmentStart = -1;
                }
            }
        }

        // If the last segment extends to the end of the line, record it
        if (currentSegmentStart != -1) {
            continuousSegments.add(new Point(currentSegmentStart, endVal - 1));
        }

        //. filter by limit length.
        int nSegCnt = continuousSegments.size();
        for (int i = 0; i < nSegCnt; i++){
            Point sc = continuousSegments.get(i);
            if (sc.y - sc.x < nLimitLen){
                continuousSegments.remove(i);
                i--;
                nSegCnt--;
            }
        }

        return continuousSegments;
    }


    public static String removeNonLetters(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            // Check if the character is a letter or a space
            if (Character.isLetter(ch) || Character.isWhitespace(ch)) {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public static String preprocessForOcrString(String input, int nPreprocessMethodForOcrString){
        String strRet = null;

        //. for find country name...
        if (nPreprocessMethodForOcrString == 1){
            //. remove (123)...
            strRet = removeNonLetters(input).trim();
        }
        else{
            strRet = input;
        }

        return strRet;

    }
    public static String removeSpaces(String input) {
        // Using regular expression to replace all spaces with an empty string
        return input.replaceAll("\\s", "");
    }
    public static boolean compareString(String ocrStr, String exactStr, int nCompareMethod){
        boolean bRet = false;

        String prep_OcrStr = removeSpaces(removeDiacriticalMarks(ocrStr).toUpperCase());
        String prep_exactStr = removeSpaces(removeDiacriticalMarks(exactStr).toUpperCase());

        if (prep_OcrStr.equals(prep_exactStr))
            bRet = true;
        else{
            switch(nCompareMethod){
                case 0:{
                    if (prep_OcrStr.contains(prep_exactStr))
                        bRet = true;
                }
                break;
                case 1:{
                    if (prep_OcrStr.contains(prep_exactStr))
                        bRet = true;
                }
                break;
                case 2:{
                    String strComp = prep_exactStr;
                    if (prep_exactStr.length() >= 3) {
                        // Extract the first three characters using substring
                        strComp = prep_exactStr.substring(0, 3);
                    }
                    if (prep_OcrStr.contains(strComp))
                        bRet = true;
                }
                break;

            }
        }

        return bRet;
    }

    //.*=====================================================================================
    //. main function of do_ocr
    //. first, do ocr per rect of rcTargets,
    //. next, match the result and string of string_param_list...
    //. according of it... set rcRetList...
    public static String do_ocr(Mat image, ArrayList<Rect> rcTargets, float fResizeRate,
                                ArrayList<Rect> rcRetList, ArrayList<String> string_param_list, int nPreprocessMethodForOcrString) {

        String strRet = "fail";

        rcRetList.clear();
        //. first , prepare result rect list.
        int nParamCnt = string_param_list.size();
        if (nParamCnt == 0 || nParamCnt % 2 != 0){
            //. invalid params.
        }
        else {
            int nTargetCnt = nParamCnt / 2;

            //. 2024-2-26.
            //. avoid including compare weakness, need remember the shortest result.
            //. 2th array, first element is rect's index, and second element is current string's length...
            int[][] matchRes = new int[nTargetCnt][2];
            int nMaxLenInitVal = 10000; // set big value...
            for (int i = 0; i < nTargetCnt; i++){
                Rect rcRet = new Rect(0,0,0,0);
                rcRetList.add(rcRet);

                matchRes[i][0] = -1;
                matchRes[i][1] = nMaxLenInitVal;
            }

            //. do ocr.
            int nTargetRectCnt = rcTargets.size();
            for (int i = 0; i < nTargetRectCnt; i++){
                Rect rc = rcTargets.get(i);

                String ocrStr = readStringbyOcrfromFullImage(image, rc);
                String preprocessOcrStr = preprocessForOcrString(ocrStr, nPreprocessMethodForOcrString);

                for (int j = 0; j < nTargetCnt; j++){
                    String strTarget = string_param_list.get(2 * j);
                    if (strTarget.equals("$user_id"))
                        strTarget = MyAccessibilityService.mainService.loadTask.user_id;

                    int nCmpMethods = Integer.parseInt(string_param_list.get(2 * j + 1));

                    boolean bEqual = compareString(preprocessOcrStr, strTarget, nCmpMethods);
                    /*
                    if (bEqual == false){
                        bEqual = JUtilFunctions.fuzyStringCompare(ocrStr, strTarget);
                    }
                    */
                    int nOcrLen = ocrStr.length();
                    if (bEqual == true){
                        //. set temporary result.
                        if (nOcrLen < matchRes[j][1]){
                            matchRes[j][0] = i;
                            matchRes[j][1] = nOcrLen;
                        }
                    }

                }
            }

            for (int j = 0; j < nTargetCnt; j++) {
                if (matchRes[j][1] < nMaxLenInitVal){
                    Rect rc = rcTargets.get(matchRes[j][0]);
                    Rect rcRet = rcRetList.get(j);
                    rcRet.x = rc.x;rcRet.y = rc.y;
                    rcRet.width = rc.width;rcRet.height = rc.height;
                    strRet = "success";

                }
            }

            matchRes = null;
        }

        return strRet;
    }


    //.*=====================================================================================
    //. readStringbyOcr
    //. the simplest case...
    //.
    public static String readStringbyOcr(Mat image) {

        String strRet = null;

        int size = (int) (image.total() * image.elemSize());
        byte[] byteArray = new byte[size];
        image.get(0, 0, byteArray);

        int nCols = image.cols();
        int nRows = image.rows();
        int nChannels = image.channels();
        tess.setImage(byteArray, nCols, nRows, nChannels,nCols * nChannels);
        strRet = tess.getUTF8Text();
        byteArray = null;

        return strRet;
    }

    //.*=====================================================================================
    //. readStringbyOcr
    //. the simplest case...
    //.
    public static String readStringbyOcrfromFullImage(Mat fullImage, Rect rcTarget) {

        String strRet = null;

        int limitWidth = fullImage.cols();
        int limitHeight = fullImage.rows();
        Rect rcExtend = ExtendRect(rcTarget , 5, limitWidth, limitHeight);
        Mat txtAreaMat = fullImage.submat(rcExtend);
        Mat resizedtxtAreaMat = null;

        //. 2024-2-26.
        //. I will automatic resize.
        //. in future, must remove parameter "fResizeRate"
        int nOrigHeight = txtAreaMat.rows();
        if (nOrigHeight < Config.tesseractDetaultCharHeight){
            float fNewResizeRate = Config.tesseractDetaultCharHeight / nOrigHeight;
            resizedtxtAreaMat = new Mat();
            Imgproc.resize(txtAreaMat, resizedtxtAreaMat, new Size(), fNewResizeRate, fNewResizeRate);
        }
        else{
            resizedtxtAreaMat = txtAreaMat;
        }
        strRet = readStringbyOcr(resizedtxtAreaMat);

        return strRet;
    }


    public static String getTextAreaFromOcr(ArrayList<String> string_param_list,
                                            Rect rcAnalyseBase, float fResizeRate, ArrayList<Rect> result_rects,
                                            int nPreprocessMethodForOcrString, int nTextDetectMode){
        String result_string = "fail_getTextAreaFromOcr";

        Rect rcForOcr = JUtilFunctions.getOrigRectFromBaseRect(rcAnalyseBase);
        Mat analyseAreaMat = JUtilFunctions.screenshot.submat(rcAnalyseBase);

        //. get text detector...
        ArrayList<Rect> rcTexts = new ArrayList<Rect>();
        int nTextRegionCnt = JUtilFunctions.textNormalDetector.do_detect(analyseAreaMat, rcTexts, nTextDetectMode);
        if (nTextRegionCnt > 0) {
            Mat ocrAreaMat = JUtilFunctions.originScreenShot.submat(rcForOcr);

            //.for test.
            JUtilFunctions.SaveMatFile(ocrAreaMat, MyAccessibilityService.mainService);

            for (int k = 0; k < rcTexts.size(); k++){
                Rect rc = rcTexts.get(k);
                JUtilFunctions.changeToOrigRectFromBaseRect(rc);
            }

            result_string = JUtilFunctions.do_ocr(ocrAreaMat, rcTexts, fResizeRate,
                    result_rects, string_param_list, nPreprocessMethodForOcrString);
            //. must do offset operation.
            if (result_string.equals("success")){
                int result_rect_cnt = result_rects.size();
                for (int i = 0; i < result_rect_cnt; i++){
                    //.
                    Rect rc = result_rects.get(i);
                    rc.x += rcForOcr.x;
                    rc.y += rcForOcr.y;
                }
            }

        }

        return result_string;
    }

    public static String findText(String strTarget, Rect rcAnalyseBase, float fResizeRate,
                                  ArrayList<Rect> result_rects, int nPreprocessMethodForOcrString, int nTextDetectMode) {

        String strRet = "fail findText: " + strTarget;
        ArrayList<String> string_param_list = new ArrayList<String>();
        string_param_list.add(strTarget);
        string_param_list.add("0");

        strRet = JUtilFunctions.getTextAreaFromOcr(string_param_list, rcAnalyseBase,
                fResizeRate, result_rects, nPreprocessMethodForOcrString, nTextDetectMode);
        string_param_list = null;

        return strRet;
    }


    public static int getNextBetCategorySection(Rect rcFind) {
        int ret = 0;
        for(int row =(int) rcFind.y + 40; row< Config.Screen_Height; row++){
            double[] pixelColor = screenshot.get(row, 10);
            int redValue =(int) pixelColor[0];
            int greenValue = (int) pixelColor[1];
            int blueValue =(int) pixelColor[2];
            if(redValue == 70 && greenValue== 75 && blueValue == 88){
                return row;
            }
        }
        return ret;
    }
}
