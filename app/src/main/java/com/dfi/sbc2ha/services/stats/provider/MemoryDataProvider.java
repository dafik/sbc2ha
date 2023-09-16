package com.dfi.sbc2ha.services.stats.provider;

import lombok.extern.slf4j.Slf4j;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MemoryDataProvider extends DataProvider {
    private GlobalMemory memory;
    private String total = "-1";
    private String avail = "-1";
    private String inUse = "-1";

    public MemoryDataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        super(hal, initialDelay, period, unit);

    }

    @Override
    public void run() {
        try {
            if (memory == null) {
                memory = hal.getMemory();
            }
            total = FormatUtil.formatBytes(memory.getTotal());
            avail = FormatUtil.formatBytes(memory.getAvailable());
            inUse = FormatUtil.formatBytes(memory.getTotal() - memory.getAvailable());

        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        onChange();
    }

    public List<String> getLines() {
        return List.of(
                "memory",
                "total: " + total,
                "avail: " + avail,
                "in use: " + inUse
        );
    }

}
