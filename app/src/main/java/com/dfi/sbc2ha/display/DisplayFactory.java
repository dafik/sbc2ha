package com.dfi.sbc2ha.display;


import com.dfi.sbc2ha.config.sbc2ha.definition.enums.InputKindType;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.OledConfig;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputSwitchConfig;
import com.dfi.sbc2ha.helper.BoneIoBBB;
import com.dfi.sbc2ha.helper.MockHelper;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionBoardInfo;
import com.dfi.sbc2ha.helper.extensionBoard.ExtensionInputBoard;
import com.dfi.sbc2ha.helper.stats.StatsProvider;
import com.dfi.sbc2ha.sensor.SensorFactory;
import com.dfi.sbc2ha.sensor.binary.Button;
import com.dfi.sbc2ha.sensor.binary.ButtonState;
import com.diozero.api.I2CDevice;
import com.diozero.api.I2CDeviceInterface;
import com.diozero.devices.oled.SH1106;
import com.diozero.devices.oled.ShOledCommunicationChannel;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import org.tinylog.Logger;

import static com.diozero.devices.oled.SH1106.DEFAULT_I2C_ADDRESS;


public class DisplayFactory {
    public static Display createDisplay(OledConfig config, StatsProvider statsProvider) {

        NativeDeviceFactoryInterface ndf = DeviceFactoryHelper.getNativeDeviceFactory();
        if (MockHelper.isMockRun()) {
            //return mock(Display.class);
        }

        try {
            ExtensionBoardInfo boardInfo = ExtensionBoardInfo.getInstance();
            ExtensionInputBoard.InputDefinition c = boardInfo.getIn().get("C").get(1);

            InputSwitchConfig inputConfig = new InputSwitchConfig();
            inputConfig.setId("case btn");
            inputConfig.setKind(InputKindType.SWITCH);
            inputConfig.setClickDetection(com.dfi.sbc2ha.config.sbc2ha.definition.enums.ButtonState.SINGLE);

            Button btn = SensorFactory.createCaseButton(c.getHeader(),c.getPin(),c.isPullUp());

            I2CDeviceInterface i2c = new I2CDevice(BoneIoBBB.I2C_CONTROLLER, DEFAULT_I2C_ADDRESS);
            ShOledCommunicationChannel channel = new ShOledCommunicationChannel.I2cCommunicationChannel(i2c);
            SH1106 delegate = new SH1106(channel, SH1106.Height.TALL);

            Display display = new Display(delegate, btn, config, statsProvider);
            Diozero.registerForShutdown(display);

            btn.addListener(display::handleClick, ButtonState.SINGLE);
            return display;

        } catch (RuntimeException e) {
            Logger.error("Unable setup display: {}", e.getMessage());

            return null;
        }


    }


}
