package com.example.akka_hw.stub_server;

import java.util.List;
import java.util.function.Predicate;

public class Google extends StubServerBase {
    private static final String kName = "Yandex";

    public Google(List<Predicate<String>> predicates, final int delay) {
        super(kName, predicates, delay);
    }

}