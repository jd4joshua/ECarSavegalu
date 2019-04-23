package com.project.ecs.ecarsavegalu;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {
    //String BASE_URL = "https://book.olacabs.com";

    @GET("/")
    Call<ResponseBody> FindRide(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("category") String category,
            @Query("utm_source") int utm,
            @Query("drop_lat") double drop_lat,
            @Query("drop_lng") double drop_lng,
            @Query("dsw") String dsw
    );
}
