package com.dfi.sbc2ha.components.platform.ha.autodiscovery.message;

import com.dfi.sbc2ha.Version;
import com.dfi.sbc2ha.components.platform.bus.ModbusFactory;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.components.sensor.modbus.ModbusSensor;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.ActuatorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.actuator.CoverConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.ModbusSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.AnalogSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.NtcConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.ResistanceConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSwitchConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.Register;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.oneWire.therm.DS18B20;
import com.dfi.sbc2ha.services.state.sensor.ButtonState;
import com.dfi.sbc2ha.web.Server;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class Availability {
    public static final String CONFIG = "config";
    public static final String CMD = "cmd";
    public static final String SET = "set";
    public static final String TOPIC_SEPARATOR = "/";
    public static final String SET_BRIGHTNESS = "set_brightness";
    public static final String TOPIC = "topic";
    public static final String STATE = "state";

    protected static String topicPrefix = "boneIO";
    private static String version;
    private static String model;
    @JsonIgnore
    private final HaDeviceType haDeviceType;
    @JsonIgnore
    private final SbcDeviceType stateDeviceType;
    /**
     * device map REQUIRED
     * Information about the device this device trigger is a part of to tie it into the device registry. At least one of identifiers or connections must be present to identify the device.
     */
    private final DeviceAvailability device;
    @JsonIgnore
    private String id;
    private List<Map<String, String>> availability = new ArrayList<>();
    private String name;
    @JsonProperty("state_topic")
    private String stateTopic;
    @JsonProperty("unique_id")
    private String uniqueId;

    public Availability(String id, String name, HaDeviceType haDeviceType, SbcDeviceType stateDeviceType) {
        String topic = Availability.topicPrefix;
        String model = Availability.model;

        Map<String, String> availabilityTopic = new HashMap<>();
        availabilityTopic.put(TOPIC, formatTopic(topic, STATE));
        availability.add(availabilityTopic);

        device = new DeviceAvailability(topic, model);
        this.name = name;

        this.id = id;
        this.haDeviceType = haDeviceType;
        this.stateDeviceType = stateDeviceType;


        stateTopic = formatTopic(topic, getStateDeviceTypeName(), id);
        uniqueId = topic + stateDeviceType.toLowerCase() + id;
    }

    public static String getDefault(String current, String defaultValue) {
        return Optional.of(current).orElse(defaultValue);
    }


    public static String formatTopic(String... args) {
        List<String> filtered = Arrays.stream(args).map(v -> v.replaceAll("[^a-zA-Z0-9_-]", "_")).collect(Collectors.toList());
        return String.join(Availability.TOPIC_SEPARATOR, filtered);

    }

    public static DeviceTriggerAvailability getDeviceTriggerAvailability(InputSwitchConfig config, List<ButtonState> states) {
        String id = String.valueOf(config.getInput());
        String name = Optional.of(config.getName()).orElse(id);

        return new DeviceTriggerAvailability(id, name, states);
    }

    public static Availability getSensorAvailability(SensorConfig config) {
        if (config instanceof AnalogSensorConfig) {
            return getSensorAvailability((AnalogSensorConfig) config);
        } else if (config instanceof ResistanceConfig) {
            return getSensorAvailability((ResistanceConfig) config);
        } else if (config instanceof NtcConfig) {
            return getTempSensorAvailability(config);
        } else if (config instanceof DS18B20) {
            return getTempSensorAvailability(config);
        } else if (config instanceof Lm75SensorConfig) {
            return getTempSensorAvailability(config);
        } else if (config instanceof InputSwitchConfig) {
            return getDeviceTriggerAvailability((InputSwitchConfig) config);
        } else if (config instanceof InputSensorConfig) {
            return getBinarySensorAvailability((InputSensorConfig) config);
        } else if (config instanceof ModbusSensorConfig) {
            return getModbusSensorAvailability((ModbusSensorConfig) config);
        }
        throw new IllegalArgumentException("unknown sensor config");
    }

    private static IterableAvailability getDeviceTriggerAvailability(InputSwitchConfig config) {
        switch (config.getClickDetection()) {
            default:
            case SINGLE:
                return Availability.getDeviceTriggerAvailability(config,
                        List.of(ButtonState.SINGLE));
            case DOUBLE:
                return Availability.getDeviceTriggerAvailability(config,
                        List.of(ButtonState.SINGLE, ButtonState.DOUBLE));
            case LONG:
                return Availability.getDeviceTriggerAvailability(config,
                        List.of(ButtonState.SINGLE, ButtonState.DOUBLE, ButtonState.LONG, ButtonState.RELEASE));
        }
    }


    public static Availability getSensorAvailability(InputConfig config) {
        switch (config.getPlatform()) {
            case SWITCH:
                return getInputAvailability((InputSwitchConfig) config);
            case DIGITAL:
            default:
                return getBinarySensorAvailability((InputSensorConfig) config);
        }

    }

    public static Availability getSensorAvailability(Lm75SensorConfig config) {
        return getTempSensorAvailability(config);

    }

    public static Availability getTempSensorAvailability(SensorConfig config) {

        return new TempSensorAvailability(config.getName(), config.getName());
    }

    public static Availability getSensorAvailability(AnalogSensorConfig config) {

        return new AnalogSensorAvailability(config.getName(), config.getName());
    }

    public static Availability getSensorAvailability(ResistanceConfig config) {
        return new ResistanceSensorAvailability(config.getName(), config.getName());
    }

    public static InputAvailability getInputAvailability(InputSwitchConfig config) {
        String id = String.valueOf(config.getInput());
        String name = Optional.of(config.getName()).orElse(id);

        InputAvailability availability = new InputAvailability(id, name);
        if (null != config.getDeviceClass()) {
            availability.deviceClass = config.getDeviceClass().toString().toLowerCase();
        }
        return availability;
    }

    private static BinarySensorAvailability getBinarySensorAvailability(InputSensorConfig config) {
        String id = String.valueOf(config.getInput());
        String name = Optional.of(config.getName()).orElse(id);

        BinarySensorAvailability availability = new BinarySensorAvailability(id, name);
        if (null != config.getDeviceClass()) {
            availability.deviceClass = config.getDeviceClass().toString().toLowerCase();
        }
        return availability;
    }

    public static Availability getActuatorAvailability(ActuatorConfig config) {
        String id = String.valueOf(config.getOutput());
        String name = Optional.of(config.getName()).orElse(config.getKind() + "_" + id);
        switch (config.getOutputType()) {
            default:
            case SWITCH:
                return new SwitchAvailability(id, name);
            case LIGHT:
                return new LightAvailability(id, name);
            case LED:
                return new LedAvailability(id, name);
            case COVER:
                return new CoverAvailability(id, name, ((CoverConfig) config).getDeviceClass());
        }
    }

    public static TextFieldAvailability getTextFieldActuatorAvailability(String id, String name) {
        return new TextFieldAvailability(id, name);
    }

    public static void setModel(String model) {
        Availability.model = model;
    }

    public static void setVersion(String version) {
        Availability.version = version;
    }

    public static Availability getModbusSensorAvailabilityOld(Register register, ModbusSensor device, String model, int registerBase) {
        ModbusSensorAvailabilityOld availability = null;
        try {
            availability = new ModbusSensorAvailabilityOld(register, device, model, registerBase);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
        }

        return availability;
    }

    public static Availability getModbusSensorAvailability(ModbusSensorConfig sensorConfig) {
        ModbusSensorDefinition def = ModbusFactory.getDevice(sensorConfig.getModel());
        return new ModbusSensorAvailability(sensorConfig, def);

    }

    @JsonIgnore
    public String getTopicPrefix() {
        return Availability.topicPrefix;
    }

    public static void setTopicPrefix(String topicPrefix) {
        Availability.topicPrefix = topicPrefix;
    }

    @JsonIgnore
    public String getNodeName() {
        return getId();
    }

    @JsonIgnore
    public String getHaDeviceTypeName() {
        return haDeviceType.toLowerCase();
    }

    @JsonIgnore
    public String getStateDeviceTypeName() {
        return stateDeviceType.toLowerCase();
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class DeviceAvailability {

        /**
         * A link to the webpage that can manage the configuration of this device. Can be either an HTTP or HTTPS link.
         * optional
         */
        @JsonProperty("configuration_url")
        private String configurationUrl;

        /**
         * A list of connections of the device to the outside world as a list of tuples [connection_type, connection_identifier].
         * For example the MAC address of a network interface: 'connections': ['mac', '02:5b:26:a8:dc:12'].
         * optional
         */
        private List<Map<String, String>> connections;

        /**
         * A list of IDs that uniquely identify the device. For example a serial number.
         * optional
         */
        private List<String> identifiers;

        /**
         * The manufacturer of the device.
         * optional
         */
        private String manufacturer;

        /**
         * The model of the device.
         * optional
         */
        private String model;

        /**
         * The name of the device.
         * optional
         */
        private String name;

        /**
         * Suggest an area if the device isn’t in one yet.
         * optional
         */
        @JsonProperty("suggested_area")
        private String suggestedArea;

        /**
         * The firmware version of the device.
         * optional
         */
        @JsonProperty("sw_version")
        private String swVersion;

        /**
         * Identifier of a device that routes messages between this device and Home Assistant.
         * Examples of such devices are hubs, or parent devices of a sub-device.
         * This is used to show device topology in Home Assistant.
         * optional
         */
        @JsonProperty("via_device")
        private String viaDevice;


        public DeviceAvailability(String topic, String model) {
            identifiers = new ArrayList<>();
            manufacturer = "boneIO";
            identifiers.add(topic);
            this.model = model;
            name = topic;
            swVersion = Version.getVersion();

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                configurationUrl = "http://" + localHost.getHostAddress() + ":" + Server.getPort();
            } catch (UnknownHostException ignored) {

            }

        }
    }

}
