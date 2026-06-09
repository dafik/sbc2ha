package iot.sbc2ha.hardware.profile;

/**
 * Thrown when a profile loading or expansion operation fails — unknown profile,
 * parse error, or resource not found.
 */
public final class ProfileLoadingException extends RuntimeException {

    public ProfileLoadingException(String message) {
        super(message);
    }

    public ProfileLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
