package com.dfi.sbc2ha.platform.mqtt.paho;

public interface MqttListener {
    void onConnected(boolean reconnect);

    void onDisconnect();
}
