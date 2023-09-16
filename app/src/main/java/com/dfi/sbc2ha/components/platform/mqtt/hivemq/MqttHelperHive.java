package com.dfi.sbc2ha.components.platform.mqtt.hivemq;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.Availability;
import com.dfi.sbc2ha.components.platform.mqtt.MqttConfigHelper;
import com.dfi.sbc2ha.services.state.device.DeviceState;
import com.hivemq.client.mqtt.MqttClientTransportConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class MqttHelperHive {
    protected final MqttConfigHelper mqttConfigHelper;
    private final MqttConfig config;

    public MqttHelperHive(MqttConfig config, MqttConfigHelper mqttConfigHelper) {

        this.config = config;
        this.mqttConfigHelper = mqttConfigHelper;
    }

    protected Mqtt5ClientBuilder getMqtt5ClientBuilder() {
        return Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(config.getHost())
                .serverPort(config.getPort())
                .simpleAuth(getSimpleAuth(config))
                .automaticReconnectWithDefaultConfig()
                .willPublish(getLastWill(mqttConfigHelper))
                .transportConfig(MqttClientTransportConfig.builder()
                        .serverHost(config.getHost())
                        .serverPort(config.getPort())
                        .mqttConnectTimeout(10L, TimeUnit.SECONDS)
                        .socketConnectTimeout(10L, TimeUnit.SECONDS).build()
                );
    }

    protected Mqtt5ClientBuilder getMqtt5ClientBuilder(MqttClientAutoReconnect autoReconnect) {

        return Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(config.getHost())
                .serverPort(config.getPort())
                .simpleAuth(getSimpleAuth(config))
                .automaticReconnect(autoReconnect)
                .willPublish(getLastWill(mqttConfigHelper))
                .transportConfig(MqttClientTransportConfig.builder()
                        .serverHost(config.getHost())
                        .serverPort(config.getPort())
                        .mqttConnectTimeout(10L, TimeUnit.SECONDS)
                        .socketConnectTimeout(10L, TimeUnit.SECONDS).build()
                );
    }

    private Mqtt5SimpleAuth getSimpleAuth(MqttConfig config) {
        return Mqtt5SimpleAuth.builder()
                .username(config.getUsername())
                .password(config.getPassword().getBytes())
                .build();
    }

    private Mqtt5Publish getLastWill(MqttConfigHelper mqttConfigHelper) {
        return Mqtt5Publish.builder()
                .topic(mqttConfigHelper.getTopicPrefix() + "/" + Availability.STATE)
                .payload(DeviceState.OFFLINE.toString().toLowerCase().getBytes())
                .qos(MqttQos.AT_MOST_ONCE)
                .retain(true)
                .build();
    }
}
