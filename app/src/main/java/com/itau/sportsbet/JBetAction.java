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
                        pRet = new JBetAction_Euro1(loadTask);
                    }
                    break;
                    case 1: {
                        pRet = new JBetAction_Peak1(loadTask);
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
