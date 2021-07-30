package com.app.autismplay.helper;

public class ConfigurationsConstants {

    public static String NODE_OBJECTS = "Objects";
    public static String NODE_ANIMALS="Animals";
    public static String NODE_USERS = "Users";
    public static String NODE_PREFERENCES = "Preferences";
    public static String NODE_RECOMENDATIONS = "Recomendations";

    public static String getNodePreferencesOptionsRandow(){
         if(Math.round( Math.random() )==1){
             return NODE_OBJECTS;
         }
         return NODE_ANIMALS;

    }


}
