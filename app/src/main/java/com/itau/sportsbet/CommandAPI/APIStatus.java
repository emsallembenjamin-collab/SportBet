package com.itau.sportsbet.CommandAPI;

import com.itau.sportsbet.CommandAPI.APIResponse;

public class APIStatus {
    private boolean isFinished = false;
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
