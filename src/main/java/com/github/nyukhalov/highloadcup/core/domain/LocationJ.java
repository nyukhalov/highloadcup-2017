package com.github.nyukhalov.highloadcup.core.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationJ {
    @JsonProperty("id")
    public Integer id;

    @JsonProperty("place")
    public String place;

    @JsonProperty("country")
    public String country;

    @JsonProperty("city")
    public String city;

    @JsonProperty("distance")
    public Integer distance;

    public boolean hasNullFields() {
        return id == null || place == null || country == null || city == null || distance == null;
    }
}
