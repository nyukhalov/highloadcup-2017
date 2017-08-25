package com.github.nyukhalov.highloadcup.core.domain;

import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.annotation.JsonWrapper;

import static com.github.nyukhalov.highloadcup.core.domain.V.requireNotNull;

public class UserUpdateJ {

    public String email;

    public String firstName;

    public String lastName;

    public String gender;

    public Long birthDate;

    public void setEmail(String email) {
        requireNotNull(email);
        this.email = email;
    }

    public void setFirst_name(String firstName) {
        requireNotNull(firstName);
        this.firstName = firstName;
    }

    public void setLast_name(String lastName) {
        requireNotNull(lastName);
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        requireNotNull(gender);
        this.gender = gender;
    }

    public void setBirth_date(Long birthDate) {
        requireNotNull(birthDate);
        this.birthDate = birthDate;
    }


    @JsonIgnore
    public boolean allFieldsNull() {
        return  email == null &&
                firstName == null &&
                lastName == null &&
                gender == null &&
                birthDate == null;
    }
}
