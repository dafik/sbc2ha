package com.dfi.sbc2ha.services.stats.provider;

import lombok.extern.slf4j.Slf4j;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Slf4j
public class NetworkDataProvider extends DataProvider {

    private List<NetworkIF> networkIFs;
    private String ip = "reading..";
    private String mask = "reading..";
    private String macAddress = "reading..";
    private String name;

    public NetworkDataProvider(HardwareAbstractionLayer hal, long initialDelay, long period, TimeUnit unit) {
        super(hal, initialDelay, period, unit);

    }


    @Override
    public void run() {
        try {
            if (null == networkIFs) {
                networkIFs = hal.getNetworkIFs();
            }

            try {
                Pattern pattern = Pattern.compile("^vpn.+|^br.+|^docker.+|^lo");
                List<String> niNames = NetworkInterface.networkInterfaces().map(NetworkInterface::getName)
                        .filter(n -> !pattern.matcher(n).matches())
                        .collect(Collectors.toList());

                String defaultNiName = "eth0";
                name = niNames.contains(defaultNiName) ? defaultNiName : niNames.get(0);
                NetworkInterface ni = NetworkInterface.getByName(name);
                byte[] hardwareAddress = ni.getHardwareAddress();
                InterfaceAddress iface = ni.getInterfaceAddresses().stream()
                        .filter(in -> in.getNetworkPrefixLength() <= 32)
                        .findFirst().orElseThrow();

                String[] hexadecimal = new String[hardwareAddress.length];
                for (int i = 0; i < hardwareAddress.length; i++) {
                    hexadecimal[i] = String.format("%02X", hardwareAddress[i]);
                }
                macAddress = String.join(":", hexadecimal);
                ip = iface.getAddress().toString().replace("/", "");
                mask = iface.getBroadcast().toString().replace("/", "");


            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        onChange();

    }


    public List<String> getLines() {
        return List.of(
                "network: " + name,
                "ip: " + ip,
                "mask: " + mask,
                "mac: " + macAddress
        );
    }


}
