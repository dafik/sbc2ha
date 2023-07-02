package com.dfi.sbc2ha.helper.timer;

import java.util.Timer;
import java.util.TimerTask;

public class ClickTimer {
    private static ClickTimer instance;
    final Timer timer = new Timer("sbs2ha-click_timer");

    private ClickTimer() {

    }

    public static ClickTimer getInstance() {
        if (instance == null) {
            instance = new ClickTimer();
        }
        return instance;
    }

    public void schedule(TimerTask task, int delay) {
        timer.schedule(task, delay);
    }
}
