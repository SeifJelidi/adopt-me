package tn.rabini.petadoption.models;

import java.util.HashMap;

public class User {
    private String username, email, phone, picture;
    private HashMap<String, String> pets;
    private HashMap<String, String> likedPets;

    public User() {}

    public User(String username, String email, String phone, String picture) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.picture = picture;
        this.pets = new HashMap<>();
        this.likedPets = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPicture() {
        return picture;
    }

    public HashMap<String, String> getPets() {
        return pets;
    }

    public HashMap<String, String> getLikedPets() {
        return likedPets;
    }

    public void setLikedPets(HashMap<String, String> likedPets) {
        this.likedPets = likedPets;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", picture='" + picture + '\'' +
                ", pets=" + pets +
                ", likedPets=" + likedPets +
                '}';
    }
}
