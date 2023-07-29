package com.dfi.sbc2ha.sensor.modbus;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.Register;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.RegisterBase;
import com.dfi.sbc2ha.event.sensor.ModbusEvent;
import com.dfi.sbc2ha.helper.Scheduler;
import com.dfi.sbc2ha.modbus.Modbus;
import com.dfi.sbc2ha.sensor.Sensor;
import com.dfi.sbc2ha.state.device.DeviceState;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.state.sensor.ScalarState.CHANGED;

@Slf4j
public class ModbusSensor extends Sensor implements Runnable {
    protected final Duration updateInterval;
    protected final AtomicBoolean stopScheduler;
    protected final Map<Register, LinkedHashSet<Consumer<ModbusEvent>>> listenersAnyRegister = new LinkedHashMap<>();
    private final String id;
    private final int unitId;
    private final Modbus bus;
    private final ModbusSensorDefinition definition;
    private final List<Consumer<DeviceState>> stateListeners = new ArrayList<>();
    private ScheduledFuture<?> scheduledFuture;
    private DeviceState state = DeviceState.OFFLINE;

    public ModbusSensor(String name, String id, int unitId, Duration updateInterval, Modbus bus, ModbusSensorDefinition definition) {
        super(name);
        this.id = id;
        this.unitId = unitId;
        this.bus = bus;
        this.definition = definition;

        this.updateInterval = updateInterval;
        stopScheduler = new AtomicBoolean(true);

        listeners.put(CHANGED, new LinkedHashSet<>());
    }

    public String getId() {
        return id;
    }

    public ModbusSensorDefinition getDefinition() {
        return definition;
    }

    @Override
    protected void closeDelegate() {

    }

    @Override
    protected void setDelegateListener() {
        stopScheduler.set(false);
        scheduledFuture = Scheduler.getInstance()
                .scheduleAtFixedRate(this, updateInterval.toNanos(), updateInterval.toNanos(),
                        TimeUnit.NANOSECONDS);

    }

    protected void removeDelegateListener() {
        stopScheduler.set(true);
        scheduledFuture.cancel(true);
    }

    public boolean isAvailable() {
        Register firstRegister = definition.getFirstRegister();
        boolean deviceAvailable = bus.isDeviceAvailable(unitId, firstRegister.getAddress(), firstRegister.getModbusRegisterType());
        if (deviceAvailable) {
            onChaneState(DeviceState.ONLINE);
        }
        return deviceAvailable;

    }

    private void onChaneState(DeviceState deviceState) {
        if (deviceState == state) {
            return;
        }
        state = deviceState;
        stateListeners.forEach(consumer -> consumer.accept(deviceState));
    }

    @Override
    public void run() {
        log.trace("refreshing modbus: {}", name);
        try {

            for (RegisterBase base : definition.getRegistersBase()) {
                short[] responseData = bus.readMany(unitId, base.getBase(), base.getLength(), base.getModbusRegisterType());
                if(responseData.length > 0) {
                    onChaneState(DeviceState.ONLINE);

                    List<Register> registers = base.getRegisters();
                    for (Register register : registers) {
                        try {
                            float number = register.decode(responseData, base.getBase());
                            handleChanged(register, number);
                        } catch (RuntimeException e) {
                            log.error(e.getMessage(), e);
                        }

                    }
                }
            }
        } catch (RuntimeException logged) {
            log.error(logged.getMessage(), logged);
            onChaneState(DeviceState.OFFLINE);
        }
    }

    public void handleChanged(Register register, float value) {
        ModbusEvent evt = new ModbusEvent(register, value);
        if (listenersAnyRegister.containsKey(register)) {
            listenersAnyRegister.get(register).forEach(consumer -> consumer.accept(evt));
        }
        listeners.get(CHANGED).forEach(consumer -> consumer.accept(evt));
        handleAny(evt);
    }

    public void whenAnyRegister(Consumer<ModbusEvent> consumer, Register registerName) {
        if (listenersEmpty()) {
            enableDelegateListener();
        }
        if (!listenersAnyRegister.containsKey(registerName)) {
            listenersAnyRegister.put(registerName, new LinkedHashSet<>());
        }
        listenersAnyRegister.get(registerName).add(consumer);
    }

    @Override
    public void removeAllListeners() {
        listenersAnyRegister.clear();
        super.removeAllListeners();
    }


    @Override
    public boolean listenersEmpty() {
        if (listenersAnyRegister.size() > 0) {
            return false;
        }
        return super.listenersEmpty();
    }

    public void whenState(Consumer<DeviceState> consumer) {
        stateListeners.add(consumer);
    }
}
