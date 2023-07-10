package com.dfi.sbc2ha.helper.mqtt.paho;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.helper.Constants;
import com.dfi.sbc2ha.helper.mqtt.MqttConfigHelper;
import com.dfi.sbc2ha.helper.mqtt.MqttHelper;
import com.dfi.sbc2ha.manager.Manager;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.tinylog.Logger;

import java.util.UUID;

public class MqttHelperPaho implements MqttHelper, MqttCallback {
    protected final MqttConfigHelper mqttConfigHelper;
    private final MqttConfig config;
    private MqttClient client;
    private boolean connected = false;
    private IMqttToken connectWithResult;
    private MqttListener connectedListener;


    public MqttHelperPaho(MqttConfig config, MqttConfigHelper mqttConfigHelper) {

        this.config = config;
        this.mqttConfigHelper = mqttConfigHelper;


        creteClient();


/*        try {
            MqttClient client = new MqttClient(broker, clientId, persistence);


            // establish a connection
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);

            System.out.println("Connected");
            System.out.println("Publishing message: " + content);

            // Subscribe
            client.subscribe(subTopic);

            // Required parameters for message publishing
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            client.publish(pubTopic, message);
            System.out.println("Message published");

            client.disconnect();
            System.out.println("Disconnected");
            client.close();
            System.exit(0);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }*/


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


            String willTopic = mqttConfigHelper.getTopicPrefix() + "/" + Constants.STATE;
            MqttMessage message = new MqttMessage(Constants.OFFLINE.getBytes());
            opts.setWill(willTopic, message);

            try {
                connectWithResult = client.connectWithResult(opts);
                client.setCallback(this);

                client.setCallback(this);


            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void publish(String topic, byte[] payload) {
        //new MqttSubscription()
        //client.subscribe()
    }

    @Override
    public void publish(String topic, byte[] payload, boolean retain) {

    }

    @Override
    public Flowable<Mqtt5Publish> subscribeTopic(String topic) {
        return null;
    }

    @Override
    public void createSubscription(String haDiscoveryTopic, Consumer<? super Mqtt5Publish> handle) {

    }

    @Override
    public Observable<Mqtt5Publish> getSubscriptions() {
        return null;
    }

    @Override
    public boolean isReadyForPublish() {
        return false;
    }

    @Override
    public void setConnectedListener(Manager connectedListener) {
        this.connectedListener = connectedListener;
    }


    //paho


    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        connected = false;
        if(connectedListener!=null){
            connectedListener.onDisconnect();
        }
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        Logger.error("mqtt error");
        Logger.error(exception);

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttToken token) {

    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        connected = true;
        if (connectedListener != null) {
            connectedListener.onConnected(reconnect);
        }
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {

    }
}
