package com.example.akka_hw.stub_server;

import java.util.ArrayList;
import java.util.List;

public class Response {
    private final String searcherName;
    private final List<String> response;

    public Response() {
        this.searcherName = "";
        this.response = new ArrayList<>();
    }

    public Response(final String name, final List<String> response) {
        this.searcherName = name;
        this.response = response;
    }

    public String getSearcherName() {
        return searcherName;
    }

    public List<String> getResponse(int elemsCount) {
        return response.subList(0, Math.min(elemsCount + 1, response.size()));
    }
}
