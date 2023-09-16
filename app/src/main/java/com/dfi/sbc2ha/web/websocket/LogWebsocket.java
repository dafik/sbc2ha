package com.dfi.sbc2ha.web.websocket;


import com.dfi.sbc2ha.services.log.RxLogPublisher;
import com.dfi.sbc2ha.web.json.PresciseTimestampSerializer;
import com.dfi.sbc2ha.web.json.ThreadMixin;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.tinylog.core.LogEntry;
import org.tinylog.runtime.PreciseTimestamp;

@WebSocket
@Slf4j
public class LogWebsocket extends BaseWebsocket {
    private RxLogPublisher logPublisher = RxLogPublisher.getInstance();
    private Disposable disposable;

    public LogWebsocket() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(PreciseTimestamp.class, new PresciseTimestampSerializer());
        om.registerModule(module);
        om.addMixIn(Thread.class, ThreadMixin.class);
    }

    @Override
    protected void onConnected(Session session) {
        if (areEmptySessions()) {
            subscribeLogs();
        }
    }

    @Override
    protected void onClosed() {
        if (areEmptySessions()) {
            unSubscribeLogs();
        }
    }

    @Override
    protected void onMessage(Session session, String message) {

    }

    private void subscribeLogs() {
        disposable = logPublisher.subscribe(this::onLogEntry);
        log.info("logs subscribed");
    }

    private void unSubscribeLogs() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
            log.info("logs unSubscribed");
        }
    }

    private void onLogEntry(LogEntry logEntry) {
        notifyAll(logEntry);
    }

}
