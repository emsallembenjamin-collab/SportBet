package com.itau.sportsbet;

abstract public class JBetAction {

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
            default:
                break;
        }

        return pRet;
    }
}
