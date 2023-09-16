package com.diozero.devices.fake;

import com.diozero.api.PinInfo;
import com.diozero.sbc.BoardInfo;
import com.diozero.sbc.LocalSystemInfo;

public class FakeBoardInfo extends BoardInfo {
    public static final String FAKE_HEADER = "FAKE";
    public static final float ADC_VREF = 1.8f;

    public FakeBoardInfo(LocalSystemInfo systemInfo) {
        super("Fake",
                systemInfo.getModel(), systemInfo.getMemoryKb() == null ? -1 : systemInfo.getMemoryKb().intValue(),
                systemInfo.getOperatingSystemId(), systemInfo.getOperatingSystemVersion());
    }

    @Override
    public void populateBoardPinInfo() {
        addGpioPinInfo(FAKE_HEADER, 1, 1, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 2, 2, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 3, 3, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 4, 4, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 5, 5, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 6, 6, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 7, 7, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 8, 8, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 9, 9, PinInfo.DIGITAL_IN);
        addGpioPinInfo(FAKE_HEADER, 10, 10, PinInfo.DIGITAL_IN);

        addGpioPinInfo(FAKE_HEADER, 11, 11, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 12, 12, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 13, 13, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 14, 14, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 15, 15, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 16, 16, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 17, 17, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 18, 18, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 19, 19, PinInfo.DIGITAL_OUT);
        addGpioPinInfo(FAKE_HEADER, 20, 20, PinInfo.DIGITAL_OUT);

        addAdcPinInfo(FAKE_HEADER, 0, "AIN0", 21, ADC_VREF);
        addAdcPinInfo(FAKE_HEADER, 1, "AIN1", 22, ADC_VREF);
        addAdcPinInfo(FAKE_HEADER, 2, "AIN2", 23, ADC_VREF);
        addAdcPinInfo(FAKE_HEADER, 3, "AIN3", 24, ADC_VREF);
        addAdcPinInfo(FAKE_HEADER, 4, "AIN4", 25, ADC_VREF);

        addPwmPinInfo(FAKE_HEADER, PinInfo.NOT_DEFINED, "PWM1", 26, 0, 0, PinInfo.DIGITAL_IN_OUT_PWM);
        addPwmPinInfo(FAKE_HEADER, PinInfo.NOT_DEFINED, "PWM2", 27, 0, 0, PinInfo.DIGITAL_IN_OUT_PWM);
        addPwmPinInfo(FAKE_HEADER, PinInfo.NOT_DEFINED, "PWM3", 28, 0, 0, PinInfo.DIGITAL_IN_OUT_PWM);
        addPwmPinInfo(FAKE_HEADER, PinInfo.NOT_DEFINED, "PWM4", 29, 0, 0, PinInfo.DIGITAL_IN_OUT_PWM);
        addPwmPinInfo(FAKE_HEADER, PinInfo.NOT_DEFINED, "PWM5", 30, 0, 0, PinInfo.DIGITAL_IN_OUT_PWM);
    }
}
