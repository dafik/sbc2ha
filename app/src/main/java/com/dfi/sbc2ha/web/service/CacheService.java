package com.dfi.sbc2ha.web.service;

import com.dfi.sbc2ha.services.config.ConfigProvider;
import com.dfi.sbc2ha.services.state.StateManager;

public class CacheService {

    public static boolean clearStates() {
        return StateManager.getInstance().clearCache();
    }


    public static boolean clearConfig() {
        return ConfigProvider.clearCache();
    }
}
