package com.vokinpirks.util;

import com.vokinpirks.enums.FileSystemItemType;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import static java.lang.String.format;

@UtilityClass
public class Strings {
    public static boolean isNullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }

    public static Optional<String> optional(final String s) {
        return isNullOrEmpty(s)
                ? Optional.empty()
                : Optional.of(s);
    }

    public static boolean isValidPath(
            final String path,
            final FileSystemItemType itemType,
            final Consumer<String> invalidPathCallback
    ) {
        if (isNullOrEmpty(path)) {
            return false;
        }

        final File file;
        try {
            file = Path.of(path).toFile();
        } catch (InvalidPathException e) {
            invalidPathCallback.accept(format("Invalid path: %s", path));
            return false;
        }

        if (!file.exists()) {
            invalidPathCallback.accept(format("%s '%s' doesn't exist", itemType.name().toLowerCase(), path));
            return false;
        }

        final boolean isOfExpectedType = switch (itemType) {
            case FILE -> file.isFile();
            case DIRECTORY -> file.isDirectory();
        };

        if (!isOfExpectedType) {
            invalidPathCallback.accept(format("Path must point to a %s", itemType.name().toLowerCase()));
            return false;
        }

        return true;
    }

    public static boolean isNotValidPath(
            final String s,
            final FileSystemItemType itemType,
            final Consumer<String> invalidPathCallback
    ) {
        return !isValidPath(s, itemType, invalidPathCallback);
    }

    public static String wrapWithQuotesIfContainsSpace(String s) {
        return s.contains(" ")
                ? "\"" + s + "\""
                : s;
    }

    public static String unwrapQuotes(final String s) {
        if (s.length() < 2) {
            return s;
        }

        if (s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
            return s.substring(1, s.length() - 1);
        }

        return s;
    }
}
