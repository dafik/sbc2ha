package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.bus.Bus;
import com.dfi.sbc2ha.bus.MCP23017Bus;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.GpioOutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.McpOutputConfig;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionOutputBoard;
import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;

public class ActuatorFactory {


    public static Relay createMcpOutput(McpOutputConfig config, MCP23017Bus bus, int initialValue) {

        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        MCP23017 busInternal = bus.getBus();
        PinInfo pinInfo = busInternal.getBoardPinInfo().getByGpioNumberOrThrow(outputDefinition.getPin());

        Relay.Builder builder = Relay.Builder.builder(pinInfo)
                .setDeviceFactory(busInternal)
                .setActiveHigh(true)
                .setInitialValue(initialValue)
                .setName(config.getName());

        return builder.build();
    }


    public static Actuator<?, ?, ?> getActuator(ActuatorConfig config, Bus<?> bus, float initialValue) {
        switch (config.getKind()) {
            case MCP:
                return createMcpOutput((McpOutputConfig) config, (MCP23017Bus) bus, (int) initialValue);
            case GPIO:
                return createLedOutput((GpioOutputConfig) config, initialValue);
            case PCA:
                throw new RuntimeException("not implemented yet");
            default:
                throw new IllegalArgumentException();
        }
    }

    private static Led createLedOutput(GpioOutputConfig config, float initialValue) {
        //TODO implement extensionconfig
        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        int gpio = outputDefinition.getPin();

        Led.Builder builder = Led.Builder.builder(gpio)
                .setActiveHigh(true)
                .setInitialValue(initialValue)
                .setPwmFrequency(100)
                .setName(config.getName());

        return builder.build();
    }
}
