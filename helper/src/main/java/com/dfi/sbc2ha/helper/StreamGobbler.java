package com.dfi.sbc2ha.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StreamGobbler implements Runnable {
    private final InputStream errorStream;
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    public StreamGobbler(Process process, Consumer<String> consumer) {
        inputStream = process.getInputStream();
        errorStream = process.getErrorStream();
        this.consumer = consumer;
    }

    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines()
                .filter(Predicate.not(String::isEmpty))
                .forEach(consumer);

        new BufferedReader(new InputStreamReader(errorStream)).lines()
                .filter(Predicate.not(String::isEmpty))
                .forEach(consumer);
    }
}
