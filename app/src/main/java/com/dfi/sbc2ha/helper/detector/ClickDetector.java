package com.dfi.sbc2ha.helper.detector;

import com.dfi.sbc2ha.sensor.Sensor;

public interface ClickDetector {

    void detect(Long time, Sensor.Direction direction);

}
