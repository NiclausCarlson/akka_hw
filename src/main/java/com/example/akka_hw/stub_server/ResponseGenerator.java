package com.example.akka_hw.stub_server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ResponseGenerator {
    private static final String kAllWords = "/words.txt";
    private static final int kResultCount = 100;
    private static final List<String> words = readWords();
    private final List<Predicate<String>> filters;

    public ResponseGenerator(final List<Predicate<String>> filters) {
        this.filters = filters;
    }

    public List<String> generate() {
        List<String> result = new ArrayList<>();
        for (var it : words) {
            if (result.size() >= kResultCount) {
                break;
            }
            if (checkFilters(it)) {
                result.add(it);
            }
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

    private static List<String> readWords() {
        List<String> result = new ArrayList<>();
        try (InputStream is = ResponseGenerator.class.getResourceAsStream(kAllWords);
             InputStreamReader isr = new InputStreamReader(is,
                     StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
