package com.example.tidalapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TideApiService {
    @GET("weatherAPI/opendata/opendata.php")
    Call<TideResponse> getTideLevels(
            @Query("dataType") String dataType,
            @Query("lang") String lang,
            @Query("rformat") String rformat,
            @Query("station") String station,
            @Query("year") int year,
            @Query("month") int month,
            @Query("day") int day,
            @Query("hour") int hour);
}