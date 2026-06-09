package iot.sbc2ha.config;

/**
 * Thrown when a configuration file fails validation (missing fields,
 * invalid stable IDs, unknown schema version, etc.).
 */
public final class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
