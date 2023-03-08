package com.example.akka_hw.actors;

import com.example.akka_hw.stub_server.StubServerBase;

import java.util.List;
import java.util.function.Predicate;

public class Messages {
    public enum StubServerType {
        GOOGLE,
        YANDEX,
        BING
    }

    public static class StartMsg {
        public StubServerType stubType;
        public List<Predicate<String>> predicates;

        public int delay = 0;

        public StartMsg(final StubServerType type, final List<Predicate<String>> predicates, final int delay) {
            this.stubType = type;
            this.predicates = predicates;
            this.delay = delay;
        }
    }
}
