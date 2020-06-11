package ru.hse.cs.java2020.task03.utils;

public final class Utils {
    private Utils() {
    }

    public static String bold(String text) {
        return "<b>" + text + "</b>";
    }

    public static String italic(String text) {
        return "<i>" + text + "</i>";
    }

    public static String underline(String text) {
        return "<u>" + text + "</u>";
    }

    public static String strikethrough(String text) {
        return "<s>" + text + "</s>";
    }

    public static String ahref(String text, String link) {
        return "<a href=\"" + link + "\">" + text + "</a>";
    }

    public static String code(String text) {
        return "<code>" + text + "</code>";
    }
}
