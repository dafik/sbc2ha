package com.dfi.sbc2ha.config.sbc2ha.definition.platform;

import com.dfi.sbc2ha.config.sbc2ha.definition.enums.PlatformType;
import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ScreenType;
import com.dfi.sbc2ha.helper.deserializer.DurationDeserializer;
import com.dfi.sbc2ha.helper.deserializer.DurationSerializer;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.dfi.sbc2ha.config.sbc2ha.definition.enums.ScreenType.*;

@Data
public class OledConfig extends PlatformConfig {

    public static List<ScreenType> DEFAULT_SCREENS = List.of(
            UPTIME,
            NETWORK,
            CPU,
            DISK,
            MEMORY,
            SWAP,
            OUTPUTS
    );
    boolean enabled = false;

    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("screensaver_timeout")
    Duration screensaverTimeout = DurationStyle.detectAndParse("60s");

    List<ScreenType> screens = new ArrayList<>();


    public OledConfig() {
        super();
        platform = PlatformType.OLED;
    }
}
