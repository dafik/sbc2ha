package com.dfi.sbc2ha.helper.deserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

import java.io.IOException;
import java.math.BigInteger;

public class HexStringIntegerSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        BigInteger bigInteger = new BigInteger(value);
        String format = bigInteger.toString(16);

        //String format = String.format("%X", value);
        String s = "0x" + format;
        gen.writeString(s);

        //@JsonFormat(pattern=...) a
    }
}


