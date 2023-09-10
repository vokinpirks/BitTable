package com.vokinpirks.util;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.util.Map;

@UtilityClass
public class Env {

    private static final String Path = "Path";

    public static void appendPath(final Map<String, String> env, final String part) {
        final String varName;
        if (env.containsKey(Path)) {
            varName = Path;
            env.put(Path, append(env.get(Path), part));
        } else if (env.containsKey(Path.toUpperCase())) {
            varName = Path.toUpperCase();
        } else if (env.containsKey(Path.toLowerCase())) {
            varName = Path.toLowerCase();
        } else {
            return;
        }

        env.put(varName, append(env.get(varName), part));
    }

    private static String append(final String olvValue, final String part) {
        return part + File.pathSeparator + olvValue;
    }
}
