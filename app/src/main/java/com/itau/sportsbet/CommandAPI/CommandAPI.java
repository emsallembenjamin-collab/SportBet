package com.itau.sportsbet.CommandAPI;

import android.util.Log;

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
    private final String apiUrl = "http://192.168.8.196:3000/";
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
                Log.d("API CAL", response.body().toString() );
                APIResponse apiResponse = response.body();
                status.setResonse(apiResponse);
                status.setFinished();
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
