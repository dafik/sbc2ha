package com.dfi.sbc2ha.actuator;

import com.dfi.sbc2ha.components.actuator.Cover;
import com.dfi.sbc2ha.event.actuator.CoverEvent;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.diozero.api.DigitalOutputDevice;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


import java.time.Duration;

import static org.mockito.Mockito.mock;

@Slf4j
public class CoverTest {

    DigitalOutputDevice open;
    DigitalOutputDevice close;

    @BeforeEach
    public void before() {
        open = mock(DigitalOutputDevice.class);
        close = mock(DigitalOutputDevice.class);
    }

    @SneakyThrows
    @Test
    @Disabled
    public void coverClose() {

        Duration openTime = DurationStyle.detectAndParse("10s");
        Duration closeTime = DurationStyle.detectAndParse("15s");
        Cover cover = new Cover(open, close, openTime, closeTime, "test", 1, 100);
        cover.addListenerAny(e -> {
            CoverEvent ce = (CoverEvent) e;
            log.info("state:{} pos:{}", ce.getState(), ce.getPosition());
        });

        cover.closeCover();

        Thread.sleep(closeTime.toMillis());
        cover.openCover();

    }

}