package com.dfi.sbc2ha.helper.stats.provider;

import com.diozero.util.SleepUtil;
import lombok.extern.slf4j.Slf4j;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CpuDataProvider extends DataProvider {

    private CentralProcessor cpu;
    private double[] average = {-1, -1, 1};
    private double[] current = {-1};

    public CpuDataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        super(hal, initialDelay, period, unit);

    }

    @Override
    public void run() {
        try {
            if (cpu == null) {
                cpu = hal.getProcessor();
            }
            long[][] startTick = cpu.getProcessorCpuLoadTicks();
            average = cpu.getSystemLoadAverage(3);
            SleepUtil.sleepMillis(100);

            current = cpu.getProcessorCpuLoadBetweenTicks(startTick);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        onChange();
    }

    public List<String> getLines() {
        return List.of(
                "cpu",
                "1m: " + average[0],
                "5m: " + average[1],
                "15m: " + average[2],
                "current: " + current[0] * 100 + "%"
/*            "total":f "{int(100 - cpu.idle)}%",
                    "user":f "{cpu.user}%",
                    "system":f "{cpu.system}%",*/
        );
    }

}
