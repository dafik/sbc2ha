package com.dfi.sbc2ha.helper.mqtt.hivemq;

import com.dfi.sbc2ha.config.sbc2ha.definition.platform.MqttConfig;
import com.dfi.sbc2ha.helper.ha.DeviceState;
import com.dfi.sbc2ha.helper.mqtt.MqttConfigHelper;
import com.dfi.sbc2ha.helper.mqtt.MqttHelper;
import com.dfi.sbc2ha.manager.Manager;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder.Publishes.Complete;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder.Publishes.Start;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.rx.FlowableWithSingle;
import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MqttHelperHiveRx extends MqttHelperHive implements MqttHelper {

    private final Subject<Mqtt5Publish> messagesSubject = PublishSubject.create();
    private final Mqtt5RxClient client;
    private final Flowable<Mqtt5PublishResult> publishScenario;
    private Flowable<Mqtt5Publish> subscribeDiscovery;
    private Flowable<Mqtt5Publish> subscribeCommands;
    private Disposable subscribe;
    private Disposable publish;
    private Disposable connected;
    private boolean readyForPublish = false;
    private Manager connectedListener;
    private List<MqttTopicFilter> subscribedTopics = new ArrayList<>();
    private Map<MqttTopicFilter, Disposable> subscriptions = new HashMap<>();

    public MqttHelperHiveRx(MqttConfig config, MqttConfigHelper mqttConfigHelper) {
        super(config, mqttConfigHelper);

        // RxJava2Debug.enableRxJava2AssemblyTracking(new String[]{"com.dfi"});

        //noinspection ResultOfMethodCallIgnored
        messagesSubject.doOnSubscribe(d -> Logger.trace("message subject subscribed"));

        client = createClientRx();

/*        List<MqttTopicFilter> sub = new ArrayList<>();
        sub.add(MqttTopicFilter.of(mqttConfigHelper.getHaDiscoveryTopic()));
        sub.add(MqttTopicFilter.of(mqttConfigHelper.getCommandTopic()));
        sub.add(MqttTopicFilter.of(mqttConfigHelper.getHaStatusTopic()));

        subscribeDiscovery = getSubscribeScenario(sub);
        subscribeCommands = getSubscribeScenario(sub);*/

        publishScenario = getPublishScenario();
    }

    public static String payloadToString(byte[] payload) {
        if (payload == null) {
            return null;
        }
        return new String(payload);
    }

    @Override
    public Flowable<Mqtt5Publish> subscribeTopic(String topic) {
        return subscribeFilter(MqttTopicFilter.of(topic));
    }

    public Flowable<Mqtt5Publish> subscribeFilter(MqttTopicFilter topicFilter) {
        Flowable<Mqtt5Publish> subscribeScenario = getSubscribeScenario(topicFilter);
        subscribedTopics.add(topicFilter);

        return subscribeScenario;
    }

    @Override
    public void createSubscription(String topic, Consumer<? super Mqtt5Publish> handle) {
        MqttTopicFilter topicFilter = MqttTopicFilter.of(topic);
        subscriptions.put(topicFilter,
                subscribeFilter(topicFilter).subscribe(handle));
    }

    @Override
    public Observable<Mqtt5Publish> getSubscriptions() {
        return subscribeDiscovery.toObservable();
    }

    @Override
    public boolean isReadyForPublish() {
        return readyForPublish;
    }

    @Override
    public void start() {
        Logger.debug("Connecting mqtt");
        Completable connectScenario = connectScenario(client).doOnComplete(this::onConnected);

        connected = connectScenario.subscribe();
    }

    @Override
    public void stop() {
        if (connected != null) {
            if (publish != null) {
                publish.dispose();
            }
            if (subscribe != null) {
                subscribe.dispose();
            }
            connected.dispose();
        }
    }

    @Override
    public void publish(String topic, byte[] payload) {
        publish(topic, payload, true);
    }

    @Override
    public void publish(String topic, byte[] payload, boolean retain) {
        MqttQos qos = MqttQos.EXACTLY_ONCE;
        Logger.trace("publish with topic: {} payload:{} retain: {} qos:{}", topic, payloadToString(payload), retain, qos);
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

    private Flowable<Mqtt5Publish> getSubscribeScenario(List<MqttTopicFilter> topics) {
        Start<FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck>> flowableWithSingleStart = client.subscribePublishesWith();

        Complete<FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck>> complete = null;
        for (MqttTopicFilter topic : topics) {
            complete = flowableWithSingleStart
                    .addSubscription()
                    .topicFilter(topic)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .applySubscription();
        }
        assert complete != null;
        FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subAckAndMatchingPublishes = complete.applySubscribe();

        return subAckAndMatchingPublishes
                .doOnSingle(subAck -> Logger.trace("Subscribed, {} ", subAck.getReasonCodes()));

    }

    private Flowable<Mqtt5Publish> getSubscribeScenario(MqttTopicFilter topic) {
        return client.subscribePublishesWith()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .applySubscribe()
                .doOnSingle(subAck -> {
                    Logger.trace("Subscribed, {} ", subAck.getReasonCodes());
                })
               .doOnNext(publish -> {
                   Logger.trace("received topic: {}", topic);
                })
                .doOnComplete(() -> {
                    Logger.trace("completed");
                })
                .doOnError(throwable -> {
                    Logger.trace("error");
                });

    }

    private Flowable<Mqtt5PublishResult> getPublishScenario() {
        Flowable<Mqtt5Publish> messagesToPublish = messagesSubject.toFlowable(BackpressureStrategy.BUFFER)
                .doOnSubscribe(d -> {
                    Logger.trace("message subject subscribed");
                    readyForPublish = true;
                });

        return client.publish(messagesToPublish).observeOn(Schedulers.io())
                //.subscribeOn(Schedulers.io())
                .doOnNext(publishResult -> {
                    Logger.trace("Publish acknowledged: {}",
                            publishResult.getPublish().getTopic());
                });
    }

    private Mqtt5RxClient createClientRx() {
        Mqtt5ClientBuilder mqtt5ClientBuilder = getMqtt5ClientBuilder()
                .addConnectedListener(context -> {
                    Logger.trace("ConnectedListener: {}" + context.getClientConfig().getState());
                    if (connectedListener != null) {
                        connectedListener.sendStatus(DeviceState.ONLINE);
                    }
                })
                .addDisconnectedListener(context -> Logger.trace("DisconnectedListener: {}", context.getClientConfig().getState()));
        return mqtt5ClientBuilder.buildRx();

    }

    private Completable connectScenario(Mqtt5RxClient client) {
        Single<Mqtt5ConnAck> connAckSingle = client.connect(Mqtt5Connect.builder().keepAlive(10).build());

        return connAckSingle
                .doOnSuccess(connAck -> Logger.trace("Connected: {} ", connAck.getReasonCode()))
                .doOnError(throwable -> Logger.error("Connection failed: {}", throwable.getMessage())).ignoreElement();
    }

    @Override
    public void setConnectedListener(Manager connectedListener) {
        this.connectedListener = connectedListener;
    }


}

