package com.diozero.devices.oneWire.bus;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public interface OneWireDevice {

    Type getType();

    String getSerialNumber();

    void close();

    enum Type {
        DS18S20(0x10), DS1822(0x22), DS18B20(0x28), DS1825(0x3B), DS28EA00(0x42), MAX31850K(0x3B),
        GENERIC(0x00);

        private static Map<Integer, Type> TYPES;

        private int id;

        Type(int id) {
            this.id = id;

            addType();
        }

        public static boolean isValid(Path path) {
            return isValidId(path.getFileName().toString().substring(0, 2));
        }

        public static boolean isValidId(String idString) {
            try {
                return TYPES.containsKey(Integer.valueOf(idString, 16));
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public static Type valueOf(int id) {
            return TYPES.get(Integer.valueOf(id));
        }

        public static Type valueOf(Path path) {
            Type type = TYPES.get(Integer.valueOf(path.getFileName().toString().substring(0, 2), 16));
            if (type == null) {
                throw new IllegalArgumentException("Invalid W1ThermSensor.Type slave='" + path.toFile().getName() + "'");
            }
            return type;
        }

        private synchronized void addType() {
            if (TYPES == null) {
                TYPES = new HashMap<>();
            }
            TYPES.put(Integer.valueOf(id), this);
        }

        public int getId() {
            return id;
        }
    }
}
