package com.dfi.sbc2ha.services.log;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;
import org.tinylog.core.LogEntry;

public class RxLogPublisher {
    private static RxLogPublisher instance;
    private final ReplaySubject<LogEntry> subject = ReplaySubject.createWithSize(100);

    private RxLogPublisher() {

    }

    public static RxLogPublisher getInstance() {
        if (instance == null) {
            instance = new RxLogPublisher();
        }
        return instance;
    }

    public Disposable subscribe(Consumer<LogEntry> onNext) {
        return subject.subscribe(onNext);
    }

    public void publish(LogEntry logEntry) {
        subject.onNext(logEntry);
    }
}
