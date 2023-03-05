package com.example.akka_hw.stub_server;

import java.util.List;
import java.util.function.Predicate;

public class StubServerBase implements StubServerInterface {
    private final String kName;
    private final ResponseGenerator generator;

    private final int delay;

    public StubServerBase(final String kName, final List<Predicate<String>> predicates, final int delay) {
        this.delay = delay;
        this.kName = kName;
        this.generator = new ResponseGenerator(predicates);
    }

    @Override
    public Response get() throws InterruptedException {
        Thread.sleep(delay);
        return new Response(kName, this.generator.generate());
    }
}
