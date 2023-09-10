package com.vokinpirks.util;

import lombok.experimental.UtilityClass;

import java.util.Locale;

// https://stackoverflow.com/a/17506150
@UtilityClass
public class OS {
    private final static String OS = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);

    public static String defaultBwsBinaryPath() {
        if (isWindows()) {
            return "%ProgramW6432%\\Bitwig Studio\\${bws.version}\\bin";
        } else if (isMac()) {
            return "/Applications/Bitwig Studio/Contents/MacOS";
        } else if (isUnix()) {
            // have no clue
            return "";
        } else {
            return "";
        }
    }

    private static boolean isWindows()
    {
        return OS.contains("win");
    }

    private static boolean isMac()
    {
        return OS.contains("mac");
    }

    private static boolean isUnix()
    {
        return OS.contains("nux");
    }
}
