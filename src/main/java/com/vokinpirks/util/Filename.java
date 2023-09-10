package com.vokinpirks.util;

import com.vokinpirks.enums.FileFormat;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class Filename {

    public static String withoutExtension(final Path path) {
        return withoutExtension(path.getFileName().toString());
    }

    public static String withoutExtension(final String filename) {
        var result = filename;
        final var pos = filename.lastIndexOf(".");

        if (pos > 0 && pos < (filename.length() - 1)) { // If '.' is not the first or last character.
            result = filename.substring(0, pos);
        }

        return result;
    }

    public static String extensionOf(final String filename) {
        final var pos = filename.lastIndexOf(".");

        if (pos > 0 && pos < (filename.length() - 1)) { // If '.' is not the first or last character.
            return filename.substring(pos);
        }

        return null;
    }

    public static String withExtension(final String fileName, final FileFormat fileFormat) {
        return withExtension(fileName, fileFormat.primaryExtension());
    }

    public static String withExtension(final String fileName, final String extension) {
        return fileName + extension;
    }

    public static String appendDatetime(String s) {
        return s + "[" + DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss").format(LocalDateTime.now()) + "]";
    }
}
