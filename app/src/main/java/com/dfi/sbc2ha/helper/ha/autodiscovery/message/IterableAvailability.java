package com.dfi.sbc2ha.helper.ha.autodiscovery.message;

import com.dfi.sbc2ha.helper.ha.autodiscovery.HaDeviceType;
import com.dfi.sbc2ha.helper.ha.autodiscovery.SbcDeviceType;

import java.util.Iterator;

public abstract class IterableAvailability extends Availability implements Iterable<IterableAvailability>, Iterator<IterableAvailability> {
    public IterableAvailability(String id, String name, HaDeviceType haDeviceType, SbcDeviceType stateDeviceType) {
        super(id, name, haDeviceType, stateDeviceType);
    }

    @Override
    public abstract IterableAvailability iterator();

    @Override
    public abstract boolean hasNext();

    @Override
    public abstract IterableAvailability next();
}
