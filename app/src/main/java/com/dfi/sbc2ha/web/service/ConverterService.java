package com.dfi.sbc2ha.web.service;

import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

import java.io.File;
import java.net.URLConnection;
import java.nio.file.Path;

import static com.dfi.sbc2ha.config.converter.Converter.convertFile;

@Slf4j
public class ConverterService {
    public static String convert(Path path) {

        try {

            URLConnection.getFileNameMap().getContentTypeFor("/tmp/upload/436751781765602436config.zip");

            File file = new File(path.toString());
            MagicMatch match = Magic.getMagicMatch(file,true);
            String mimeType = match.getMimeType();


            AppConfig appConfig = convertFile(path.toString(), "boneio", null, null);
            return new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(appConfig);
        } catch (Exception e) {
            log.error("error convert file", e);
            throw new RuntimeException(e);

        }
    }

}