package com.vokinpirks.util;

import com.vokinpirks.enums.CallResult;
import com.vokinpirks.ConsoleLogger;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Processes {

    /**
     *
     * @param command
     * @return
     * zero if success
     * non zero in case of a fail
     * null in case of timeout (5s)
     */
    public static CallResult run(final List<String> command, final Optional<String> path, final ConsoleLogger logger) {
        final Process process;
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .redirectErrorStream(true);
            path.ifPresent(s -> Env.appendPath(processBuilder.environment(), s));

            logger.debug("");
            processBuilder.environment().forEach((key, value) -> logger.debug("%s = %s", key, value));
            logger.debug("");

            process = processBuilder.start();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return CallResult.IO_ERROR;
        }

        logger.debug("Waiting for the process to finish");

        final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                logger.debug(line);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return CallResult.UNKNOWN_ERROR;
        }

        try {
            if (process.waitFor(5L, TimeUnit.SECONDS)) {
                if (process.exitValue() != 0) {
                    logger.error("Process exited with code %s", process.exitValue());
                }

                logger.debug("Process finished with exit code %s", process.exitValue());

                return process.exitValue() == 0
                        ? CallResult.SUCCESS
                        : CallResult.OKWT_ERROR;
            } else {
                return CallResult.TIMEOUT;
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            return CallResult.UNKNOWN_ERROR;
        }
    }
}
