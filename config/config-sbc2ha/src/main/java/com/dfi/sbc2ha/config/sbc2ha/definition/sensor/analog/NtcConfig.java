package com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.SingleSourceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NtcConfig extends SingleSourceConfig {

    /**
     * The calibration parameters of the sensor
     */
    @JsonProperty("b_calibration")
    BconstantCalibration bCalibration;

    @JsonProperty("v_calibration")
    ValueCalibration vCalibration;

    public NtcConfig(String sensor) {
        super(sensor);
        platform = PlatformType.NTC;
    }

    public NtcConfig() {
        super();
        platform = PlatformType.NTC;
    }

    @Data
    public static class BconstantCalibration {
        /*b_constant: 3950
        reference_temperature: 25°C
        reference_resistance: 10kOhm*/

        @JsonProperty("b_constant")
        int bConstant;
        @JsonProperty("reference_temperature")
        String referenceTemperature;
        @JsonProperty("reference_resistance")
        String referenceResistance;
    }

    @Data
    public static class ValueCalibration {
        SetPoint low;
        SetPoint mid;
        SetPoint high;
        /*- 10.0kOhm -> 25°C
        - 27.219kOhm -> 0°C
        - 14.674kOhm -> 15°C*/
    }

    @Data
    public static class SetPoint {
        String temperature;
        String resistance;
    }

}
