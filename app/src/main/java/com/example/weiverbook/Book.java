package com.example.weiverbook;

public class Book {
    private int id;
    private String title;
    private String author;       // ✅ Tambah field author
    private String synopsis;
    private String imageName;
    private float averageRating;

    // ✅ Constructor lengkap dengan author
    public Book(int id, String title, String author, String synopsis, String imageName, float averageRating) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.synopsis = synopsis;
        this.imageName = imageName;
        this.averageRating = averageRating;
    }

    // ✅ Getter
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }      // ✅ Getter author
    public String getSynopsis() { return synopsis; }
    public String getImageName() { return imageName; }
    public float getAverageRating() { return averageRating; }
}
