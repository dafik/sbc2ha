package iot.sbc2ha.runtime;

/**
 * Types of switch/button events that can be detected.
 *
 * <p>These represent the full vocabulary of press/release events.
 * The detection algorithm is pluggable — the enum itself is stable.</p>
 *
 * <pre>
 * click      — single press followed by release
 * double     — two rapid presses in succession
 * long       — press held beyond the long-press threshold
 * release    — release after any press (fired independently of click/long)
 * </pre>
 */
public enum EventType {
    /** Single click: press + release within the debounce window. */
    CLICK,
    /** Double click: two presses within the double-click window. */
    DOUBLE,
    /** Long press: held press exceeding the long-press threshold. */
    LONG,
    /** Release: the moment the switch is released. */
    RELEASE
}
