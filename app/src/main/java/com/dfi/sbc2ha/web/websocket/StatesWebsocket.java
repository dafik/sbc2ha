package com.dfi.sbc2ha.web.websocket;


import com.dfi.sbc2ha.Sbc2ha;
import com.dfi.sbc2ha.event.DeviceStateEvent;
import com.dfi.sbc2ha.services.manager.Manager;
import com.dfi.sbc2ha.web.json.PresciseTimestampSerializer;
import com.dfi.sbc2ha.web.json.ThreadMixin;
import com.dfi.sbc2ha.web.websocket.command.state.StateCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.tinylog.runtime.PreciseTimestamp;

import java.io.IOException;
import java.util.Set;

@WebSocket
@Slf4j
public class StatesWebsocket extends BaseWebsocket {
    private final EventBus eventBus = EventBus.getDefault();

    public StatesWebsocket() {



        SimpleModule module = new SimpleModule();
        module.addSerializer(PreciseTimestamp.class, new PresciseTimestampSerializer());
        om.registerModule(module);
        om.addMixIn(Thread.class, ThreadMixin.class);


        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.setDefaultFilter(SimpleBeanPropertyFilter.serializeAll());
        writer = om.writer(filterProvider);


    }

    @Override
    protected void onConnected(Session session) {
        if (areEmptySessions()) {
            subscribeStates();
        }
        Set<Manager.InitialState> all = getInitial();
        try {
            String initial = serialize(all);
            session.getRemote().sendString(initial);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribeStates() {
        eventBus.register(this);
        log.info("states subscribed");
    }

    private Set<Manager.InitialState> getInitial() {
        return Sbc2ha.getSbc2ha().getCurrentState();
    }

    @Override
    protected void onClosed() {
        if (areEmptySessions()) {
            unSubscribeStates();
        }
    }

    private void unSubscribeStates() {
        eventBus.unregister(this);
        log.info("states unSubscribed");
    }

    @Override
    protected void onMessage(Session session, String message) {
        try {
            StateCommand stateCommand = om.readValue(message, StateCommand.class);
            eventBus.post(stateCommand);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onStateEvent(DeviceStateEvent state) {
        log.trace("got stateEvent: {}", state);
        notifyAll(state);
    }
}
