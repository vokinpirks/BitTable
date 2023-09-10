package com.vokinpirks;

import com.bitwig.extension.controller.api.ControllerHost;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;

@RequiredArgsConstructor
public class Notifier {

    private final ControllerHost host;

    private final ConsoleLogger logger;

    public void error(final String format, final Object... formatArgs) {
        final String message = format(format, formatArgs);
        host.showPopupNotification(message);
        logger.error(message);
    }

    public void info(final String format, final Object... formatArgs) {
        final String message = format(format, formatArgs);
        host.showPopupNotification(message);
        logger.info(message);
    }
}
