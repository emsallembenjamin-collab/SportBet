package com.itau.sportsbet;

import org.opencv.core.Rect;

import java.util.ArrayList;

abstract public class JBetAction {

    public JLoadTask loadTask = null;
    ArrayList<Rect> result_rects = new ArrayList<Rect>();
    float fResizeRate = 1.0f;

    //. Abstract method
    abstract String run();

    public static JBetAction createObject(JLoadTask loadTask){
        JBetAction pRet = null;
        switch(loadTask.site){
            case "oxbet.in":{
                switch (loadTask.category){
                    case 0:{
                        pRet = new JBetAction_ESports_prod20091_bti(loadTask);
                    }
                    break;
                    case 1: {
                        pRet = new JBetAction_KSports_m_zenandfe(loadTask);
                    }
                    break;
                    default:
                    break;

                }
            }
            break;
            case "nn88111.com":{
                switch (loadTask.category){
                    case 0:{
                        pRet = new JBetAction_SABA_Gr(loadTask);
                    }
                    break;
                    default:
                        break;

                }
            }
            break;
            default:
                break;
        }

        return pRet;
    }
}
