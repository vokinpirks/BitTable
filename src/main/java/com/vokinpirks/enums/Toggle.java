package com.vokinpirks.enums;

// Bitwig doesn't show boolean settings in the controller script panel, have to use this as a workaround
public enum Toggle {
    on,
    off;

    public boolean asBoolean() {
        return switch (this) {
            case on -> true;
            case off -> false;
        };
    }
}
