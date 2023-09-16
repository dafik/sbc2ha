package com.dfi.sbc2ha.web.service;

import com.dfi.sbc2ha.Sbc2ha;
import com.dfi.sbc2ha.services.config.ConfigProvider;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigService {

    public static void writeConfig(Path config) {

    }

    public static void writeCache(Path config) throws IOException {
        AppConfig appConfig = ConfigProvider.loadFromUploadedCache(config);
        ConfigProvider.saveCache(Sbc2ha.getConfigFile(), appConfig);
        //Sbc2ha.reload();
    }
}
