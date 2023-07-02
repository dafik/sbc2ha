package com.dfi.sbc2ha.helper;

public class Constants {
    public static final String HIGH = "high";
    public static final String LOW = "low";
    public static final String BOTH = "both";
    public static final String FALLING = "falling";
    public static final String RISING = "rising";

    public static final String BONEIO = "boneIO";
    public static final String NONE = "none";

    // MISCELLANEOUS CONSTS
    public static final String RELAY = "relay";
    public static final String LED = "led";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String TOGGLE = "TOGGLE";
    public static final String STATE = "state";
    public static final String BRIGHTNESS = "brightness";
    public static final String SET_BRIGHTNESS = "set_brightness";
    public static final String ENABLED = "enabled";
    public static final String OUTPUT = "output";
    public static final String PIN = "pin";
    public static final String ID = "id";
    public static final String KIND = "kind";
    public static final String GPIO = "gpio";
    public static final String PCA = "pca";
    public static final String GPIO_MODE = "gpio_mode";
    public static final String ACTIONS = "actions";
    public static final String ACTION = "action";
    public static final String SWITCH = "switch";
    public static final String LIGHT = "light";
    public static final String BUTTON = "button";
    public static final String CONFIG_PIN = "/usr/bin/config-pin";
    public static final String UPDATE_INTERVAL = "update_interval";
    public static final String ADC = "adc";
    public static final String IP = "ip";
    public static final String MASK = "mask";
    public static final String MAC = "mac";
    public static final String MODBUS = "modbus";
    public static final String UART = "uart";
    public static final String RX = "rx";
    public static final String TX = "tx";
    public static final String RESTORE_STATE = "restore_state";
    public static final String MODEL = "model";
/*
UARTS = {
    "uart1": {ID: "/dev/ttyS1", TX: "P9.24", RX: "P9.26"},
    "uart2": {ID: "/dev/ttyS2", TX: "P9.21", RX: "P9.22"},
    "uart3": {ID: "/dev/ttyS3", TX: "P9.42", RX: None},
    "uart4": {ID: "/dev/ttyS4", TX: "P9.13", RX: "P9.11"},
    "uart5": {ID: "/dev/ttyS5", TX: "P8.37", RX: "P8.38"},
}

relay_actions = {ON: "turn_on", OFF: "turn_off", TOGGLE: "toggle"}
*/

    // HA CONSTS
    public static final String HOMEASSISTANT = "homeassistant";
    public static final String HA_DISCOVERY = "ha_discovery";
    public static final String OUTPUT_TYPE = "output_type";
    public static final String SHOW_HA = "show_in_ha";

    // OLED CONST
    public static final String OLED = "oled";
    public static final String FONTS = "fonts";
    public static final String OLED_PIN = "P9_41";
    public static final int GIGABYTE = 1073741824;
    public static final int MEGABYTE = 1048576;
    public static final int WIDTH = 128;
    public static final String UPTIME = "uptime";
    public static final String NETWORK = "network";
    public static final String CPU = "cpu";
    public static final String DISK = "disk";
    public static final String MEMORY = "memory";
    public static final String SWAP = "swap";
    public static final String WHITE = "white";

    // INPUT CONST
    public static final String INPUT = "input";
    public static final String SINGLE = "single";
    public static final String DOUBLE = "double";
    public static final String LONG = "long";
    public static final String PRESSED = "pressed";
    public static final String RELEASED = "released";


    // MQTT CONST
    public static final String PAHO = "paho.mqtt.client";
    public static final String PYMODBUS = "pymodbus";
    public static final String MQTT = "mqtt";
    public static final String HOST = "host";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PORT = "port";
    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    public static final String TOPIC = "topic";
    public static final String TOPIC_PREFIX = "topic_prefix";

    // I2C, PCA and MCP CONST
    public static final String ADDRESS = "address";
    public static final String MCP23017 = "mcp23017";
    public static final String PCA9685 = "pca9685";
    public static final String MCP = "mcp";
    public static final String MCP_ID = "mcp_id";
    public static final String PCA_ID = "pca_id";
    public static final String INIT_SLEEP = "init_sleep";

    // SENSOR CONST
    public static final String TEMPERATURE = "temperature";
    public static final String SENSOR = "sensor";
    public static final String BINARY_SENSOR = "binary_sensor";
    public static final String LM75 = "lm75";
    public static final String MCP_TEMP_9808 = "mcp9808";
    public static final String INPUT_SENSOR = "inputsensor";
    public static final String DS2482 = "ds2482";
    public static final String DALLAS = "dallas";
    public static final String ONEWIRE = "onewire";

    public static final String BASE = "base";
    public static final String LENGTH = "length";
    public static final String REGISTERS = "registers";

    public static final String COVER = "cover";
    public static final String IDLE = "idle";
    public static final String OPENING = "opening";
    public static final String CLOSING = "closing";
    public static final String CLOSED = "closed";
    public static final String OPEN = "open";
    public static final String CLOSE = "close";
    public static final String STOP = "stop";

// TYPING
    /*
ClickTypes = Literal[SINGLE, DOUBLE, LONG, PRESSED, RELEASED]
OledDataTypes = Literal[UPTIME, NETWORK, CPU, DISK, MEMORY, SWAP, OUTPUT]
Gpio_States = Literal[HIGH, LOW]
Gpio_Edges = Literal[BOTH, FALLING]
InputTypes = Literal[INPUT, INPUT_SENSOR]
DEVICE_CLASS = "device_class"
DallasBusTypes = Literal[DS2482, DALLAS]
FILTERS = "filters"

     */


}
