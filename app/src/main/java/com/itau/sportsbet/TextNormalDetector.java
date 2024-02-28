package com.itau.sportsbet;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.RETR_LIST;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

class CLabelInfo
{

    Point	    m_ptCenter = new Point();
    Rect        m_rcBound = new Rect();
    int		    m_nPtCnts;
    boolean	    m_bRemoved;

    static int MaxCharWidth = 70;
    static int MaxCharHeight = 45;
    static int MaxGapOverlappedDetection = 2;
    static int MinCharHeight = 11;
    static int MinCharWidth = 10;
    static int MaxOffsetCenterY = 8;
    static int MaxOffsetHeight = 15;
    static int  MaxOffsetX = 10;
    public CLabelInfo() {
        m_nPtCnts = 0;
        m_rcBound.x = 10000;
        m_rcBound.y = 10000;
        m_rcBound.width = 0;
        m_rcBound.height = 0;
        m_bRemoved = false;
    }

    public static boolean isIgnore(Mat edges, MatOfPoint ptInfo ){
        boolean bRet = false;

        Rect rcBound =Imgproc.boundingRect(ptInfo);

        if (rcBound.width > CLabelInfo.MaxCharWidth)
            bRet = true;
        else if (rcBound.height > CLabelInfo.MaxCharHeight)
            bRet = true;

        return bRet;
    }

    public void attach(MatOfPoint _ptInfo){

        m_nPtCnts = (int)_ptInfo.elemSize();
        m_rcBound = Imgproc.boundingRect(_ptInfo);
        m_ptCenter.x = m_rcBound.x + m_rcBound.width / 2;
        m_ptCenter.y = m_rcBound.y + m_rcBound.height / 2;

    }
    public boolean isSmallDot()
    {
        boolean bRet = false;
        if (m_rcBound.width < 10 && m_rcBound.height < 10)
        {
            bRet = true;
        }
        return bRet;
    }

    public boolean isOverlaped(CLabelInfo otherlabelInfo){
        boolean bRet = false;

        int nGap = 0;
        //. if one of two is dot,...
        boolean bMyDot = isSmallDot();
        boolean bOtherDot = otherlabelInfo.isSmallDot();
        boolean bDot = (bMyDot == true && bOtherDot == false) || (bMyDot == false && bOtherDot == true);
        if (bDot)
            nGap = CLabelInfo.MaxGapOverlappedDetection;
        // Check for horizontal overlap
        boolean x_overlap = (m_rcBound.x <= otherlabelInfo.m_rcBound.x + otherlabelInfo.m_rcBound.width + nGap) &&
                (otherlabelInfo.m_rcBound.x <= m_rcBound.x + m_rcBound.width + nGap);
        // Check for vertical overlap
        boolean y_overlap = (m_rcBound.y <= otherlabelInfo.m_rcBound.y + otherlabelInfo.m_rcBound.height + nGap) &&
                (otherlabelInfo.m_rcBound.y <= m_rcBound.y + m_rcBound.height + nGap);
        // Return true if both horizontal and vertical overlap exist
        bRet = x_overlap && y_overlap;
        return bRet;
    }

    public void attach(CLabelInfo labelInfo){
        m_nPtCnts += labelInfo.m_nPtCnts;

        int newLeft =(int) JUtilFunctions.min(m_rcBound.x, labelInfo.m_rcBound.x);
        int newTop = (int) JUtilFunctions.min(m_rcBound.y, labelInfo.m_rcBound.y);

        int newRight = (int) JUtilFunctions.max(m_rcBound.x + m_rcBound.width, labelInfo.m_rcBound.x + labelInfo.m_rcBound.width);
        int newBottom = (int) JUtilFunctions.max(m_rcBound.y + m_rcBound.height, labelInfo.m_rcBound.y + labelInfo.m_rcBound.height);

        m_ptCenter.x = (newLeft + newRight) / 2;
        m_ptCenter.y = (newTop + newBottom) / 2;

        m_rcBound.x = newLeft;
        m_rcBound.y = newTop;
        m_rcBound.width = newRight - newLeft;
        m_rcBound.height = newBottom - newTop;
        //.set flags.
        labelInfo.m_bRemoved = true;
    }
    public boolean isNeighbour(CLabelInfo otherlabelInfo){
        boolean bNeighbour = false;
        //. first , fitted center.y gap...
        boolean bCond1 = (Math.abs(m_ptCenter.y - otherlabelInfo.m_ptCenter.y) <= CLabelInfo.MaxOffsetCenterY);
        if (bCond1)
        {
            //. second. limit horizontal gap...
            int right1 = m_rcBound.x + m_rcBound.width;
            int right2 = otherlabelInfo.m_rcBound.x + otherlabelInfo.m_rcBound.width;

            int nNewLeft =(int) JUtilFunctions.min(m_rcBound.x, otherlabelInfo.m_rcBound.x);
            int nNewRight = (int)JUtilFunctions.max(right1, right2);
            int nNewLength = nNewRight - nNewLeft;
            boolean bCond2 = (nNewLength <= (m_rcBound.width + otherlabelInfo.m_rcBound.width + CLabelInfo.MaxOffsetX));
            if (bCond2)
                bNeighbour = true;
        }
        return bNeighbour;
    }
    public CLabelInfo do_clone()
    {
        CLabelInfo pNew = new CLabelInfo();
        pNew.m_ptCenter = m_ptCenter.clone();
        pNew.m_rcBound = m_rcBound.clone();
        pNew.m_nPtCnts = m_nPtCnts;
        return pNew;
    }

    boolean isIgnore_after_merge()
    {
        boolean bRet = false;
        if (m_rcBound.width < 20) {
            bRet = true;
        }
        else if (m_rcBound.height > CLabelInfo.MaxCharHeight || m_rcBound.height < CLabelInfo.MinCharHeight) {
            bRet = true;
        }
        else
        {
            int max_val = Math.max(m_rcBound.height, m_rcBound.width);
            int min_val = Math.min(m_rcBound.height, m_rcBound.width);
            float fAspect = min_val / (float)max_val;
            if (fAspect > 0.8)
                bRet = true;
        }
        if (bRet == false) {
            if (m_rcBound.height > m_rcBound.width) {
                bRet = true;
            }
        }
        return bRet;
    }

};
class CLabels_OneLine extends ArrayList<CLabelInfo>
{
    public int	m_nMidY = 0;
    public void add_labelInfo(CLabelInfo  pInfo){
        if (m_nMidY == 0)
            m_nMidY = (int) pInfo.m_ptCenter.y;
        else
            m_nMidY =(int) (m_nMidY + pInfo.m_ptCenter.y) / 2;

        CLabelInfo pNew = pInfo.do_clone();
        add(pNew);
    }
    public void do_sort(){
        int n = this.size();
        for (int i = 0; i < n - 1; ++i) {
            for (int j = 0; j < n - i - 1; ++j)
            {
                CLabelInfo pJ = this.get(j);
                CLabelInfo pJP1 = this.get(j + 1);
                if (pJ.m_rcBound.x > pJP1.m_rcBound.x) {
                    // Swap pJ and pJP1
                    set(j, pJP1);
                    set(j + 1, pJ);
                }
            }
        }
    }
    Rect do_build(int nStartIdx){
        Rect rcRect = new Rect(0,0,0,0);
        int nSizeLabels = this.size();
        CLabelInfo pMasterInfo = get(nStartIdx);
        CLabelInfo pMainInfo = pMasterInfo.do_clone();
        for (int j = nStartIdx + 1; j < nSizeLabels; j++)
        {
            CLabelInfo pInfo = get(j);
            pMainInfo.attach(pInfo);
        }
        rcRect = pMainInfo.m_rcBound;
        pMainInfo = null;
        return rcRect;
    }
};
class CTotalLabels extends  ArrayList<CLabels_OneLine>
{
    public void do_sort(){
        int n = this.size();
        for (int i = 0; i < n - 1; ++i) {
            for (int j = 0; j < n - i - 1; ++j)
            {
                CLabels_OneLine pJ = get(j);
                CLabels_OneLine pJP1 = get(j + 1);
                if (pJ.m_nMidY > pJP1.m_nMidY) {
                    // Swap pJ and pJP1
                    set(j, pJP1);
                    set(j + 1, pJ);
                }
            }
        }
    }
};


public class TextNormalDetector {
    Context context;
    public TextNormalDetector(Context context){
        this.context = context;
    }
    void do_build_text_region(CTotalLabels total_labels, ArrayList<Rect> outRcArray, int nDetectMode){
        int nLineCnt = (int)total_labels.size();
        for (int i = 0; i < nLineCnt; i++)
        {
            CLabels_OneLine pLineInfo = total_labels.get(i);
            int nLabelCnt = pLineInfo.size();
            Rect rcTxt0 = pLineInfo.do_build(0);
            outRcArray.add(rcTxt0);

            //. 2024-2-27
            //. if nDetectMode == 1, in case of country name, there is image front of name
            if (nLabelCnt >= 2 && nDetectMode == 1)
            {
                Rect rcTxt1 = pLineInfo.do_build(1);
                outRcArray.add(rcTxt1);
            }
        }
    }
    public  int do_detect(Mat _image, ArrayList<Rect> outRcArray, int nDetectMode){
        int nRet = 0;
        // do canny.
        Mat edges = new Mat();
        double threshold1 = 30;
        double threshold2 = 100;
        int apertureSize = 3;
        Imgproc.Canny(_image, edges, threshold1, threshold2, apertureSize);

        Mat image = edges.clone();

        // Find contours in the edge image
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, RETR_LIST, CHAIN_APPROX_NONE);

        ArrayList<CLabelInfo>	labelInfos = new ArrayList<CLabelInfo>();
        int nSizeCont = (int)contours.size();
        for (int i = 0; i < nSizeCont; i++)
        {
            boolean bIngore = CLabelInfo.isIgnore(edges, contours.get(i));
            if (bIngore == false)
            {
                CLabelInfo pInfo = new CLabelInfo();
                pInfo.attach(contours.get(i));
                labelInfos.add(pInfo);
            }

        }
        ///////////////////////////////////step-2///////////////////////////////
        int nSizeLabels = labelInfos.size();
        Log.d("nSizeLabels", "" +  nSizeLabels);
        //. first merge.
        for (int i = 0; i < nSizeLabels; i++)
        {
            if (labelInfos.get(i).m_bRemoved)
                continue;
            R_LOOP:  while(true){
                for (int j = 0; j < nSizeLabels; j++)
                {
                    if (i == j)
                        continue;
                    if (labelInfos.get(j).m_bRemoved)
                        continue;

                    if (labelInfos.get(i).isOverlaped(labelInfos.get(j)))
                    {
                        labelInfos.get(i).attach(labelInfos.get(j));
                        continue R_LOOP;
                    }
                }
                break;
            }
        }

        //==========================================================================
        //. step-3. merge Neighbour hood labels to one large label.
        //. and keep(remember) their topology...
        CTotalLabels total_labels = new CTotalLabels();

        for (int i = 0; i < nSizeLabels; i++)
        {
            CLabelInfo pInfo = labelInfos.get(i);
            if (pInfo.m_bRemoved)
                continue;
            CLabels_OneLine pOneLine = new CLabels_OneLine();
            pOneLine.add_labelInfo(pInfo);

            R_LOOP1:   while(true){
                for (int j = 0; j < nSizeLabels; j++)
                {
                    if (i == j)
                        continue;
                    CLabelInfo pInfo1 = labelInfos.get(j);
                    if (pInfo1.m_bRemoved)
                        continue;
                    if (pInfo.isNeighbour(pInfo1))
                    {
                        pInfo.attach(pInfo1);
                        pOneLine.add_labelInfo(pInfo1);
                        continue R_LOOP1;
                    }
                }
                break;
            }
            //. final check.
            if (pInfo.isIgnore_after_merge())
            {
                pInfo.m_bRemoved = true;
                pOneLine = null;
            }
            else
            {
                pOneLine.do_sort();
                total_labels.add(pOneLine);
            }

        }
        total_labels.do_sort();

        //==========================================================================
        //. step-4. finally, build text regions...
        //. decide text_region per block.
        //. and do some decides...
        do_build_text_region(total_labels, outRcArray, nDetectMode);

        //pgh. for test.
        //. draw rect into text bound.
        nSizeLabels = outRcArray.size();
        for (int i = 0; i < nSizeLabels; i++)
        {
            Rect rect = outRcArray.get(i);
            Imgproc.rectangle(_image, rect, new Scalar(255, 0, 0, 255), 1);
        }
        JUtilFunctions.SaveMatFile(_image, context );

        nRet = outRcArray.size();
        return nRet;
    }

}
