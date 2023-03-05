package com.example.akka_hw.stub_server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ResponseGenerator {
    private static final String kAllWords = "/words.txt";
    private static final int kResultCount = 100;
    private final List<Predicate<String>> filters;

    public ResponseGenerator(final List<Predicate<String>> filters) {
        this.filters = filters;
    }

    public List<String> generate() {
        List<String> result = new ArrayList<>();
        var path = this.getClass().getResource(kAllWords);
        try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
            String line;
            while ((line = br.readLine()) != null && result.size() < kResultCount) {
                if (checkFilters(line)) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private boolean checkFilters(final String str) {
        if (filters.isEmpty()) {
            return true;
        }
        for (var filter : filters) {
            if (filter.test(str)) {
                return true;
            }
        }
        return false;
    }

}
