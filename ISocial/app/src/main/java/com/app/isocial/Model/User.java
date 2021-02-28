package com.app.isocial.Model;

public class User {

    String id;
    String name;
    String bio;
    String imageurl;
    String username;


    public User(String id, String name, String imageurl, String bio, String username) {
        this.id = id;
        this.name = name;
        this.imageurl = imageurl;
        this.bio = bio;
        this.username = username;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
