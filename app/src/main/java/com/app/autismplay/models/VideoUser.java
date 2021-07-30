package com.app.autismplay.models;

import java.util.ArrayList;

public class VideoUser {

    private String id;
    private String videoId;
    private int rating;
    private int timeView;
    private int numberViewsByUser;
    private Long resultRelevance;


    public VideoUser() {
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getTimeView() {
        return timeView;
    }

    public void setTimeView(int timeView) {
        this.timeView = timeView;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumberViewsByUser() {
        return numberViewsByUser;
    }

    public void setNumberViewsByUser(int numberViewsByUser) {
        this.numberViewsByUser = numberViewsByUser;
    }

    public Long getResultRelevance() {
        return resultRelevance;
    }

    public void setResultRelevance() {
        this.resultRelevance = (long)timeView*numberViewsByUser*rating;
    }
}
