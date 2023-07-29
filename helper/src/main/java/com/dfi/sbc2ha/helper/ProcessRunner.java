package com.dfi.sbc2ha.helper;

import lombok.extern.slf4j.Slf4j;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.concurrent.*;
@Slf4j
public class ProcessRunner {
    public static void runSystemCommand(ProcessBuilder builder, String errorMessage, String... args) {
        try {
            Process process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process, Logger::debug);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            Object o = future.get(10, TimeUnit.SECONDS);
            if (exitCode != 0) {
                log.error(errorMessage, exitCode, args);
            }


        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            log.error("run failed",e);
            throw new RuntimeException(e);
        }
    }
}
