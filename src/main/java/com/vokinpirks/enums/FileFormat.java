package com.vokinpirks.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public enum FileFormat {
    WAV(List.of(".wav"), true, true, false),
    AIFF(List.of(".aif", ".aiff"), true, false, true),
    FLAC(List.of(".flac"), true, false, true),
    MP3(List.of(".mp3"), true, false, true),
    WT(List.of(".wt"), false, true, false),
    ;

    @Getter
    private final List<String> extensions;

    @Getter
    private final boolean input;

    @Getter
    private final boolean output;

    private final boolean requiresFfmpeg;

    public static final Set<FileFormat> INPUT_FORMATS = stream(FileFormat.values()).filter(FileFormat::isInput).collect(toSet());

    public static final Set<FileFormat> OUTPUT_FORMATS = stream(FileFormat.values()).filter(FileFormat::isOutput).collect(toSet());

    public static FileFormat fromExtension(final String extension) {
        return stream(FileFormat.values())
                .filter(fileFormat -> fileFormat.extensions.contains(extension))
                .findFirst()
                .orElse(null);
    }

    public String primaryExtension() {
        return extensions.get(0);
    }

    public boolean doesRequireFfmpeg() {
        return requiresFfmpeg;
    }
}
