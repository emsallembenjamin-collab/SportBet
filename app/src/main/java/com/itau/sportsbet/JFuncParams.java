package com.itau.sportsbet;


import static com.itau.sportsbet.Config.StrCompMethod.e_PermitIncluding;

import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Rect;

class JFuncParams_ColorBar {
    public Point3 targetUpColor = null;
    public Point3 targetDownColor = null;
    public int nLimitLen = 0;
    public int fixedVal = 0;
    public int startVal = 0;
    public int endVal = 0;

    public boolean bVert = true;

};

class JFuncParams_FindSectionIncluding2Targets {
    public String sectionTarget = null;
    public Config.StrCompMethod eSecTargetComMethod = e_PermitIncluding;
    //. 2024-3-9
    //. for find League section.
    //. in some case (SABA_Gr)... league and bet body's color is same...
    //. so we detect border(background color) and guess League section...
    int    secWidthforUsingBorder = 0;

    public String target1 = null;
    public Config.StrCompMethod  strCompMethod1;
    public Config.StrPreprocessMethod  strPreprocessMethod1;
    public String target2 = null;
    public Config.StrCompMethod  strCompMethod2;
    public Config.StrPreprocessMethod  strPreprocessMethod2;
    public int tryScrollCnt = 0;
    public int nAnalyseWidth = 0;

    public Config.NeighborCond2Targets neighborCond2Targets;
    public boolean bFindedLeagueSection = false;

    public JFuncParams_ColorBar nextSectionInfo = null;
    //.2024-3-8
    Point3  ptBetPannelBackUpColor = null;
    Point3  ptBetPannelBackDownColor = null;
    Rect    rcDecideforCollapseCond = null;
    Point   ptPosClickforExpanding = null;

};




public class JFuncParams {
}
