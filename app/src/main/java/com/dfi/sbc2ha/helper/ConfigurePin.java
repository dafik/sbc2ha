package com.dfi.sbc2ha.helper;


import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

//TODO check is run on beaglebone
public class ConfigurePin {
    public static void configure(PinInfo pin, GpioPullUpDown pud) {
        if (pud == GpioPullUpDown.NONE) {
            return;
        }
        String mode = GpioPullUpDown.PULL_UP == pud ? "gpio_pu" : "gpio_pd";

        configure(pin, mode);
    }

    public static void configureUart(PinInfo pin) {
        String mode = "uart";
        configure(pin, mode);
    }

    private static void configure(PinInfo pin, String mode) {
        ProcessBuilder builder = new ProcessBuilder();
        String executable = "/usr/bin/config-pin";
        File file = new File(executable);
        if (!file.exists()) {
            Logger.warn("executable not found, skipping");
            return;
        }

        builder.command(List.of(
                executable,
                pin.getHeader() + "." + pin.getPhysicalPin(),
                mode
        ));
        builder.directory(new File(System.getProperty("user.home")));

        ProcessRunner.runSystemCommand(builder, "configure pin failed");
    }
}
