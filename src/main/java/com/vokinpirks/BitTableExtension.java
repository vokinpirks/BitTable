package com.vokinpirks;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;
import com.vokinpirks.enums.FileFormat;
import com.vokinpirks.enums.FileSystemItemType;
import com.vokinpirks.util.Filename;
import com.vokinpirks.util.Processes;
import com.vokinpirks.util.Strings;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class BitTableExtension extends ControllerExtension {

    private static final long INDEXER_POLLING_INTERVAL = 2000L;

    private final FileNameIndexer sampleNameIndexer;

    private SettingsManager settingsManager;

    private CursorTrack cursorTrack;

    private PinnableCursorDevice cursorDevice;

    private PopupBrowser popupBrowser;

    private BrowserResultsItem cursorItem;

    private final ConsoleLogger logger;

    private final Notifier notifier;

    protected BitTableExtension(BitTableExtensionDefinition definition, ControllerHost host) {
        super(definition, host);

        this.logger = new ConsoleLogger(host);
        this.notifier = new Notifier(host, logger);
        this.sampleNameIndexer = new FileNameIndexer(logger);
    }

    @Override
    public void init() {
        logger.info("Host version: %s", getHost().getHostVersion());

        this.cursorTrack = getHost().createCursorTrack(16, 16);
        this.cursorDevice = cursorTrack.createCursorDevice();
        cursorDevice.sampleName().markInterested();

        this.popupBrowser = getHost().createPopupBrowser();
        this.popupBrowser.exists().markInterested();
        this.cursorItem = popupBrowser.resultsColumn().createCursorItem();
        cursorItem.name().markInterested();

        this.settingsManager = new SettingsManager(getHost(), notifier, logger, sampleNameIndexer::indexDirectory);

        getHost().getPreferences().getSignalSetting(" ".repeat(SettingsManager.SAMPLE_LOCATION_COUNT + 2), "Samples Locations", "Force Reindex").addSignalObserver(() -> {
            sampleNameIndexer.clearIndex();
            settingsManager.sampleLocations().forEach(sampleNameIndexer::indexDirectory);
        });

        final var documentState = getHost().getDocumentState();

        documentState.getSignalSetting("Convert and", "1", "Save")
                .addSignalObserver(() -> performConversion(settingsManager.fileFormat(), false));

        documentState.getSignalSetting("Convert and", "2", "Load new Sampler")
                .addSignalObserver(() -> performConversion(FileFormat.WAV, true));

        documentState.getSignalSetting("Convert and", "3", "Load new Polymer")
                .addSignalObserver(() -> performConversion(FileFormat.WT, true));

        getHost().scheduleTask(this::touchIndexer, INDEXER_POLLING_INTERVAL);
    }

    private void performConversion(final FileFormat fileFormat, final boolean shouldLoadDevice) {
        if (saveLocation(fileFormat) == null) {
            notifier.error("You must specify save location for %s files first", fileFormat.primaryExtension());
            return;
        }

        final Optional<String> sampleName = inferSampleName();

        final Path inFilePath;
        if (sampleName.isPresent()) {
            // name of a sample comes from either popup browser or sampler
            // need to determine its full path before calling okwt
            final String sampleFullPath = sampleNameIndexer.getFullPath(sampleName.get());
            logger.debug("Sample: %s, full path: %s", sampleName.get(), sampleFullPath);

            if (sampleFullPath == null) {
                // unable to determine full path, possibly the sample wasn't indexed
                notifier.error("Unable to find full path to the sample '%s'. " +
                        "Consider adding the directory where it's located to samples locations in the controller settings", sampleName.get());
                return;
            }

            inFilePath = Path.of(sampleFullPath);
        } else {
            inFilePath = settingsManager.infile();
        }

        if (inFilePath == null) {
            notifier.error("There is nothing selected to convert. Specify path to a file, " +
                    "or select a sample in the popup browser, " +
                    "or select Sampler with a sample loaded in it");
            return;
        }

        if (!inFilePath.toFile().exists()) {
            notifier.error("The path '%s' points to nonexistent file", sampleName);
            return;
        }

        final Path outFilePath = buildOutputFilePath(Filename.withoutExtension(inFilePath), fileFormat);

        if (convert(inFilePath, outFilePath) && shouldLoadDevice) {
            cursorTrack.endOfDeviceChainInsertionPoint().insertFile(outFilePath.toString());
        }
    }

    private void touchIndexer() {
        sampleNameIndexer.checkForChanges();
        getHost().scheduleTask(this::touchIndexer, INDEXER_POLLING_INTERVAL);
    }

    private Optional<String> inferSampleName() {
        if (popupBrowser.exists().getAsBoolean()) {
            // the popup browser reports filename without extension
            return Strings.optional(cursorItem.name().get())
                    .flatMap(sampleName  ->
                        FileFormat.INPUT_FORMATS.stream()
                                .flatMap(fileFormat -> fileFormat.getExtensions().stream())
                                .map(extension -> Filename.withExtension(sampleName, extension))
                                .filter(sampleNameIndexer::doesContain)
                                .findFirst()
                    );
        } else {
            return Strings.optional(cursorDevice.sampleName().get());
        }
    }

    private Path buildOutputFilePath(final String fileName, final FileFormat outputFileFormat) {
        String filenameWithoutExtension = Filename.withoutExtension(fileName);

        if (settingsManager.appendDateTime()) {
            filenameWithoutExtension = Filename.appendDatetime(filenameWithoutExtension);
        }

        final String filename = Filename.withExtension(filenameWithoutExtension, outputFileFormat);
        return saveLocation(outputFileFormat).resolve(filename);
    }

    private Path saveLocation(final FileFormat fileFormat) {
        return switch (fileFormat) {
            case WAV -> settingsManager.wavSaveLocation();
            case WT -> settingsManager.wtSaveLocation();
            default -> throw new IllegalStateException("File formats other than WAV and WT currently are not supposed to be used for output files");
        };

    }

    @Override
    public void exit() {
        sampleNameIndexer.close();
    }

    @Override
    public void flush() {
    }

    @SneakyThrows
    private boolean convert(Path inFile, Path outFile) {
        final ConversionCommandBuilder conversionCommandBuilder = createCommandBuilder(inFile, outFile);
        final List<String> command = conversionCommandBuilder.build();
        logger.debug("Command to call okwt: %s",
                command.stream().map(Strings::wrapWithQuotesIfContainsSpace).collect(joining(" ", "(", ")")));

        final Optional<String> ffmpegPath = Strings.optional(settingsManager.ffmpegPath());

        final FileFormat recognizedInFileFormat = FileFormat.fromExtension(Filename.extensionOf(inFile.toString()));
        // some audio files require ffmpeg
        if (recognizedInFileFormat != null && recognizedInFileFormat.doesRequireFfmpeg()) {
            if (ffmpegPath.isEmpty()) {
                notifier.error("ffmpeg is required for files of type %s", recognizedInFileFormat.primaryExtension());
                return false;
            }

            if (Strings.isNotValidPath(ffmpegPath.get(), FileSystemItemType.DIRECTORY, logger::error)) {
                notifier.error("ffmpeg path is invalid");
                return false;
            }
        }

        switch (Processes.run(command, ffmpegPath, logger)) {
            case SUCCESS -> {
                notifier.info("Conversion is finished");
                return true;
            }

            case IO_ERROR -> {
                notifier.error("Unable to call okwt. Check if okwt path is specified correctly in the controller settings");
                return false;
            }

            case TIMEOUT -> {
                notifier.error("Unable to convert the file due to timeout");
                return false;
            }

            case UNKNOWN_ERROR -> {
                notifier.error("Unable to convert the file due to unknown error");
                return false;
            }

            case OKWT_ERROR -> {
                notifier.error("Unable to convert the file as okwt has exited with error code");
                return false;
            }

            default -> throw new RuntimeException("Unhandled CallResult value encountered");
        }
    }

    private ConversionCommandBuilder createCommandBuilder(Path inFile, Path outFile) {
        return ConversionCommandBuilder.builder()
                .okwtPath(settingsManager.okwtPath().resolve("okwt").toString())
                .inFile(inFile.toString())
                .outFile(outFile.toString())
                .resizeAlgorithm(settingsManager.resizeAlgorithm())
                .maximize(settingsManager.maximize().asBoolean())
                .normalize(settingsManager.normalize().asBoolean())
                .fade(settingsManager.fade())
                .trim(settingsManager.trim().asBoolean())
                .trimThreshold(settingsManager.trimThreshold())
                .shuffle(settingsManager.shuffle().asBoolean())
                .shuffleChunks(settingsManager.shuffleChunks())
                .build();
    }
}
