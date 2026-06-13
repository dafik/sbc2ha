package iot.sbc2ha.hardware.io.diozero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generic registry for hardware components.
 *
 * <p>Mirrors the old app's {@code Bus<?>} busMap pattern: physical chips
 * (MCP23017, PCA9685, OLED I2C device, etc.) are registered once and
 * shared across all pins/adapters targeting the same physical component.
 * Pin-specific devices (DigitalOutputDevice, etc.) are created per-adapter
 * using the shared component as factory.</p>
 *
 * <p>Registration-then-usage lifecycle:</p>
 * <ol>
 *   <li><b>Register</b> — {@link #register} the physical component (once per chip)</li>
 *   <li><b>Lookup</b> — {@link #getComponent} retrieves it for adapter construction</li>
 * </ol>
 *
 * <h3>Key composition</h3>
 * <p>Each component is keyed by {@code type:id}, where:</p>
 * <ul>
 *   <li>{@code type} is a lowercase identifier (e.g. {@code "mcp23017"}, {@code "pca9685"})</li>
 *   <li>{@code id} distinguishes multiple instances of the same type (e.g. {@code "0x20"}, {@code "mcp0"})</li>
 * </ul>
 *
 * <h3>Thread safety</h3>
 * <p>Concurrent access is safe — instances are cached in a
 * {@link ConcurrentHashMap}. Lookup of existing entries is lock-free.</p>
 *
 * @see HardwareComponentKey
 */
public final class HardwareComponentRegistry {

    private static final Logger log = LoggerFactory.getLogger(HardwareComponentRegistry.class);

    /** Singleton instance. */
    public static final HardwareComponentRegistry INSTANCE = new HardwareComponentRegistry();

    private final Map<HardwareComponentKey, Object> cache = new ConcurrentHashMap<>();

    private HardwareComponentRegistry() {
    }

    /**
     * Register a hardware component.
     *
     * @param key   type:id identifier
     * @param component the hardware component instance (e.g. MCP23017, PCA9685)
     * @throws IllegalStateException if a component with the same key is already registered
     */
    @SuppressWarnings("unused")
    public <T> T register(HardwareComponentKey key, T component) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(component, "component must not be null");
        T existing = (T) cache.putIfAbsent(key, component);
        if (existing != null) {
            throw new IllegalStateException(
                    "Component already registered: " + key
                    + " (existing: " + existing.getClass().getSimpleName() + ")");
        }
        log.info("Registered hardware component: type={}, id={}", key.type(), key.id());
        return component;
    }

    /**
     * Register a component if not already present (idempotent).
     * <p>If the factory returns {@code null}, the entry is NOT cached
     * (ConcurrentHashMap does not accept null values). Subsequent lookups
     * will retry the factory.</p>
     *
     * @return the registered or previously registered component (may be null)
     */
    @SuppressWarnings("unused")
    public <T> T getOrRegister(HardwareComponentKey key, java.util.function.Supplier<T> factory) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(factory, "factory must not be null");
        @SuppressWarnings("unchecked")
        T existing = (T) cache.get(key);
        if (existing != null) {
            return existing;
        }
        T component = factory.get();
        if (component == null) {
            // Factory failed — don't cache null (ConcurrentHashMap rejects it)
            log.warn("Factory returned null for key {}: component not cached", key);
            return null;
        }
        T put = (T) cache.putIfAbsent(key, component);
        if (put != null) {
            log.debug("Component already registered by concurrent thread: {}", key);
            return put;
        }
        log.info("Registered hardware component (lazy): type={}, id={}", key.type(), key.id());
        return component;
    }

    /**
     * Lookup a registered component.
     *
     * @return the component, or {@code null} if not found
     */
    @SuppressWarnings("unused")
    public Object getComponent(HardwareComponentKey key) {
        return cache.get(key);
    }

    /**
     * Check if a component is registered.
     */
    public boolean contains(HardwareComponentKey key) {
        return cache.containsKey(key);
    }

    /**
     * Number of registered components (for testing).
     */
    @SuppressWarnings("unused")
    int size() {
        return cache.size();
    }

    /**
     * Registered keys (for testing).
     */
    @SuppressWarnings("unused")
    java.util.Set<HardwareComponentKey> keys() {
        return Collections.unmodifiableSet(cache.keySet());
    }

    /**
     * Clear all registered components (for testing).
     * <p>Does NOT close the underlying devices — mock I2C providers
     * will throw if close() is called on an incomplete/mock instance.</p>
     */
    @SuppressWarnings("unused")
    void clear() {
        cache.clear();
        log.info("Hardware component registry cleared");
    }
}
