package com.dfi.sbc2ha.config.sbc2ha.deserializer;

import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.digital.InputConfig;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;

public class InputConfigDeserializer extends JsonDeserializer<InputConfig<?>> {
    static ObjectMapper mapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build();
    @Override
    public InputConfig<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        InputConfig<?> inputConfig = mapper.readValue(p, InputConfig.class);
        return inputConfig;
    }
}


