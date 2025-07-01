package me.xoq.cortex.util;

import net.minecraft.client.util.InputUtil;

import java.util.Arrays;
import java.util.Locale;
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

    public static String keyToString(int key) {
        if (key < 0) {
            return "Unbound";
        }

        InputUtil.Key inputKey = InputUtil.fromKeyCode(key, 0);
        if (inputKey != InputUtil.UNKNOWN_KEY) {
            return prettifyKeyName(inputKey.getLocalizedText().getString());
        }

        return "Unknown";
    }

    /**
     * Try to turn something like "key.keyboard.left_shift" or "GLFW_KEY_SPACE"
     * into "Left Shift" or "Space".
     */
    private static String prettifyKeyName(String raw) {
        // Strip off any namespace or dots
        String simple = raw.contains(".")
                ? raw.substring(raw.lastIndexOf('.') + 1)
                : raw;

        simple = simple.replace('_', ' ');

        return Arrays.stream(simple.split(" "))
                .map(s -> {
                    String lower = s.toLowerCase(Locale.ROOT);
                    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
                })
                .collect(Collectors.joining(" "));
    }
}
