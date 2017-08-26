package com.github.nyukhalov.highloadcup.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jsoniter.JsonIterator;

public class VisitJ {
    @JsonProperty("id")
    public Integer id;

    @JsonProperty("location")
    public Integer location;

    @JsonProperty("user")
    public Integer user;

    @JsonProperty("visited_at")
    public Long visitedAt;

    @JsonProperty("mark")
    public Integer mark;

    public boolean hasNullField() {
        return id == null || location == null || user == null || visitedAt == null || mark == null;
    }

    public boolean isValid() {
        return true;
    }

    public static VisitJ fromJson(String json) {
        return JsonIterator.deserialize(json, VisitJ.class);
    }
}
