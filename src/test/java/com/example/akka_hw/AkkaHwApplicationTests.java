package com.example.akka_hw;

import com.example.akka_hw.actors.Supervisor;
import com.example.akka_hw.parser.SimpleRequestParser;
import com.example.akka_hw.stub_server.Response;
import com.example.akka_hw.stub_server.ResponseGenerator;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AkkaHwApplicationTests {
    @Before
    public void init() {
        ResponseGenerator init = new ResponseGenerator(Collections.emptyList());
    }

    @Test
    public void SimpleTest() {
        String request = "a";
        Supervisor supervisor = new Supervisor(request, 0, 0, 0);
        var responses = supervisor.get();
        assertEquals(3, responses.size());
        assertTrue(CheckResponse(responses, request));
    }

    @Test
    public void WordTest() {
        String request = "dog";
        Supervisor supervisor = new Supervisor(request, 0, 0, 0);
        var responses = supervisor.get();
        assertEquals(3, responses.size());
        assertTrue(CheckResponse(responses, request));
    }

    @Test
    public void WordsTest() {
        String request = "the dog";
        Supervisor supervisor = new Supervisor(request, 0, 0, 0);
        var responses = supervisor.get();
        assertEquals(3, responses.size());
        assertTrue(CheckResponse(responses, request));
    }

    @Test
    public void Timeout(){
        String request = "beware of the dog";
        Supervisor supervisor = new Supervisor(request, 400, 0, 0);
        var responses = supervisor.get();
        assertEquals(2, responses.size());

        Supervisor supervisor1 = new Supervisor(request, 400, 400, 0);
        var responses1 = supervisor1.get();
        assertEquals(1, responses1.size());

        Supervisor supervisor2 = new Supervisor(request, 400, 400, 400);
        var responses2 = supervisor2.get();
        assertEquals(0, responses2.size());
    }
    private boolean CheckResponse(final List<Response> resp, final String request) {
        var predicates = SimpleRequestParser.parse(request);
        for (var it : resp) {
            for (var resp_it : it.getResponse(50)) {
                boolean res = false;
                for (var pred : predicates) {
                    res |= pred.test(resp_it);
                }
                if (!res) {
                    System.err.printf("Test failed for searcher %s and word %s",
                            it.getSearcherName(), resp_it);
                    return false;
                }
            }
        }
        return true;
    }
}
