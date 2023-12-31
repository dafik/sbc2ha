package com.dfi.sbc2ha.util;


import com.dfi.sbc2ha.helper.ProcessRunner;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;


//TODO check is run on beaglebone
@Slf4j
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
            log.warn("executable not found, skipping");
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
