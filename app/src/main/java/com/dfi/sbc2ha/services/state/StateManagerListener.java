package com.dfi.sbc2ha.services.state;

@FunctionalInterface
public interface StateManagerListener {
    void onStateChange();
}

