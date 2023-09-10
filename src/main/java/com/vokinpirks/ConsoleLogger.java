package com.vokinpirks;

import com.bitwig.extension.controller.api.ControllerHost;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class ConsoleLogger {

    private final ControllerHost controllerHost;

    @Setter
    private boolean debug = false;

    public void info(final String message, final Object... formatArgs) {
        log(false, message, formatArgs);
    }

    public void debug(final String message, final Object... formatArgs) {
        log(true, message, formatArgs);
    }

    public void error(final String message, final Object... formatArgs) {
        controllerHost.errorln(String.format(message, formatArgs));
    }

    private void log(final boolean debug, final String message, final Object... formatArgs) {
        if (this.debug || !debug) {
            controllerHost.println(String.format(message, formatArgs));
        }
    }
}
