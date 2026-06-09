package iot.sbc2ha.hardware;

/**
 * Thrown when a hardware mapping operation fails — missing channel, duplicate mapping, or validation error.
 */
public final class HardwareMappingException extends RuntimeException {

    public HardwareMappingException(String message) {
        super(message);
    }
}
