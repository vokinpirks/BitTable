package com.vokinpirks;

import com.vokinpirks.enums.ResizeAlgorithm;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;

@Builder
@Getter
public class ConversionCommandBuilder {

    private String okwtPath;

    private String inFile;

    private String outFile;

    private ResizeAlgorithm resizeAlgorithm;

    private Boolean trim;

    private Double trimThreshold;

    private Boolean shuffle;

    private Integer shuffleChunks;

    private Integer fade;

    private Boolean normalize;

    private Boolean maximize;

    private Integer frames;

    private Integer frameSize;

    public List<String> build() {
        final List<String> commands = new ArrayList<>(List.of(
                okwtPath,
                "--infile",
                inFile,
                "--outfile",
                outFile
        ));

        if (resizeAlgorithm != ResizeAlgorithm.none) {
            commands.add("--resize");
            commands.add(resizeAlgorithm.name());
        }

        if (trim) {
            commands.add("--trim");
            commands.add(valueOf(trimThreshold));
        }

        if (shuffle) {
            commands.add("--shuffle");
            if (shuffleChunks > 0) {
                commands.add(valueOf(shuffleChunks));
            }
        }

        if (fade > 0) {
            commands.add("--fade");
            commands.add(valueOf(fade));
        }

        if (normalize) {
            commands.add("--normalize");
        }

        if (maximize) {
            commands.add("--maximize");
        }

        if (frames != null && frames > 0) {
            commands.add("--num-frames");
            commands.add(valueOf(frames));
        }

        return commands;
    }
}
