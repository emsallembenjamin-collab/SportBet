package com.itau.sportsbet.CommandAPI;

import android.util.Log;

import com.itau.sportsbet.Assets;
import com.itau.sportsbet.Config;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommandAPI {
    private final String apiUrl = "http://192.168.8.195:3000/api/phoneSetting/";
    private Retrofit retrofit;

    public CommandAPI() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(logging); // Add logging interceptor

        OkHttpClient client = httpClientBuilder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(apiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public String callAPI( APIStatus status ) {

        CommandAPIService commandAPIService = retrofit.create(CommandAPIService.class);
        CommandRequest commandRequest = new CommandRequest(Config.phoneId);
        Call<APIResponse> call = commandAPIService.getCommand(commandRequest);

        call.enqueue(new Callback<APIResponse>() {
            @Override
            public void onResponse(Call<APIResponse> call, Response<APIResponse> response) {

                if(response.isSuccessful() == false){
                    return;
                }
                APIResponse apiResponse = response.body();
                boolean api_success = true;
                if(apiResponse.commandType.equals(APIResponse.CMD_CONFIG)){
                    if(apiResponse.data != null && apiResponse.actionScenario != null){
                        Assets.save_betconfig_json_from_file(apiResponse.data);
                        Assets.save_action_scenario_from_file(apiResponse.actionScenario);
                    }else
                        api_success = false;
                }else if(apiResponse.commandType.equals(APIResponse.CMD_GAME)){
                    if(apiResponse.data != null){
                        Assets.save_bettask_json_from_file(apiResponse.data);
                        status.jEngine.startBetting();
                    }else {
                        api_success = false;
                    }
                }
                if(api_success != true){
                    // Setting config and command failed
                    // Send report
                }
            }
            @Override
            public void onFailure(Call<APIResponse> call, Throwable t) {
                status.setFinished();
            }
        });
        return "";
    }

    public String readStream(InputStream in){

        String result = "";




        return result;
    }

}
