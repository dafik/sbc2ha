package com.dfi.sbc2ha.bus;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.diozero.api.DeviceInterface;
import com.diozero.api.RuntimeIOException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class Bus<B extends DeviceInterface> implements DeviceInterface {
    PlatformType platformType;
    B bus;
    String id;

    @Override
    public void close() throws RuntimeIOException {
        bus.close();
    }
}
