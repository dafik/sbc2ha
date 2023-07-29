package com.dfi.sbc2ha.platform.mqtt.paho;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.helper.ha.autodiscovery.message.Availability;
import com.dfi.sbc2ha.platform.mqtt.MqttConfigHelper;
import com.dfi.sbc2ha.platform.mqtt.MqttHelper;
import com.dfi.sbc2ha.manager.ManagerExecutor;
import com.dfi.sbc2ha.state.device.DeviceState;
import io.reactivex.functions.BiConsumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
@Slf4j
public class MqttHelperPaho implements MqttHelper, MqttCallback, Runnable {

    protected final MqttConfigHelper mqttConfigHelper;
    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private final MqttConfig config;
    private final ExecutorService executor;
    private MqttClient client;
    private boolean connected = false;
    private Future<?> queueFeature;

    private final Map<String, Future<?>> subscription = new HashMap<>();


    public MqttHelperPaho(MqttConfig config, MqttConfigHelper mqttConfigHelper) {
        this.config = config;
        this.mqttConfigHelper = mqttConfigHelper;

        ManagerExecutor.ManagerThreadFactory threadFactory = new ManagerExecutor.ManagerThreadFactory();
        executor = Executors.newFixedThreadPool(3, threadFactory);

        creteClient();
    }

    protected void creteClient() {
        String broker = "tcp://" + config.getHost() + ":" + config.getPort();
        String clientId = UUID.randomUUID().toString();
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            client = new MqttClient(broker, clientId, persistence);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        queueFeature = executor.submit(this);
    }

    @Override
    public void run() {
        if (!client.isConnected()) {
            // MQTT connection option
            MqttConnectionOptions opts = new MqttConnectionOptions();
            opts.setUserName(config.getUsername());
            opts.setPassword(config.getPassword().getBytes());
            // retain session
            opts.setCleanStart(true);
            opts.setAutomaticReconnect(true);
            opts.setConnectionTimeout(60);
            opts.setKeepAliveInterval(60);
            opts.setReceiveMaximum(100);


            String willTopic = mqttConfigHelper.getTopicPrefix() + "/" + Availability.STATE;
            MqttMessage m = new MqttMessage(DeviceState.OFFLINE.toString().toLowerCase().getBytes(), 0, false, new MqttProperties());
            opts.setWill(willTopic, m);

            client.setCallback(this);

            try {
                client.connect(opts);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }


            try {
                while (!queueFeature.isDone()) {
                    Object item = queue.take();
                    if (item instanceof MqttPublish) {
                        publishInternal((MqttPublish) item);
                    }
                    if (item instanceof MqttSubscribe) {
                        Future<?> submit = executor.submit(new MqttSubscriber((MqttSubscribe) item, client));
                        subscription.put(((MqttSubscribe) item).topic, submit);
                    }
                    log.info("queue length: {}", queue.size());
                }
                log.info("queue exit");

            } catch (InterruptedException e) {
                log.info("on interrupt");
            }

        }
    }

    private void publishInternal(MqttPublish publish) {
        log.info("publish {}", publish.getTopic());
        try {
            client.publish(publish.getTopic(), publish.getPayload(), 1, publish.isRetain());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void stop() {

    }


    @Override
    public void publish(String topic, byte[] payload, boolean retain) {
        try {
            queue.put(new MqttPublish(topic, payload, retain));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void createSubscription(String topic, BiConsumer<String, byte[]> handle) {
        try {
            queue.put(new MqttSubscribe(topic, handle));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSubscription(String topic) {
        if (subscription.containsKey(topic)) {
            subscription.get(topic).cancel(true);
        }
    }

    public void subscribeInternal(MqttSubscribe subscribe) {
        log.info("subscribe {}", subscribe.getTopic());
        try {
            client.subscribe(subscribe.getTopic(), 2, (t, p) -> subscribe.getHandle().accept(t, p.getPayload()));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean isReadyForPublish() {
        return connected;
    }


    //paho


    @Override
    public void deliveryComplete(IMqttToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("connected");
        connected = true;
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {

    }


    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        log.warn("disconnected");
        connected = false;
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {

    }

    @Data
    @AllArgsConstructor
    private static class MqttPublish {
        String topic;
        byte[] payload;
        boolean retain;
    }

    @Data
    @AllArgsConstructor
    private static class MqttSubscribe {
        String topic;
        BiConsumer<String, byte[]> handle;
    }

    @Data
    private static class MqttSubscriber implements Runnable {
        final MqttSubscribe subscribe;
        final MqttClient client;

        @Override
        public void run() {
            log.info("subscribe {}", subscribe.getTopic());
            try {
                client.subscribe(subscribe.getTopic(), 1, (t, p) -> subscribe.getHandle().accept(t, p.getPayload()));
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
