package com.example.akka_hw.stub_server;

import java.util.List;

public class Response {
    private final String searcherName;
    private final List<String> response;

    public Response(final String name, final List<String> response) {
        this.searcherName = name;
        this.response = response;
    }

    public String getSearcherName() {
        return searcherName;
    }

    public List<String> getResponse() {
        return response;
    }
}
