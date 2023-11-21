package com.dfi.sbc2ha;

public class Version {
    private static final String VERSION = "java-0.0.7";

    public static String getVersion() {
        String implementationVersion = Version.class.getPackage().getImplementationVersion();
        if (implementationVersion == null) {
            return VERSION;
        }
        return implementationVersion;
    }
}
