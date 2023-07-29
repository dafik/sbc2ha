package com.dfi.sbc2ha.actuator;

import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;

public class TextField extends Actuator {
    protected final VoidDevice delegate;

    public TextField(String name, int id) {
        super( name, id);
        this.delegate = new VoidDevice();
    }

    public static class VoidDevice implements DeviceInterface {

        @Override
        public void close() throws RuntimeIOException {

        }
    }

}
