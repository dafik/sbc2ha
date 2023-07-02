package com.dfi.sbc2ha.config.sbc2ha.definition.enums;

public enum UartType {

    UART1(new UartConfig("/dev/ttyS1", "P9_24", "P9_26")),
    UART2(new UartConfig("/dev/ttyS2", "P9_21", "P9_22")),
    UART3(new UartConfig("/dev/ttyS3", "P9_42", null)),
    UART4(new UartConfig("/dev/ttyS4", "P9_13", "P9_11")),
    UART5(new UartConfig("/dev/ttyS5", "P8_37", "P8_38"));

    public final UartConfig uartConfig;

    UartType(UartConfig uartConfig) {

        this.uartConfig = uartConfig;
    }

    public static class UartConfig {
        public final String file;
        public final String tx;
        public final String rx;

        public UartConfig(String file, String tx, String rx) {
            this.file = file;
            this.tx = tx;
            this.rx = rx;
        }
    }

}


