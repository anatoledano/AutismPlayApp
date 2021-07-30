package com.app.autismplay.helper;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfigurationRetrofit {

    public static Retrofit getRetrofit(){
        return new Retrofit.Builder()
                .baseUrl(ConfigurationYoutube.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
