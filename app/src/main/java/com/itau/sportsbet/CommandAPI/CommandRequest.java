package com.itau.sportsbet.CommandAPI;

public class CommandRequest {

    public static String RequestCommand = "COMMAND";
    public static String RequestReport = "REPORT";
    public static String CommandSuccess = "Success";
    public static String CommandFailed = "Failed";
    private String phone_id;
    private String request_type = CommandRequest.RequestCommand;
    private String data= null;
    private String is_success= null;
    public CommandRequest(String devId) {
        this.phone_id = devId;
    }
    // Getter and Setter
    public String getDevId() {
        return phone_id;
    }
    public void setRequest_type (String request_type) {
        this.request_type = request_type;
    }
    public void setDevId(String devId) {
        this.phone_id = devId;
    }
    public void setData(String data){
        this.data= data;
    }
    public void setSuccess(String success){
        this.is_success= success;
    }

}