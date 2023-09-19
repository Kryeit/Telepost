package com.kryeit.telepost;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }
}
