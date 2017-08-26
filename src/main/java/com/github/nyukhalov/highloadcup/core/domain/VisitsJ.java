package com.github.nyukhalov.highloadcup.core.domain;

import com.jsoniter.JsonIterator;

import java.util.ArrayList;
import java.util.List;

public class VisitsJ {
    public List<VisitJ> visits = new ArrayList<>();

    public static VisitsJ fromJson(String json) {
        return JsonIterator.deserialize(json, VisitsJ.class);
    }
}
