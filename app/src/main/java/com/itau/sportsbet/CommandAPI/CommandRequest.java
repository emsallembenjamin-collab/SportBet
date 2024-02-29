package com.itau.sportsbet.CommandAPI;

public class CommandRequest {
    private String devId;

    public CommandRequest(String devId) {
        this.devId = devId;
    }

    // Getter and Setter
    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }
}