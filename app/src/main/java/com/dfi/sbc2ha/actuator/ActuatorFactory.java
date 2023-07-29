package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.bus.Bus;
import com.dfi.sbc2ha.bus.MCP23017Bus;
import com.dfi.sbc2ha.bus.PCA9685Bus;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.*;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ActuatorType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.actuator.LedFadingEvent;
import com.dfi.sbc2ha.helper.StateManager;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionOutputBoard;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.diozero.api.PinInfo;
import com.diozero.devices.MCP23017;
import com.diozero.devices.PCA9685;

import java.util.List;

public class ActuatorFactory {


    public static Actuator getActuator(ActuatorConfig config, List<Bus<?>> busMap) {
        ExtensionOutputBoard.OutputDefinition outputDefinition = ExtensionBoardInfo.getInstance().getOut().get(config.getOutput());
        Bus<?> bus;

        switch (config.getKind()) {
            case MCP:
                bus = getBus(busMap, PlatformType.MCP23017, "mcp" + outputDefinition.getId(), MCP23017Bus.class);
                return createMcpRelay((McpOutputConfig) config, (MCP23017Bus) bus);
            case GPIOPWM:
                if (System.getProperty("usePwmFadingLed") != null) {
                    return createGpioFadeLed((GpioPwmOutputConfig) config);
                } else {
                    return createGpioLed((GpioPwmOutputConfig) config);
                }
            case GPIO:
                return createGpioRelay((GpioOutputConfig) config);
            case PCA:
                bus = getBus(busMap, PlatformType.PCA9685, "pca" + outputDefinition.getId(), PCA9685Bus.class);

                return createPcaLed((PcaOutputConfig) config, (PCA9685Bus) bus);

            case COVER:
                MCP23017Bus openBus = getBus(busMap, PlatformType.MCP23017, "mcp" + ((CoverConfig) config).getOpenRelayBusId(), MCP23017Bus.class);
                MCP23017Bus closeBus = getBus(busMap, PlatformType.MCP23017, "mcp" + ((CoverConfig) config).getCloseRelayBusId(), MCP23017Bus.class);
                return createMcpCover((CoverConfig) config, openBus, closeBus);

            default:
                throw new IllegalArgumentException();
        }
    }

    private static Actuator createMcpCover(CoverConfig config, MCP23017Bus openBus, MCP23017Bus closeBus) {
        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition odOpen = out.get(Integer.parseInt(config.getOpenRelay()));
        ExtensionOutputBoard.OutputDefinition odClose = out.get(Integer.parseInt(config.getCloseRelay()));

        MCP23017 openBusInternal = openBus.getBus();
        MCP23017 closeBusInternal = closeBus.getBus();

        PinInfo openPinInfo = openBusInternal.getBoardPinInfo().getByGpioNumberOrThrow(odOpen.getPin());
        PinInfo closePinInfo = closeBusInternal.getBoardPinInfo().getByGpioNumberOrThrow(odClose.getPin());

        StateEvent initialValue = StateManager.getInstance().get(config.getOutputType(), config.getName());

        Cover.Builder builder = Cover.Builder.builder(openPinInfo, closePinInfo)
                .setOpenDeviceFactory(openBusInternal)
                .setCloseDeviceFactory(closeBusInternal)
                .setInitialState(initialValue)
                .setName(config.getName())
                .setId(config.getOutput())
                .setCloseTime(config.getCloseTime())
                .setOpenTime(config.getOpenTime());


        return builder.build();


    }

    private static Relay createMcpRelay(McpOutputConfig config, MCP23017Bus bus) {

        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        MCP23017 busInternal = bus.getBus();
        PinInfo pinInfo = busInternal.getBoardPinInfo().getByGpioNumberOrThrow(outputDefinition.getPin());

        StateEvent initialValue = StateManager.getInstance().get(config.getOutputType(), config.getName());

        Relay.Builder builder = Relay.Builder.builder(pinInfo)
                .setDeviceFactory(busInternal)
                .setActiveHigh(true)
                .setInitialState(initialValue)
                .setName(config.getName())
                .setId(config.getOutput())
                .setMomentaryTurnOn(config.getMomentaryTurnOn())
                .setMomentaryTurnOff(config.getMomentaryTurnOff());

        return builder.build();
    }

    private static Relay createGpioRelay(GpioOutputConfig config) {

        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        StateEvent initialValue = StateManager.getInstance().get(config.getOutputType(), config.getName());

        int gpio = outputDefinition.getPin();

        Relay.Builder builder = Relay.Builder.builder(gpio)
                .setActiveHigh(true)
                .setInitialState(initialValue)
                .setName(config.getName())
                .setId(config.getOutput())
                .setMomentaryTurnOn(config.getMomentaryTurnOn())
                .setMomentaryTurnOff(config.getMomentaryTurnOff());

        return builder.build();
    }

    private static Led createPcaLed(PcaOutputConfig config, PCA9685Bus bus) {
        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        PCA9685 busInternal = bus.getBus();
        PinInfo pinInfo = busInternal.getBoardPinInfo().getByGpioNumberOrThrow(outputDefinition.getPin());

        StateEvent initialValue = StateManager.getInstance().get(config.getOutputType(), config.getName());

        Led.Builder builder = Led.Builder.builder(pinInfo)
                .setPwmDeviceFactory(busInternal)
                .setActiveHigh(true)
                .setInitialState(initialValue)
                .setName(config.getName())
                .setId(config.getOutput())
                .setPercentageDefaultBrightness(config.getPercentageDefaultBrightness())
                .setMomentaryTurnOn(config.getMomentaryTurnOn())
                .setMomentaryTurnOff(config.getMomentaryTurnOff());

        return builder.build();
    }

    private static Led createGpioLed(GpioPwmOutputConfig config) {
        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        StateEvent initialValue = StateManager.getInstance().get(config.getOutputType(), config.getName());

        int gpio = outputDefinition.getPin();

        Led.Builder builder = Led.Builder.builder(gpio)
                .setActiveHigh(true)
                .setInitialState(initialValue)
                .setPwmFrequency(100)
                .setName(config.getName())
                .setId(config.getOutput());

        return builder.build();
    }
    private static LedFading createGpioFadeLed(GpioPwmOutputConfig config) {
        ExtensionOutputBoard out = ExtensionBoardInfo.getInstance().getOut();
        ExtensionOutputBoard.OutputDefinition outputDefinition = out.get(config.getOutput());

        LedFadingEvent initialValue = (LedFadingEvent) StateManager.getInstance().get(config.getOutputType(), config.getName());
        if(initialValue == null){
            //TODO is this nessery ?
            initialValue = new LedFadingEvent(ActuatorState.OFF, 0, 0);
            StateManager.getInstance().save(ActuatorType.LED, config.getName(),initialValue);
        }

        int gpio = outputDefinition.getPin();

        LedFading.Builder builder = LedFading.Builder.builder(gpio)
                .setActiveHigh(true)
                .setInitialState(initialValue)
                .setPwmFrequency(100)
                .setName(config.getName())
                .setId(config.getOutput());

        return builder.build();
    }

    public static TextField getLoggerField() {
        return new TextField("loggerInput", 1000);
    }

    private static <B extends Bus<?>> B getBus(List<Bus<?>> busMap, PlatformType type, String id, Class<B> returnClass) {
        return busMap.stream()
                .filter(bus -> bus.getPlatformType() == type)
                .filter(bus -> bus.getId().equals(id))
                .map(returnClass::cast)
                .findFirst()
                .orElseThrow();
    }
}
