package com.app.autismplay.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {

    private String id;
    private String nameKid;
    private String nameParent;
    private int age;
    private String sex;
    private String email;

    public User(){

    }

    public User(String id, String nameKid, String nameParent, int age, String sex, String email) {
        this.id = id;
        this.nameKid = nameKid;
        this.nameParent = nameParent;
        this.age = age;
        this.sex = sex;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameKid() {
        return nameKid;
    }

    public void setNameKid(String nameKid) {
        this.nameKid = nameKid;
    }

    public String getNameParent() {
        return nameParent;
    }

    public void setNameParent(String nameParent) {
        this.nameParent = nameParent;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        /*
        result.put("id",this.id);
        result.put("nameKid", this.nameKid);
        result.put("nameParent", this.nameParent);
        result.put("age",this.age);
        result.put("sex",this.sex);
        result.put("email",this.email);
        */
        return result;
    }
}
