package iot.sbc2ha.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages boot lifecycle state transitions and display updates.
 *
 * <p>The manager owns a mutable {@code state} field and delegates to a
 * {@link BootDisplay} on every transition. If the display throws,
 * the error is logged and the transition continues — display
 * failures must never block the boot sequence (fail-open).</p>
 *
 * @see LifecycleState
 * @see BootDisplay
 */
public class Lifecycle {

    private static final Logger log = LoggerFactory.getLogger(Lifecycle.class);

    private LifecycleState state = LifecycleState.BOOTING;
    private BootDisplay display;

    public Lifecycle(BootDisplay display) {
        this.display = display;
    }

    /**
     * @return the current lifecycle state
     */
    public LifecycleState state() {
        return state;
    }

    /**
     * Transition to a new state, updating the display.
     *
     * <p>If the display throws, the exception is caught, logged at WARN,
     * and the state is still recorded — the boot sequence is never blocked
     * by a display failure.</p>
     *
     * @param newState the target state
     */
    public void transition(LifecycleState newState) {
        LifecycleState oldState = this.state;
        log.info("Lifecycle: {} -> {}", oldState, newState);
        this.state = newState;
        try {
            display.update(newState);
        } catch (Exception e) {
            log.warn("BootDisplay.update failed (non-fatal): {}", e.getMessage());
        }
    }

    /**
     * Replace the display with a new implementation (e.g. swap from
     * log-only to real OLED after config is parsed).
     *
     * @param newDisplay the new display to use
     */
    public void setDisplay(BootDisplay newDisplay) {
        this.display = newDisplay;
    }

    /**
     * Clean up the display on shutdown.
     */
    public void shutdown() {
        try {
            display.close();
        } catch (Exception e) {
            log.warn("BootDisplay.close failed (non-fatal): {}", e.getMessage());
        }
    }
}
