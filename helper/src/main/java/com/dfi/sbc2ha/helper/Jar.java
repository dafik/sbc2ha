package com.dfi.sbc2ha.helper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Jar {
    private static boolean isRunInJar = false;
    private static boolean isChecked = false;

    public static boolean isRunInJar() {
        if (!isChecked) {
            checkIsRunningInJar();
        }
        return isRunInJar;
    }

    private static void checkIsRunningInJar() {
        URL resource = Jar.class.getResource(Jar.class.getSimpleName() + ".class");
        assert resource != null;
        String protocol = resource.getProtocol();
        if (Objects.equals(protocol, "jar")) {
            isRunInJar = true;// run in jar
        } else if (Objects.equals(protocol, "file")) {
            isRunInJar = false;
            // run in ide
        }
        isChecked = true;
    }


    public static String getTempDir() {
        String dir;
        if (isRunInJar()) {
            dir = System.getProperty("user.dir");
        } else {
            dir = "/tmp/sbc2ha";

        }
        try {
            Files.createDirectories(Paths.get(dir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dir;

    }


}
