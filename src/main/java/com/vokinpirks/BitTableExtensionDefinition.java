package com.vokinpirks;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

import java.util.UUID;

public class BitTableExtensionDefinition extends ControllerExtensionDefinition {

    private static final String AUTHOR =                "vokinpirks";
    private static final String EXTENSION_NAME =        "BitTable";
    private static final String VERSION =               "0.1";
    private static final UUID   EXTENSION_ID =          UUID.fromString("6345b8fb-39ad-43c5-b9fc-adf715b73e0b");
    private static final int    REQUIRED_API_VERSION =  10;

    @Override
    public String getHardwareVendor() {
        return "Utilities";
    }

    @Override
    public String getHardwareModel() {
        return EXTENSION_NAME;
    }

    @Override
    public int getNumMidiInPorts() {
        return 0;
    }

    @Override
    public int getNumMidiOutPorts() {
        return 0;
    }

    @Override
    public void listAutoDetectionMidiPortNames(AutoDetectionMidiPortNamesList list, PlatformType platformType) {
    }

    @Override
    public ControllerExtension createInstance(ControllerHost host) {
        return new BitTableExtension(this, host);
    }

    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public UUID getId() {
        return EXTENSION_ID;
    }

    @Override
    public int getRequiredAPIVersion() {
        return REQUIRED_API_VERSION;
    }
}
