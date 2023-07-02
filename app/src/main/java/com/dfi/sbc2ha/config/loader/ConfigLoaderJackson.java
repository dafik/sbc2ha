package com.dfi.sbc2ha.config.loader;

import com.dfi.sbc2ha.App;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.tinylog.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class ConfigLoaderJackson {


    public static AppConfig loadConfig(String configPath) {

        InputStream io;

        if (Optimize.checkIncludes(configPath)) {
            String newPath = Optimize.convert(configPath);
            io = getInputFileStream(newPath);
        } else {
            io = getInputFileStream(configPath);
        }


        YAMLFactory streamFactory = new YAMLFactory();
        ObjectMapper om = JsonMapper.builder(streamFactory)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
        om.registerModule(new JavaTimeModule());

        try {
            Logger.info("parsing config");
            AppConfig appConfig = om.readValue(io, AppConfig.class);
            Logger.info("config parsed");

            return appConfig;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //polimorfizm https://stackoverflow.com/questions/24263904/deserializing-polymorphic-types-with-jackson-based-on-the-presence-of-a-unique-p
        // https://stackabuse.com/reading-and-writing-yaml-files-in-java-with-jackson/
        //https://www.baeldung.com/jackson-yaml
    }

    public static InputStream getInputFileStream(String location) {
        InputStream resource;
        try {
            resource = new FileInputStream(location);
        } catch (FileNotFoundException ignored) {
            URL resource1 = App.class.getClassLoader().getResource(location);
            try {
                assert resource1 != null;
                Path file = Paths.get(resource1.toURI().getPath());
                BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                FileTime message = attr.lastModifiedTime();
                Logger.info(message);

            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
            resource = App.class.getClassLoader().getResourceAsStream(location);
        }
        return resource;
    }
}
