package com.example.akka_hw.stub_server;

import java.util.List;
import java.util.function.Predicate;

public class Bing extends StubServerBase {
    private static final String kName = "Yandex";

    public Bing(List<Predicate<String>> predicates, final int delay) {
        super(kName, predicates, delay);
    }

}