package com.dfi.sbc2ha;

public class Version {
    public static final String VERSION = "java-0.0.4";
    public static String getVersion(){
        return Version.class.getPackage().getImplementationVersion();
    }
}
