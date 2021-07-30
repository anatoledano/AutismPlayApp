package com.app.autismplay.services;

import com.app.autismplay.responseyoutube.Result;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeService {

    /*
    search
    ?part=snippet
    &relatedToVideoId=[VIDEOID]
    &maxResults=20
    &key=[KEY]
    &channelId=[CHANNEL]
     */

    @GET("search")
    Call<Result> recurperarVideos(
            @Query("part") String part,
            @Query("order") String order,
            @Query("maxResults") String maxResults,
            @Query("key") String key,
            @Query("channelId") String channelId,
            @Query("q") String q,
            @Query("safeSearch") String safeSearch,
            @Query("regionCode") String regionCode
            );

    @GET("search")
    Call<Result> recurperarVideosrRelacionados(
            @Query("part") String part,
            @Query("order") String order,
            @Query("maxResults") String maxResults,
            @Query("key") String key,
            @Query("safeSearch") String safeSearch,
            @Query("regionCode") String regionCode,
            @Query("relatedToVideoId") String videoRelationId,
            @Query("type") String type
    );


}
