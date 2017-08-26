package com.github.nyukhalov.highloadcup.core.domain;

import com.jsoniter.JsonIterator;

import java.util.ArrayList;
import java.util.List;

public class LocationsJ {
    public List<LocationJ> locations = new ArrayList<>();

    public static LocationsJ fromJson(String json) {
        return JsonIterator.deserialize(json, LocationsJ.class);
    }
}
