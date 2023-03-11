package com.example.akka_hw.stub_server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Yandex extends StubServerBase {
    private static final String kName = "Yandex";

    public Yandex(List<Predicate<String>> predicates, final int delay) {
        super(kName, updatePredicates(predicates), delay);
    }

    private static List<Predicate<String>> updatePredicates(List<Predicate<String>> predicates) {
        List<Predicate<String>> inner = new ArrayList<>();
        for (var it : predicates) {
            inner.add(it.and(s -> s.startsWith("Y") || s.startsWith("y") || s.length() >= 5));
        }
        return inner;
    }

}
