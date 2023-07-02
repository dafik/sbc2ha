package com.diozero.devices;

/*-
 * #%L
 * Organisation: diozero
 * Project:      diozero - Core
 * Filename:     W1ThermSensor.java
 *
 * This file is part of the diozero project. More information about this project
 * can be found at https://www.diozero.com/.
 * %%
 * Copyright (C) 2016 - 2023 diozero
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import com.diozero.devices.oneWire.OneWireThermSensor;
import com.diozero.devices.oneWire.bus.OneWireThermDevice;
import com.diozero.devices.oneWire.bus.fs.FsBus;

import java.util.List;
import java.util.stream.Collectors;


public class W1ThermSensor extends OneWireThermSensor {

    public W1ThermSensor(OneWireThermDevice device) {
        super(device);

    }

    /**
     * Compatibility with previous implementation without bus
     *
     * @deprecated use  OneWireBus.getAvailableSensors()
     */
    @Deprecated
    public static List<W1ThermSensor> getAvailableSensors() {
        return getAvailableSensors(FsBus.BASE_DIRECTORY);
    }

    /**
     * Compatibility with previous implementation without bus
     *
     * @deprecated use  OneWireFsBus(folder).getAvailableSensors()
     */
    @Deprecated
    public static List<W1ThermSensor> getAvailableSensors(String folder) {
        return FsBus.getAvailableSensors(folder).stream()
                .map(s -> (W1ThermSensor) s)
                .collect(Collectors.toList());
    }


}
