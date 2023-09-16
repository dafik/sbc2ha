package com.dfi.sbc2ha.services.manager;

import lombok.Data;

@Data
public class ManagerCommandExternal implements ManagerCommand {
    private final String topic;
    private final byte[] payloadAsBytes;

    public ManagerCommandExternal(String topic, byte[] payloadAsBytes) {

        this.topic = topic;
        this.payloadAsBytes = payloadAsBytes;
    }
}
