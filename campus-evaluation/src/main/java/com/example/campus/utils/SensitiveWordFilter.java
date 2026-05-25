package com.example.campus.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SensitiveWordFilter {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>();

    @PostConstruct
    public void init() {
        SENSITIVE_WORDS.add("广告");
        SENSITIVE_WORDS.add("加微信");
        SENSITIVE_WORDS.add("qq群");
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        for (String word : SENSITIVE_WORDS) {
            if (result.contains(word)) {
                result = result.replace(word, "***");
            }
        }
        return result;
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String word : SENSITIVE_WORDS) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
