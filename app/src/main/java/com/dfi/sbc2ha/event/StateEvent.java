package com.dfi.sbc2ha.event;

import com.dfi.sbc2ha.event.actuator.CoverEvent;
import com.dfi.sbc2ha.event.actuator.LedEvent;
import com.dfi.sbc2ha.event.actuator.LedFadingEvent;
import com.dfi.sbc2ha.event.actuator.RelayEvent;
import com.dfi.sbc2ha.event.sensor.BinaryEvent;
import com.dfi.sbc2ha.event.sensor.ButtonEvent;
import com.dfi.sbc2ha.event.sensor.ModbusEvent;
import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CoverEvent.class),
        @JsonSubTypes.Type(value = LedEvent.class),
        @JsonSubTypes.Type(value = LedFadingEvent.class),
        @JsonSubTypes.Type(value = ModbusEvent.class),
        @JsonSubTypes.Type(value = RelayEvent.class),
        @JsonSubTypes.Type(value = ScalarEvent.class),
        @JsonSubTypes.Type(value = BinaryEvent.class),
        @JsonSubTypes.Type(value = ButtonEvent.class),
})
@JsonIgnoreProperties(value = {"epochTime", "nanoTime"})
@Data
@NoArgsConstructor
@JsonFilter("stateFilter")
public abstract class StateEvent {

    @Getter
    protected String state;
    private long epochTime;
    private long nanoTime;

    public StateEvent(long epochTime, long nanoTime, String state) {
        this.epochTime = epochTime;
        this.nanoTime = nanoTime;

        this.state = state;
    }
    public StateEvent(String state) {
        this.epochTime = System.currentTimeMillis();
        this.nanoTime = System.nanoTime();
        this.state = state;
    }

}
