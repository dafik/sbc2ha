package com.dfi.sbc2ha.exception;

public class MissingConfigException extends Exception {
    public MissingConfigException(String message) {
        super(message);
    }

    public MissingConfigException(Exception e) {
        super(e);
    }
}
