package com.sns.backend.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HashtagParser {
    // 한글/영문/숫자/_ 허용, 길이 1~50
    private static final Pattern P = Pattern.compile("#([0-9A-Za-z가-힣_]{1,50})");

    public Set<String> extract(String text) {
        if (text == null) return Set.of();
        Matcher m = P.matcher(text);
        Set<String> out = new LinkedHashSet<>();
        while (m.find()) out.add(m.group(1).toLowerCase());
        return out;
    }

    public List<String> normalize(List<String> raw) {
        if (raw == null) return List.of();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(s -> s.replaceAll("^#", "").trim().toLowerCase())
                .filter(s -> !s.isBlank() && s.length() <= 50)
                .distinct()
                .toList();
    }
}
