package com.itau.sportsbet.CommandAPI;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
public interface CommandAPIService {
    @POST("requests")
    Call<APIResponse> getCommand(@Body CommandRequest commandRequest);
}
