package com.example.weiverbook;

public class Review {
    private int id;
    private String reviewText;
    private float rating;
    private String username;

    public Review(int id, String reviewText, float rating) {
        this.id = id;
        this.reviewText = reviewText;
        this.rating = rating;
    }

    public int getId() { return id; }
    public String getReviewText() { return reviewText; }
    public float getRating() { return rating; }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}