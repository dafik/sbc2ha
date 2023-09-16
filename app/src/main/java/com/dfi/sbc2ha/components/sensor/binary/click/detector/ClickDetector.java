package com.dfi.sbc2ha.components.sensor.binary.click.detector;

import com.dfi.sbc2ha.components.sensor.Sensor;

public interface ClickDetector {

    void detect(Long time, Sensor.Direction direction);

}
