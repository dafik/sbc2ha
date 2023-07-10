package com.dfi.sbc2ha.sensor.modbus;

import com.dfi.sbc2ha.modbus.Modbus;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.ModbusSensorDefinition;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.Register;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.modbus.RegisterBase;
import com.dfi.sbc2ha.helper.ha.DeviceState;
import com.dfi.sbc2ha.sensor.NullDelegate;
import com.dfi.sbc2ha.sensor.scheduled.ScheduledSensor;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class ModbusSensor extends ScheduledSensor<NullDelegate, ModbusEvent, ModbusState> {

    protected final Map<Register, LinkedHashSet<Consumer<ModbusEvent>>> listenersAnyRegister = new LinkedHashMap<>();
    private final String id;
    private final int unitId;
    private final Modbus bus;
    private final ModbusSensorDefinition definition;
    private final List<Consumer<DeviceState>> stateListeners = new ArrayList<>();
    private DeviceState state = DeviceState.OFFLINE;

    public ModbusSensor(String name, String id, int unitId, Duration updateInterval, Modbus bus, ModbusSensorDefinition definition) {
        super(name, updateInterval);
        this.id = id;
        this.unitId = unitId;


        this.bus = bus;
        this.definition = definition;

        listeners.put(ModbusState.CHANGED, new LinkedHashSet<>());
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
        Logger.debug("refreshing modbus: {}", name);
        try {


            for (RegisterBase base : definition.getRegistersBase()) {
                short[] responseData = bus.readMany(unitId, base.getBase(), base.getLength(), base.getModbusRegisterType());
                onChaneState(DeviceState.ONLINE);

                List<Register> registers = base.getRegisters();
                for (Register register : registers) {
                    try {
                        Number number = register.decode(responseData, base.getBase());
                        handleChanged(register, number);
                    } catch (RuntimeException e) {
                        Logger.error(e);
                    }

                }
            }
        } catch (RuntimeException logged) {
            Logger.error(logged);
            onChaneState(DeviceState.OFFLINE);
        }
    }

    public void handleChanged(Register register, Number value) {
        ModbusEvent evt = new ModbusEvent(ModbusState.CHANGED, register, value);
        if (listenersAnyRegister.containsKey(register)) {
            listenersAnyRegister.get(register).forEach(consumer -> consumer.accept(evt));
        }
        listeners.get(ModbusState.CHANGED).forEach(consumer -> consumer.accept(evt));
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
