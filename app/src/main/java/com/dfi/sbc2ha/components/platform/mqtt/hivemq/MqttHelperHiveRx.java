package com.dfi.sbc2ha.components.platform.mqtt.hivemq;

import com.dfi.sbc2ha.components.platform.mqtt.MqttConfigHelper;
import com.dfi.sbc2ha.components.platform.mqtt.MqttHelper;
import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqttHelperHiveRx extends MqttHelperHive implements MqttHelper {

    private final Subject<Mqtt5Publish> messagesSubject = PublishSubject.create();
    private final Mqtt5RxClient client;
    private final Flowable<Mqtt5PublishResult> publishScenario;
    private final List<MqttTopicFilter> subscribedTopics = new ArrayList<>();
    private final Map<MqttTopicFilter, Disposable> subscriptions = new HashMap<>();
    private Disposable publish;
    private Disposable connected;
    private boolean readyForPublish = false;

    public MqttHelperHiveRx(MqttConfig config, MqttConfigHelper mqttConfigHelper) {
        super(config, mqttConfigHelper);

        //messagesSubject.doOnSubscribe(d -> log.trace("message subject subscribed"));

        client = createClientRx();

        publishScenario = getPublishScenario();
    }

    public static String payloadToString(byte[] payload) {
        if (payload == null) {
            return null;
        }
        return new String(payload);
    }

    public Flowable<Mqtt5Publish> subscribeFilter(MqttTopicFilter topicFilter) {
        Flowable<Mqtt5Publish> subscribeScenario = getSubscribeScenario(topicFilter);
        subscribedTopics.add(topicFilter);

        return subscribeScenario;
    }

    @Override
    public void createSubscription(String topic, BiConsumer<String, byte[]> handle) {
        MqttTopicFilter topicFilter = MqttTopicFilter.of(topic);
        subscriptions.put(topicFilter,
                subscribeFilter(topicFilter).subscribe(mqtt5Publish -> handle.accept(mqtt5Publish.getTopic().toString(), mqtt5Publish.getPayloadAsBytes())));
    }

    @Override
    public void removeSubscription(String topic) {
        MqttTopicFilter topicFilter = MqttTopicFilter.of(topic);
        Disposable disposable = subscriptions.get(topicFilter);
        if (disposable != null) {
            disposable.dispose();
        }
        subscribedTopics.remove(topicFilter);
    }

    @Override
    public boolean isReadyForPublish() {
        return readyForPublish;
    }

    @Override
    public void start() {
        log.debug("Connecting mqtt");
        Completable connectScenario = connectScenario(client).doOnComplete(this::onConnected);

        connected = connectScenario.subscribe();
    }

    @Override
    public void stop() {
        if (connected != null) {
            if (publish != null) {
                publish.dispose();
            }
            if (!subscriptions.isEmpty()) {
                subscriptions.forEach((topicFilter, disposable) -> disposable.dispose());
            }
            connected.dispose();
        }
    }


    @Override
    public void publish(String topic, byte[] payload, boolean retain) {
        MqttQos qos = MqttQos.AT_LEAST_ONCE;
        log.trace("publish with topic: {} payload:{} retain: {} qos:{}", topic, payloadToString(payload), retain, qos);
        Mqtt5Publish build = Mqtt5Publish.builder()
                .topic(topic)
                .payload(payload)
                .retain(true)
                .qos(qos)
                .build();
        messagesSubject.onNext(build);
    }

    private void onConnected() {
        //subscribe = subscribeDiscovery.ignoreElements().subscribe();
        publish = publishScenario.subscribe();
    }


    private Flowable<Mqtt5Publish> getSubscribeScenario(MqttTopicFilter topic) {
        return client.subscribePublishesWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .applySubscribe()
                .doOnSingle(subAck -> log.trace("Subscribed, {} ", subAck.getReasonCodes()))
                .doOnNext(publish -> log.trace("received topic: {}", topic))
                .doOnComplete(() -> log.trace("completed"))
                .doOnError(throwable -> log.trace("error"));

    }

    private Flowable<Mqtt5PublishResult> getPublishScenario() {
        Flowable<Mqtt5Publish> messagesToPublish = messagesSubject.toFlowable(BackpressureStrategy.BUFFER)
                .doOnSubscribe(d -> {
                    log.trace("message subject subscribed");
                    readyForPublish = true;
                });

        return client.publish(messagesToPublish).observeOn(Schedulers.io())
                //.subscribeOn(Schedulers.io())
                .doOnNext(publishResult -> log.trace("Publish acknowledged: {}",
                        publishResult.getPublish().getTopic()));
    }

    private Mqtt5RxClient createClientRx() {
        Mqtt5ClientBuilder mqtt5ClientBuilder = getMqtt5ClientBuilder()
                .addConnectedListener(context -> log.trace("ConnectedListener: {}", context.getClientConfig().getState()))
                .addDisconnectedListener(context -> log.trace("DisconnectedListener: {}", context.getClientConfig().getState()));
        return mqtt5ClientBuilder.buildRx();

    }

    private Completable connectScenario(Mqtt5RxClient client) {
        Single<Mqtt5ConnAck> connAckSingle = client.connect(Mqtt5Connect.builder()
                .keepAlive(10)
                //.cleanStart(false)
                .build());

        return connAckSingle
                .doOnSuccess(connAck -> log.trace("Connected: {} ", connAck.getReasonCode()))
                .doOnError(throwable -> log.error("Connection failed: {}", throwable.getMessage())).ignoreElement();
    }

}

