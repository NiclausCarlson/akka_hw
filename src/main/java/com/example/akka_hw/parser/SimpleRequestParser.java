package com.example.akka_hw.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SimpleRequestParser {
    public static List<Predicate<String>> parse(final String request) {
        var strings = request.split("\\s+");
        List<Predicate<String>> result = new ArrayList<>();
        for (var str : strings) {
            result.add(s -> s.contains(str));
        }
        return result;
    }
}
