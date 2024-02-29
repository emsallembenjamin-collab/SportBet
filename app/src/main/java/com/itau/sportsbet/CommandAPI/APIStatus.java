package com.itau.sportsbet.CommandAPI;

import com.itau.sportsbet.CommandAPI.APIResponse;
import com.itau.sportsbet.JEngine;

public class APIStatus {
    private boolean isFinished = false;
    public JEngine jEngine = null;
    private APIResponse apiResponse ;
    public void setFinished() {
        this.isFinished = true;
    }
    public boolean getFinished() {
        return this.isFinished;
    }
    public void setResonse(APIResponse response){
        this.apiResponse = response;
    }

    public APIResponse getApiResponse() {
        return this.apiResponse;
    }
    public void reset() {
        isFinished = false;
        apiResponse = null;
    }

}
