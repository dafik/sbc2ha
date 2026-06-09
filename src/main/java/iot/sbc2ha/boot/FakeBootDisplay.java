package iot.sbc2ha.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A no-op {@link BootDisplay} that writes state transitions to the log.
 *
 * <p>This is the default implementation used during fake/offline testing
 * and when no real display hardware is available.</p>
 */
public class FakeBootDisplay implements BootDisplay {

    private static final Logger log = LoggerFactory.getLogger(FakeBootDisplay.class);

    @Override
    public void update(LifecycleState state) {
        log.info("[BootDisplay] state -> {}", state);
    }

    @Override
    public void close() {
        log.info("[BootDisplay] closing");
    }
}
