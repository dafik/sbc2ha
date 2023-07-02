package com.dfi.sbc2ha.helper;

import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;

public class MockHelper {
    private static boolean isMockRun = false;
    private static boolean isChecked = false;

    public static boolean isMockRun() {
        if (!isChecked) {
            checkIsMockRun();
        }
        return isMockRun;
    }

    private static void checkIsMockRun() {
        NativeDeviceFactoryInterface ndf = DeviceFactoryHelper.getNativeDeviceFactory();
        if (ndf.getClass().getSimpleName().equals("MockDeviceFactory")) {
            isMockRun = true;
        }
        isChecked = true;
    }
}
