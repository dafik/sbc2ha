package iot.sbc2ha.boot;

/**
 * Boot lifecycle states.
 *
 * <p>Transitions follow the reference document {@code .plan/reference/06-BOOT-LIFECYCLE.md}.
 * Not every state is reached on every run — MQTT/HA states are skipped when those
 * optional layers are not configured.</p>
 *
 * @see Lifecycle
 */
public enum LifecycleState {

    /** Early startup before any configuration is loaded. */
    BOOTING,

    /** Configuration YAML parsed and validated successfully. */
    CONFIG_LOADED,

    /** Configuration validation failed — distinct from a generic runtime ERROR. */
    CONFIG_ERROR,

    /** {@code StateService} persisted states restored onto the action engine. */
    STATE_RESTORED,

    /** Minimal hardware layer is ready (real GPIO/I2C adapters initialised). */
    HARDWARE_MINIMAL_READY,

    /** Local input-to-output path is operational without optional layers. */
    OFFLINE_READY,

    /** Attempting to connect to the MQTT broker. */
    MQTT_CONNECTING,

    /** Connected to the MQTT broker. */
    MQTT_CONNECTED,

    /** Sending Home Assistant discovery messages. */
    HA_DISCOVERY_RUNNING,

    /** Home Assistant discovery messages published. */
    HA_DISCOVERY_DONE,

    /** All configured layers (MQTT, HA, WebSocket) are operational. */
    FULL_READY,

    /** One or more non-critical layers are unavailable or degraded. */
    DEGRADED,

    /** Generic runtime error — anything that is not a CONFIG_ERROR. */
    ERROR
}
