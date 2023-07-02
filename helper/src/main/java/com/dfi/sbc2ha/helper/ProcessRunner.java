package com.dfi.sbc2ha.helper;

import org.tinylog.Logger;

import java.io.IOException;
import java.util.concurrent.*;

public class ProcessRunner {
    public static void runSystemCommand(ProcessBuilder builder, String errorMessage, String... args) {
        try {
            Process process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process, Logger::debug);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            Object o = future.get(10, TimeUnit.SECONDS);
            if (exitCode != 0) {
                Logger.error(errorMessage, exitCode, args);
            }


        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            Logger.error(e);
            throw new RuntimeException(e);
        }
    }
}
