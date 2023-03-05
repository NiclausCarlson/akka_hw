package com.example.akka_hw.stub_server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Yandex extends StubServerBase {
    private static final String kName = "Yandex";

    public Yandex(List<Predicate<String>> predicates, final int delay) {
        super(kName, predicates, delay);
    }

}
