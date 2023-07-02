package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.config.sbc2ha.definition.output.GpioOutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.output.McpOutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.output.OutputConfig;
import com.dfi.sbc2ha.helper.bus.BusFactory;
import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;

import java.util.Map;

public class ActuatorFactory {


    public static Relay createMcpOutput(McpOutputConfig config, Map<String, Object> busMap, int initialValue) {


        MCP23017 bus = BusFactory.getBus(config.getMcpId(), busMap, MCP23017.class);
        PinInfo pinInfo = bus.getBoardPinInfo().getByGpioNumberOrThrow(Integer.parseInt(config.getPin()));

        Relay.Builder builder = Relay.Builder.builder(pinInfo)
                .setDeviceFactory(bus)
                .setActiveHigh(true)
                .setInitialValue(initialValue)
                .setName(config.getName());

        return builder.build();
    }

/*    public static Actuator getActuator(OutputConfig config, Map<String, Object> busMap, boolean initialValue) {
        switch (config.getKind()) {
            case MCP:
                return createMcpOutput((McpOutputConfig) config, busMap, initialValue ? 1 : 0);
            case GPIO:
                return createLedOutput((GpioOutputConfig) config, busMap, initialValue ? 1 : 0);
            case PCA:
                throw new RuntimeException();
            default:
                throw new IllegalArgumentException();
        }
    }*/

    public static Actuator getActuator(OutputConfig config, Map<String, Object> busMap, float initialValue) {
        switch (config.getKind()) {
            case MCP:
                return createMcpOutput((McpOutputConfig) config, busMap, (int) initialValue);
            case GPIO:
                return createLedOutput((GpioOutputConfig) config, busMap, initialValue);
            case PCA:
                throw new RuntimeException();
            default:
                throw new IllegalArgumentException();
        }
    }

    private static Actuator createLedOutput(GpioOutputConfig config, Map<String, Object> busMap, float initialValue) {
        int gpio = Integer.parseInt(config.getPin());

        Led.Builder builder = Led.Builder.builder(gpio)
                .setActiveHigh(true)
                .setInitialValue(initialValue)
                .setPwmFrequency(100)
                .setName(config.getName());

        return builder.build();
    }
}
