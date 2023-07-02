package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.Version;
import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.input.InputSwitchConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.modbus.Register;
import com.dfi.sbc2ha.config.sbc2ha.definition.output.OutputConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.AdcSensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.Lm75SensorConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SensorConfig;
import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;
import com.dfi.sbc2ha.sensor.binary.ButtonState;
import com.dfi.sbc2ha.sensor.modbus.ModbusSensor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.tinylog.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class Availability {
    public static final String CONFIG = "config";
    public static final String CMD = "cmd";
    public static final String SET = "set";
    public static final String TOPIC_SEPARATOR = "/";
    public static final String SET_BRIGHTNESS = "set_brightness";
    public static final String TOPIC = "topic";

    protected static String topicPrefix = "boneIO";
    private static String version;
    private static String model;

    @JsonIgnore
    private final String id;
    @JsonIgnore
    private final HaDeviceType haDeviceType;
    @JsonIgnore
    private final SbcDeviceType stateDeviceType;
    /**
     * device map REQUIRED
     * Information about the device this device trigger is a part of to tie it into the device registry. At least one of identifiers or connections must be present to identify the device.
     */
    private final DeviceAvailability device;
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
        availabilityTopic.put(TOPIC, formatTopic(topic, Constants.STATE));
        availability.add(availabilityTopic);

        device = new DeviceAvailability(topic, model);
        this.name = name;

        this.id = id;
        this.haDeviceType = haDeviceType;
        this.stateDeviceType = stateDeviceType;


        stateTopic = formatTopic(topic, getStateDeviceTypeName(), id);
        uniqueId = topic + stateDeviceType.toLowerCase() + id;
    }

    public static String formatTopic(String... args) {
        List<String> filtered = Arrays.stream(args).map(v -> v.replaceAll("[^a-zA-Z0-9_-]", "_")).collect(Collectors.toList());
        return String.join(Availability.TOPIC_SEPARATOR, filtered);

    }

    public static String getDefault(String current, String defaultValue) {
        return Optional.of(current).orElse(defaultValue);
    }


/*    public static DeviceTriggerAvailability getDeviceTriggerAvailability(InputSwitchConfig config, ButtonState state) {
        String id = config.getPin();
        String name = Optional.of(config.getId()).orElse(config.getPin());

        return new DeviceTriggerAvailability(id, name, state);
    }*/

    public static DeviceTriggerAvailability getDeviceTriggerAvailability(InputSwitchConfig config, List<ButtonState> states) {
        String id = config.getPin();
        String name = Optional.of(config.getId()).orElse(config.getPin());

        return new DeviceTriggerAvailability(id, name, states);
    }


    public static Availability getSensorAvailability(InputConfig<?> config) {
        switch (config.getKind()) {
            case SWITCH:
                return getInputAvailability((InputSwitchConfig) config);
            case SENSOR:
            default:
                return getBinarySensorAvailability((InputSensorConfig) config);
        }

    }

    public static Availability getSensorAvailability(Lm75SensorConfig config) {
        return getTempSensorAvailability(config);

    }

    public static Availability getSensorAvailability(SensorConfig config) {
        return getTempSensorAvailability(config);

    }


    private static Availability getTempSensorAvailability(Lm75SensorConfig config) {

        return new TempSensorAvailability(config.getId(), config.getId());
    }

    private static Availability getTempSensorAvailability(SensorConfig config) {

        return new TempSensorAvailability(config.getId(), config.getId());
    }

    public static Availability getSensorAvailability(AdcSensorConfig config) {

        return new AdcSensorAvailability(config.getId(), config.getId());
    }

    public static InputAvailability getInputAvailability(InputSwitchConfig config) {
        String id = config.getPin();
        String name = Optional.of(config.getId()).orElse(config.getPin());

        InputAvailability availability = new InputAvailability(id, name);
        if (null != config.getDeviceClass()) {
            availability.deviceClass = config.getDeviceClass().toString().toLowerCase();
        }
        return availability;
    }

    private static BinarySensorAvailability getBinarySensorAvailability(InputSensorConfig config) {
        String id = config.getPin();
        String name = Optional.of(config.getId()).orElse(config.getPin());

        BinarySensorAvailability availability = new BinarySensorAvailability(id, name);
        if (null != config.getDeviceClass()) {
            availability.deviceClass = config.getDeviceClass().toString().toLowerCase();
        }
        return availability;
    }

    public static Availability getActuatorAvailability(OutputConfig config) {
        String id = config.getId().replace(" ", "");
        String name = Optional.of(config.getId()).orElse(config.getKind() + "_" + config.getPin());
        switch (config.getOutputType()) {
            case SWITCH:
                return new SwitchAvailability(id, name);
            case LIGHT:
                return new LightAvailability(id, name);
            case LED:
                return new LedAvailability(id, name);
            case NONE:
            default:
                return null;
        }
    }

    public static void setModel(String model) {
        Availability.model = model;
    }

    public static void setVersion(String version) {
        Availability.version = version;
    }

    public static Availability getModbusSensorAvailability(Register register, ModbusSensor device, String model, int registerBase) {
        ModbusSensorAvailability availability = null;
        try {
            availability = new ModbusSensorAvailability(register, device, model, registerBase);
        } catch (RuntimeException e) {
            Logger.error(e);
        }

        return availability;
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
            name = "boneIO " + topic;
            swVersion = Version.VERSION;
        }
    }

}
