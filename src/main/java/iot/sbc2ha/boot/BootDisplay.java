package iot.sbc2ha.boot;

/**
 * Displays the current boot lifecycle state to the user.
 *
 * <p>Implementations may target an OLED screen, serial console, log-only output,
 * or any other indicator. Implementations must be <strong>fail-open</strong>:
 * any failure during {@code update()} must not throw or block the lifecycle.</p>
 *
 * <p>See ADR-0006: OLED as early boot indicator.</p>
 */
public interface BootDisplay {

    /**
     * Notify the display of a new lifecycle state.
     *
     * @param state the new lifecycle state
     */
    void update(LifecycleState state);

    /**
     * Release any held resources (e.g. close an I2C connection).
     *
     * <p>Invoked during shutdown. A fail-open implementation must catch
     * all exceptions inside {@code close()}.</p>
     */
    void close();
}
