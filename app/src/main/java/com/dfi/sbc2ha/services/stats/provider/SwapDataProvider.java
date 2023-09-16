package com.dfi.sbc2ha.services.stats.provider;

import lombok.extern.slf4j.Slf4j;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.util.FormatUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SwapDataProvider extends DataProvider {

    private GlobalMemory memory;
    private String used = "reading..";
    private String avail = "reading..";
    private String vmInUse = "reading..";
    private String vmMax = "reading..";

    public SwapDataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        super(hal, initialDelay, period, unit);

    }

    @Override
    public void run() {
        try {
            if (null == memory) {
                memory = hal.getMemory();
            }
            VirtualMemory virtualMemory = memory.getVirtualMemory();

            used = FormatUtil.formatBytes(virtualMemory.getSwapUsed());
            avail = FormatUtil.formatBytes(virtualMemory.getSwapTotal());
            vmInUse = FormatUtil.formatBytes(virtualMemory.getVirtualInUse());
            vmMax = FormatUtil.formatBytes(virtualMemory.getVirtualMax());

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        onChange();
    }


    public List<String> getLines() {
        GlobalMemory memory = hal.getMemory();
        VirtualMemory virtualMemory = memory.getVirtualMemory();
        return List.of(
                "swap",
                "used: " + used,
                "avail: " + avail,
                "vm in use: " + vmInUse,
                "vm max: " + vmMax
        );
    }


}
