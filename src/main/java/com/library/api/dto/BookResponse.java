package com.library.api.dto;

public class BookResponse {

    private Long id;
    private String title;
    private String genre;
    private AuthorResponse author;

    public BookResponse(Long id, String title, String genre, AuthorResponse author) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public AuthorResponse getAuthor() { return author; }
}
