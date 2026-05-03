package com.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthorRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String nationality;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
}
