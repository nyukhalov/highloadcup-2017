package com.github.nyukhalov.highloadcup.core.domain;

import com.jsoniter.JsonIterator;

public class LocationUpdateJ {

    public boolean isValid() {
        return true;
    }

    public static LocationUpdateJ fromJson(String json) {
        return JsonIterator.deserialize(json, LocationUpdateJ.class);
    }
}
