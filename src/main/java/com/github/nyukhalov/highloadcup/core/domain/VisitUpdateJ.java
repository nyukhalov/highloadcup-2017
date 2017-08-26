package com.github.nyukhalov.highloadcup.core.domain;

import com.jsoniter.JsonIterator;

public class VisitUpdateJ {

    public boolean isValid() {
        return true;
    }

    public static VisitUpdateJ fromJson(String json) {
        return JsonIterator.deserialize(json, VisitUpdateJ.class);
    }
}
