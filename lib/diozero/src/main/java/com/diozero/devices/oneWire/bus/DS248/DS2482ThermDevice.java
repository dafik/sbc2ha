package com.diozero.devices.oneWire.bus.DS248;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DS2482ThermDevice extends DS2482Device implements OneWireThermDevice {

    public DS2482ThermDevice(TemperatureContainer container) {
        super(container);
    }

    public void setTemperatureResolution(double resolution) throws OneWireException {

        double[] allowed = getContainer().getTemperatureResolutions();
        if (Arrays.stream(allowed).noneMatch(r1 -> r1 == resolution)) {
            throw new IllegalArgumentException("resolution " + resolution + "bit not in valid range " + Arrays.stream(allowed).boxed()
                    .map(Object::toString)
                    .collect(Collectors.joining(","))
            );
        }
        initializeIfNeeded();
        TemperatureContainer container = getContainer();
        container.setTemperatureResolution(resolution, state);
        container.writeDevice(state);
    }

    @Override
    public float getTemperature() throws RuntimeIOException {
        try {
            initializeIfNeeded();
            TemperatureContainer container = getContainer();
            container.doTemperatureConvert(state);

            return (float) container.getTemperature(state);
        } catch (OneWireException e) {
            throw new RuntimeException(e);
        }
    }

    private TemperatureContainer getContainer() {
        return (TemperatureContainer) this.sensor;
    }
}
