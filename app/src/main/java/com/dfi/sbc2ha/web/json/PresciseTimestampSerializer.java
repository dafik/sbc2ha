package com.dfi.sbc2ha.web.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.tinylog.runtime.PreciseTimestamp;

import java.io.IOException;

public class PresciseTimestampSerializer extends JsonSerializer<PreciseTimestamp> {

    @Override
    public void serialize(PreciseTimestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toInstant().toString());
    }
}
