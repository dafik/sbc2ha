package com.dfi.sbc2ha.components.platform.mqtt;

import com.dfi.sbc2ha.Version;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.Availability;
import com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.IterableAvailability;
import com.dfi.sbc2ha.services.manager.Manager;
import com.dfi.sbc2ha.components.platform.mqtt.hivemq.MqttHelperHiveRx;
import com.dfi.sbc2ha.services.state.device.DeviceState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.functions.BiConsumer;
import lombok.extern.slf4j.Slf4j;

import static com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.Availability.CONFIG;
import static com.dfi.sbc2ha.components.platform.ha.autodiscovery.message.Availability.formatTopic;


@Slf4j
public class MqttPlatform {

    private final Manager manager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean enabled = true;
    private MqttConfig mqttConfig;
    private MqttConfigHelper mqttConfigHelper;
    private MqttHelperHiveRx mqttHelper;

    public MqttPlatform(Manager manager, MqttConfig mqttConfig) {
        this.manager = manager;
        if (mqttConfig == null) {
            enabled = false;
        } else {
            this.mqttConfig = mqttConfig;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void start() {
        if (!enabled) {
            return;
        }
        setupMqtt();
        prepareAutodiscovery();
    }

    private void setupMqtt() {

        log.debug("Setup mqtt");
        manager.addDisplayLine("setup mqtt");
        mqttConfigHelper = new MqttConfigHelper(mqttConfig);

        mqttHelper = new MqttHelperHiveRx(mqttConfig, mqttConfigHelper);
        //mqttHelper = new MqttHelperPaho(mqtt, mqttConfigHelper);

        manager.addDisplayLine("start mqtt");
        mqttHelper.start();
    }

    private void prepareAutodiscovery() {

        if (mqttConfig.getHaDiscovery().isEnabled()) {
            manager.addDisplayLine("prepare autodiscovery");
            Availability.setTopicPrefix(mqttConfig.getTopicPrefix());
            Availability.setModel("boneIO Relay Board");
            Availability.setVersion(Version.VERSION);
        }
    }

    public void waitForMqttReadyForPublish() {
        if (!enabled) {
            return;
        }
        while (!mqttHelper.isReadyForPublish()) {
            manager.addDisplayLine("wait for mqtt ready");
            log.debug("Wait for mqtt ready");
            try {
                //noinspection BusyWait
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendStatus(DeviceState deviceState, String device) {
        if (!enabled) {
            return;
        }
        log.debug("Sending {} state device: {}", deviceState, device);
        String topic = formatTopic(mqttConfigHelper.getTopicPrefix(), device + Availability.STATE);
        String payload = deviceState.name().toLowerCase();
        mqttHelper.publish(topic, payload.getBytes(), true);
    }

    public void sendStatus(DeviceState deviceState) {
        if (!enabled) {
            return;
        }
        log.debug("Sending {} state", deviceState);
        String topic = mqttConfigHelper.getSelfStateTopic();
        String payload = deviceState.name().toLowerCase();

        mqttHelper.publish(topic, payload.getBytes(), true);
    }

    public void subscribeHaCommands(BiConsumer<String, byte[]> handle) {

        manager.addDisplayLine("subscribe ha events");
        mqttHelper.createSubscription(mqttConfigHelper.getCommandTopic(), handle);
    }

    public void unSubscribeHaCommands() {
        if (enabled && mqttHelper != null) {
            manager.addDisplayLine("unsubscribe ha events");
            mqttHelper.removeSubscription(mqttConfigHelper.getCommandTopic());
        }
    }

    public void subscribeHaDiscovery() {
        manager.addDisplayLine("subscribe ha discovery");
        mqttHelper.createSubscription(mqttConfigHelper.getHaDiscoveryTopic(), this::handleHaDiscovery);
    }

    public void unSubscribeHaDiscovery() {
        if (enabled && mqttHelper != null && mqttConfigHelper.isHaDiscovery()) {
            manager.addDisplayLine("unsubscribe ha discovery");
            mqttHelper.removeSubscription(mqttConfigHelper.getHaDiscoveryTopic());
        }
    }


    private void handleHaDiscovery(String topic, byte[] payload) {
        log.trace("Received autodiscovery topic:  {}", topic);
        if (payload.length == 0) {
            return;
        }

        if (!mqttConfigHelper.getAutodiscovery().contains(topic)) {
            log.debug("removing: {}", topic);
            mqttHelper.publish(topic, null, true);
        }
    }

    public void subscribeSelfState(BiConsumer<String, byte[]> handle) {
        manager.addDisplayLine("subscribe ha discovery");
        mqttHelper.createSubscription(mqttConfigHelper.getSelfStateTopic(), handle);
    }

    public void unSubscribeSelfState() {
        if (enabled && mqttHelper != null && mqttConfigHelper.isHaDiscovery()) {
            manager.addDisplayLine("unsubscribe ha discovery");
            mqttHelper.removeSubscription(mqttConfigHelper.getSelfStateTopic());
        }
    }

    public void sendHaAutodiscovery(Availability availability) {
        if (mqttConfig == null || !mqttConfig.getHaDiscovery().isEnabled()) {
            return;
        }
        if (availability instanceof IterableAvailability) {
            for (var avail : ((IterableAvailability) availability)) {
                sendHaAutodiscoveryInternal(avail);
            }
        } else {
            sendHaAutodiscoveryInternal(availability);
        }
    }

    private void sendHaAutodiscoveryInternal(Availability availability) {
        var topicPrefix = availability.getTopicPrefix();
        String payload;
        try {
            payload = objectMapper.writeValueAsString(availability);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        var topic = Availability.formatTopic(
                mqttConfigHelper.getHaDiscoveryPrefix(),
                availability.getHaDeviceTypeName(),
                topicPrefix,
                availability.getNodeName(),
                CONFIG
        );
        log.info("Sending HA discovery for {}, id:{} : {}", availability.getHaDeviceType(), availability.getId(), availability.getName());
        mqttConfigHelper.addAutodiscovery(topic);

        mqttHelper.publish(topic, payload.getBytes());
    }


    public void publish(String topic, byte[] payload) {
        publish(topic, payload, true);

    }

    public void publish(String topic, byte[] payload, boolean retain) {
        mqttHelper.publish(topic, payload, retain);
    }

    public void close() {
        unSubscribeHaDiscovery();
        unSubscribeSelfState();
        unSubscribeHaCommands();

        if (enabled && mqttHelper != null) {
            mqttHelper.stop();
        }
    }
}
