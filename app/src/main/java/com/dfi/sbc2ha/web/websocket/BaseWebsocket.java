package com.dfi.sbc2ha.web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public abstract class BaseWebsocket {
    private final Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    protected ObjectWriter writer;
    protected ObjectMapper om = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .registerModule(new JavaTimeModule());

    @OnWebSocketConnect
    public void connected(Session session) {
        log.error("new session {}", session.getRemoteAddress());
        onConnected(session);
        synchronized (sessions) {
            sessions.add(session);
        }
    }

    protected abstract void onConnected(Session session);

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        log.error("close session {}", session.getRemoteAddress());
        synchronized (sessions) {
            sessions.remove(session);
        }
        onClosed();
    }

    protected abstract void onClosed();

    protected boolean areEmptySessions() {
        return sessions.isEmpty();
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Got: " + message);   // Print message
        //session.getRemote().sendString(message); // and send it back
        onMessage(session, message);
    }

    protected abstract void onMessage(Session session, String message);

    protected void notifyAll(Object message) {
        if (!sessions.isEmpty()) {
            try {
                String s = serialize(message);
                synchronized (sessions) {
                    sessions.forEach(session -> {
                        try {
                            if (session.isOpen()) {
                                session.getRemote().sendString(s);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected String serialize(Object message) throws JsonProcessingException {
        if (writer != null) {
            return writer.writeValueAsString(message);
        }
        return om.writeValueAsString(message);
    }
}
