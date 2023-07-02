package com.dfi.sbc2ha.helper.stats.provider;

import org.tinylog.Logger;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UptimeDataProvider extends DataProvider<List<String>> {

    private String hostName = "reading..";
    private String systemUptime = "reading..";

    public UptimeDataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        super(hal, initialDelay, period, unit);
    }

    private static String getSystemUptime() throws Exception {
        String uptime = "";
        String os = System.getProperty("os.name").toLowerCase();

        Process uptimeProc = Runtime.getRuntime().exec("uptime");
        BufferedReader in = new BufferedReader(new InputStreamReader(uptimeProc.getInputStream()));
        String line = in.readLine();
        if (line != null) {
            Pattern parse = Pattern.compile("((\\d+) days,)? (\\d+):(\\d+)");
            Matcher matcher = parse.matcher(line);
            if (matcher.find()) {
                String _days = matcher.group(2);
                String _hours = matcher.group(3);
                String _minutes = matcher.group(4);
                int days = _days != null ? Integer.parseInt(_days) : 0;
                int hours = _hours != null ? Integer.parseInt(_hours) : 0;
                int minutes = _minutes != null ? Integer.parseInt(_minutes) : 0;
                uptime = days + "d" + hours + "h" + minutes + "m";
            }
        }

        return uptime;
    }

    @Override
    public void run() {
        var lastHostname = hostName;
        var lastUptime = systemUptime;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            systemUptime = getSystemUptime();
        } catch (Exception e) {
            //throw new RuntimeException(e);
            Logger.error(e);
        }
        if (!Objects.equals(lastHostname, hostName) || !Objects.equals(lastUptime, systemUptime)) {
            onChange();
        }
    }

    @Override
    public List<String> getLines() {
        return List.of(
                "host: " + hostName,
                "uptime: " + systemUptime
        );
    }


}
