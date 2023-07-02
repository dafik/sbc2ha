package com.dfi.sbc2ha.helper.stats.provider;

import org.tinylog.Logger;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiskDataProvider extends DataProvider<List<String>> {

    private List<HWDiskStore> diskStore;

    public DiskDataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        super(hal, initialDelay, period, unit);

    }


    @Override
    public void run() {
        try {
            if (diskStore == null) {
                diskStore = hal.getDiskStores();
            }
            HWDiskStore hwDiskStore = diskStore.get(0);
            List<HWPartition> partitions = hwDiskStore.getPartitions();
        } catch (Exception e) {
            Logger.error(e);
        }
        onChange();
    }


    public List<String> getLines() {
        return List.of(
                "disk"
/*            "total":f "{int(100 - cpu.idle)}%",
                    "user":f "{cpu.user}%",
                    "system":f "{cpu.system}%",*/
        );
    }

}
