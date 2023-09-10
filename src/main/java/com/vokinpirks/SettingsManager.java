package com.vokinpirks;

import com.bitwig.extension.controller.api.*;
import com.vokinpirks.enums.FileFormat;
import com.vokinpirks.enums.ResizeAlgorithm;
import com.vokinpirks.enums.Toggle;
import com.vokinpirks.util.Enums;
import com.vokinpirks.util.OS;
import com.vokinpirks.util.Strings;
import lombok.Getter;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static com.vokinpirks.enums.FileSystemItemType.DIRECTORY;
import static com.vokinpirks.enums.FileSystemItemType.FILE;
import static java.util.stream.Collectors.toList;

@Getter
public class SettingsManager {
    private static final int STRING_SETTING_MAX_LEN = 256;
    public static final int SAMPLE_LOCATION_COUNT = 7;

    private final ControllerHost host;

    private final SettableStringValue       okwtPathSetting;
    private final SettableStringValue       ffmpegPathSetting;
    private final SettableStringValue       wtSaveLocationSetting;
    private final SettableStringValue       wavSaveLocationSetting;
    private final SettableEnumValue         resizeAlgorithmSetting;
    private final SettableEnumValue         normalizeSetting;
    private final SettableEnumValue         maximizeSetting;
    private final SettableRangedValue       fadeSetting;
    private final SettableEnumValue         trimSetting;
    private final SettableRangedValue       trimThresholdSetting;
    private final SettableEnumValue         shuffleSetting;
    private final SettableRangedValue       shuffleChunksSetting;
    private final SettableStringValue       fileSetting;
    private final SettableEnumValue         fileFormatSetting;
    private final List<SettableStringValue> sampleLocationsSettings;
    private final SettableBooleanValue      appendDateTimeSetting;

    public SettingsManager(
            ControllerHost host,
            Notifier notifier,
            ConsoleLogger logger,
            Consumer<Path> sampleDirectoryAdded
    ) {
        this.host = host;

        // global settings
        final Preferences preferences = host.getPreferences();
        okwtPathSetting = preferences.getStringSetting("okwt", "Paths", STRING_SETTING_MAX_LEN, "");
        okwtPathSetting.addValueObserver(newValue ->
                Strings.isValidPath(newValue, DIRECTORY, notifier::error));

        ffmpegPathSetting = preferences.getStringSetting("ffmpeg", "Paths", STRING_SETTING_MAX_LEN, OS.defaultBwsBinaryPath());
        ffmpegPathSetting.addValueObserver(newValue -> {
            // skip validation for the default value
            if (!OS.defaultBwsBinaryPath().equals(newValue)) {
                Strings.isValidPath(resolvePath(newValue), DIRECTORY, notifier::error);
            }
        });

        wtSaveLocationSetting = preferences.getStringSetting("WT Files", "Save Locations", STRING_SETTING_MAX_LEN, "");
        wtSaveLocationSetting.addValueObserver(newValue ->
                Strings.isValidPath(newValue, DIRECTORY, notifier::error));

        wavSaveLocationSetting = preferences.getStringSetting("WAV Files", "Save Locations", STRING_SETTING_MAX_LEN, "");
        wavSaveLocationSetting.addValueObserver(newValue ->
                Strings.isValidPath(newValue, DIRECTORY, notifier::error));

        sampleLocationsSettings = IntStream.range(0, SAMPLE_LOCATION_COUNT).boxed()
                .map(i -> preferences.getStringSetting(" ".repeat(i + 1), "Samples Locations", STRING_SETTING_MAX_LEN, ""))
                .peek(setting -> setting.addValueObserver(newValue -> {
                    if (Strings.isValidPath(newValue, DIRECTORY, notifier::error)) {
                        sampleDirectoryAdded.accept(Path.of(newValue));
                    }
                }))
                .collect(toList());

        preferences.getBooleanSetting("Debug", "Misc", false).addValueObserver(logger::setDebug);
        this.appendDateTimeSetting = preferences.getBooleanSetting("Append date and time", "Misc", true);

        // project level settings
        final DocumentState documentState = host.getDocumentState();
        resizeAlgorithmSetting = documentState.getEnumSetting("Resize Algorithm", "Processing",
                Enums.namesOf(ResizeAlgorithm.class),
                ResizeAlgorithm.truncate.name()
        );

        // Bitwig won't show boolean settings in the controller pane, have to use a custom enum for toggles
        final String[] onOff = Enums.namesOf(Toggle.class);

        normalizeSetting        = documentState.getEnumSetting("Normalize", "Processing", onOff, Toggle.off.name().toLowerCase());
        maximizeSetting         = documentState.getEnumSetting("Maximize", "Processing", onOff, Toggle.off.name().toLowerCase());
        fadeSetting             = documentState.getNumberSetting("Fade", "Processing", 0.0, 128, 1., "samples", 0.);
        trimSetting             = documentState.getEnumSetting("Trim", "Processing", onOff, Toggle.off.name().toLowerCase());
        trimThresholdSetting    = documentState.getNumberSetting("Trim Threshold", "Processing", 0.0, 1., 0.05, "", 0.);
        shuffleSetting          = documentState.getEnumSetting("Shuffle", "Processing", onOff, Toggle.off.name().toLowerCase());
        shuffleChunksSetting    = documentState.getNumberSetting("Shuffle groups", "Processing", 0, 128, 1, "groups", 0.);
        fileSetting             = documentState.getStringSetting("File", "File", STRING_SETTING_MAX_LEN, "");
        fileFormatSetting       = documentState.getEnumSetting("Save as", "Convert", Enums.namesOf(FileFormat.OUTPUT_FORMATS), FileFormat.WAV.name());

        fileSetting.addValueObserver(newValue -> Strings.isValidPath(Strings.unwrapQuotes(newValue), FILE, notifier::error));
    }

    public Path okwtPath() {
        return Path.of(okwtPathSetting.get());
    }

    public String ffmpegPath() {
        return resolvePath(ffmpegPathSetting.get());
    }

    public Path infile() {
        return Strings.optional(fileSetting.get())
                .map(Strings::unwrapQuotes)
                .map(Path::of)
                .orElse(null);
    }

    public FileFormat fileFormat() {
        return FileFormat.valueOf(fileFormatSetting.get());
    }

    public Path wavSaveLocation() {
        return Strings.optional(wavSaveLocationSetting.get())
                .map(Path::of)
                .orElse(null);
    }

    public Path wtSaveLocation() {
        return Strings.optional(wtSaveLocationSetting.get())
                .map(Path::of)
                .orElse(null);
    }

    public ResizeAlgorithm resizeAlgorithm() {
        return ResizeAlgorithm.valueOf(resizeAlgorithmSetting.get());
    }

    public Toggle normalize() {
        return Toggle.valueOf(normalizeSetting.get());
    }

    public Toggle maximize() {
        return Toggle.valueOf(maximizeSetting.get());
    }

    public Toggle trim() {
        return Toggle.valueOf(trimSetting.get());
    }

    public Integer fade() {
        return (int) fadeSetting.getRaw();
    }

    public Toggle shuffle() {
        return Toggle.valueOf(shuffleSetting.get());
    }

    public Double trimThreshold() {
        return trimThresholdSetting.get();
    }

    public Integer shuffleChunks() {
        return (int) shuffleChunksSetting.getRaw();
    }

    public Boolean appendDateTime() {
        return appendDateTimeSetting.get();
    }

    public List<Path> sampleLocations() {
        return sampleLocationsSettings.stream()
                .map(StringValue::get)
                .map(Path::of)
                .filter(path -> path.toFile().exists())
                .collect(toList());
    }

    private String resolvePath(final String s) {
        return s.replace("${bws.version}", host.getHostVersion());
    }
}
