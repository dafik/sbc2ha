package com.dfi.sbc2ha.helper.mqtt.paho;

public interface MqttListener {
    void onConnected(boolean reconnect);

    void onDisconnect();
}
