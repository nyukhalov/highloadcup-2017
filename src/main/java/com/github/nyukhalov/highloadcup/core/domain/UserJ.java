package com.github.nyukhalov.highloadcup.core.domain;


import com.fasterxml.jackson.annotation.JsonProperty;

public class UserJ {
    @JsonProperty("id")
    public Integer id;

    @JsonProperty("email")
    public String email;

    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("gender")
    public String gender;

    @JsonProperty("birth_date")
    public Long birthDate;

    public boolean hasNullField() {
        return id == null ||
                email == null ||
        firstName == null ||
        lastName == null ||
        gender == null ||
        birthDate == null;
    }
}
