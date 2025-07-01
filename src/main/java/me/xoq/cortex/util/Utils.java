package me.xoq.cortex.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {
    public static String nameToTitle(String name) {
        name = name.replace("-", " ");

        return Arrays.stream(name.split("[^A-Za-z0-9]+"))
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    String lower = s.toLowerCase();
                    return Character.toUpperCase(lower.charAt(0))
                            + lower.substring(1);
                })
                .collect(Collectors.joining(" "));
    }
}
