package com.library.api.dto;

public class AuthorResponse {

    private Long id;
    private String name;
    private String nationality;

    public AuthorResponse(Long id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
}
