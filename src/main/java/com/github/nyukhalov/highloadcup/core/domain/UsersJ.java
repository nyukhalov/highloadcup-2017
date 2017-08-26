package com.github.nyukhalov.highloadcup.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jsoniter.JsonIterator;

import java.util.ArrayList;
import java.util.List;

public class UsersJ {
    @JsonProperty("users")
    public List<UserJ> users = new ArrayList<>();

    public static UsersJ fromJson(String json) {
        return JsonIterator.deserialize(json, UsersJ.class);
    }
}
