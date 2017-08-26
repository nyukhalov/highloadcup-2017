package com.github.nyukhalov.highloadcup.core.domain;


import com.jsoniter.JsonIterator;
import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;

import java.util.List;

public class UserJ {
    public Integer id;

    public String email;

    public String firstName;

    public String lastName;

    public String gender;

    public Long birthDate;

    @JsonCreator
    public UserJ(
            @JsonProperty(value = "id", required = true) Integer id,
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "first_name", required = true) String firstName,
            @JsonProperty(value = "last_name", required = true) String lastName,
            @JsonProperty(value = "gender", required = true) String gender,
            @JsonProperty(value = "birth_date", required = true) Long birthDate
    ) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public boolean hasNullField() {
        return id == null ||
                email == null ||
        firstName == null ||
        lastName == null ||
        gender == null ||
        birthDate == null;
    }

    public boolean isValid() {
        return true;
    }

    public static UserJ fromJson(String json) {
        return JsonIterator.deserialize(json, UserJ.class);
    }
}
