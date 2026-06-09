package iot.sbc2ha.runtime;

/**
 * Actions that can be dispatched to target devices.
 *
 * <p>Early actions cover output control; mqtt/remote/noop are reserved for future phases.</p>
 */
public enum ActionType {
    /** Turn the target device on. */
    OUTPUT_ON,
    /** Turn the target device off. */
    OUTPUT_OFF,
    /** Toggle the target device state (on ↔ off). */
    OUTPUT_TOGGLE,
    /** No-op action (useful as a default or placeholder). */
    NOOP
}
