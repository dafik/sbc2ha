package com.dfi.sbc2ha.helper.deserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DurationSerializer extends JsonSerializer<Duration> {

    @Override
    public void serialize(Duration value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String duration;
        if (value.getSeconds() > 0) {
            duration = DurationStyle.SIMPLE.print(value, ChronoUnit.SECONDS);
        } else {
            duration = DurationStyle.SIMPLE.print(value);
        }


        gen.writeString(duration);
    }
}


