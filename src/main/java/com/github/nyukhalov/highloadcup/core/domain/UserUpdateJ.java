package com.github.nyukhalov.highloadcup.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize()
public class UserUpdateJ {
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

    public boolean allFieldsNull() {
        return  email == null &&
                firstName == null &&
                lastName == null &&
                gender == null &&
                birthDate == null;
    }
}
