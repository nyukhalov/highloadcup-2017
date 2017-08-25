package com.github.nyukhalov.highloadcup.core.domain;

public class V {
    public static void requireNotNull(Object o) {
        if (o == null) throw new RuntimeException();
    }
}
