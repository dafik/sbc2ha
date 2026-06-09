package iot.sbc2ha.config;

/**
 * Bus specification — nested under {@code hardware.buses.<id>:}.
 */
public final class BusConfig {

    private String type;
    private String path;

    public BusConfig() {
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String path() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
