package com.dfi.sbc2ha.web.websocket;


import com.dfi.sbc2ha.helper.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@WebSocket
@Slf4j
public class DeviceWebsocket extends BaseWebsocket {

    private ScheduledFuture<?> scheduledFuture;

    public DeviceWebsocket() {

    }

    @Override
    protected void onConnected(Session session) {
        if (areEmptySessions()) {
            sendPing();
        }
        try {
            session.getRemote().sendString("{\"initial\":true}");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPing() {
        String message = "{\"ping\":\"ping\"}";
        scheduledFuture = Scheduler.getInstance()
                .scheduleAtFixedRate(() -> notifyAll(message), 1, 1, TimeUnit.SECONDS);

    }

    @Override
    protected void onClosed() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

    @Override
    protected void onMessage(Session session, String message) {
        try {
            session.getRemote().sendString("pong");
        } catch (IOException e) {
            log.error("send error: ", e);
            //throw new RuntimeException(e);
        }
    }


}
