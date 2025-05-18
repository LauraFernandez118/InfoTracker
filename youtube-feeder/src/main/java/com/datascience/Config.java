package com.datascience;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private final Map<String, String> params;

    public Config(String[] args) {
        this.params = parseArgs(args);
    }

    private Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.stream(args)
                .filter(arg -> arg.startsWith("--"))
                .forEach(arg -> {
                    String[] keyValue = arg.substring(2).split("=");
                    params.put(keyValue[0], keyValue[1]);
                });
        return params;
    }


}