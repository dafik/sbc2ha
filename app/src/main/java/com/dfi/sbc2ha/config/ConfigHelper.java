package com.dfi.sbc2ha.config;

import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.config.loader.ConfigLoaderJackson;
import com.dfi.sbc2ha.helper.JarHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigHelper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Map<String, FileStat> statMap;

    public static AppConfig loadConfig(String configLocation) throws IOException, URISyntaxException {

        Logger.info("Loading config");

        File configFile = resolveFile(configLocation);
        configLocation = configFile.getPath();

        if (isCached(configLocation)) {
            if (isNotModified(configFile)) {
                try {
                    return loadFromCache(configFile);
                } catch (Exception ignored) {

                }

            }
        }

        AppConfig appConfig = ConfigLoaderJackson.loadConfig(configLocation);
        saveCache(configLocation, appConfig);


        return appConfig;
    }

    private static File resolveFile(String configLocation) throws FileNotFoundException, URISyntaxException {
        File absoluteFile = new File(configLocation).getAbsoluteFile();
        if (absoluteFile.exists()) {
            return absoluteFile;
        }
        URL resource = ConfigHelper.class.getClassLoader().getResource(configLocation);

        if (resource == null) {
            throw new FileNotFoundException(configLocation);
        }

        return new File(resource.toURI().getPath());

    }


    private static void saveCache(String originalLocation, AppConfig appConfig) {


        File originalFile = new File(originalLocation).getAbsoluteFile();

        String cacheFileName = getCacheFileName(originalFile);
        Logger.info("save in cache file: {}", cacheFileName);

        File cacheFile = new File(cacheFileName);
        try {
            mapper
                    .registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                    .writerWithDefaultPrettyPrinter().writeValue(cacheFile, appConfig);
        } catch (IOException e) {
            e.printStackTrace();

        }
        try {
            updateStats(originalLocation, originalFile.lastModified(), cacheFile.getPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
    private static void saveCacheYaml(String originalLocation, AppConfig appConfig) {


        File originalFile = new File(originalLocation).getAbsoluteFile();

        String cacheFileName = getCacheFileNameYaml(originalFile);
        Logger.info("save in cache file: {}", cacheFileName);

        File cacheFile = new File(cacheFileName);
        try {
            mapper
                    .registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                    .writerWithDefaultPrettyPrinter().writeValue(cacheFile, appConfig);
        } catch (IOException e) {
            e.printStackTrace();

        }
        try {
            updateStats(originalLocation, originalFile.lastModified(), cacheFile.getPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

    private static void updateStats(String originalLocation, long fileTime, String cachedLocation) {
        Map<String, FileStat> stats = getStats();

        FileStat fileStat = new FileStat();
        fileStat.setCachedFileName(cachedLocation);
        fileStat.setOriginalFileName(originalLocation);
        fileStat.setLastModified(fileTime);


        stats.put(originalLocation, fileStat);

        saveStats(stats);
    }

    private static Map<String, FileStat> getStats() {
        if (statMap != null) {
            return statMap;
        }
        String statsFileLocation = JarHelper.getTempDir() + "/cacheStats.json";
        File statsFile = new File(statsFileLocation).getAbsoluteFile();
        if (!statsFile.exists()) {
            return new HashMap<>();
        }
        ObjectMapper mapper = new ObjectMapper();
        MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, FileStat.class);
        try {
            statMap = mapper.readValue(new FileInputStream(statsFile), type);
            return statMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void saveStats(Map<String, FileStat> stats) {
        String statsFileLocation = JarHelper.getTempDir() + "/cacheStats.json";
        File statsFile = new File(statsFileLocation).getAbsoluteFile();
        try {
            // Serialize Java object info JSON file.
            mapper.writerWithDefaultPrettyPrinter().writeValue(statsFile, stats);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    private static String getCacheFileName(File originalFile) {

        File absoluteFile = originalFile.getAbsoluteFile();
        String directory = JarHelper.getTempDir();
        String fileName = absoluteFile.getName();

        String file = fileName;
        String extension = "json";

        if (fileName.contains(".")) {
            String[] parts = fileName.split("\\.");
            file = parts[0];
        }

        return directory + "/" + String.join(".", List.of(file + "Optimized", extension));

    }
    private static String getCacheFileNameYaml(File originalFile) {

        File absoluteFile = originalFile.getAbsoluteFile();
        String directory = JarHelper.getTempDir();
        String fileName = absoluteFile.getName();

        String file = fileName;
        String extension = "yaml";

        if (fileName.contains(".")) {
            String[] parts = fileName.split("\\.");
            file = parts[0];
        }

        return directory + "/" + String.join(".", List.of(file + "Optimized", extension));

    }

    private static AppConfig loadFromCache(File configFile) throws IOException {
        String cacheFileName = getStats().get(configFile.getPath()).cachedFileName;

        Logger.info("Loading from cache file {}", cacheFileName);
        mapper.registerModule(new JavaTimeModule());
        return mapper.readValue(new FileInputStream(cacheFileName), AppConfig.class);
    }

    private static boolean isNotModified(File configFile) {
        FileStat fileStat = getStats().get(configFile.getPath());
        return configFile.lastModified() <= fileStat.getLastModified();
    }

    private static boolean isCached(String configFile) {
        return getStats().containsKey(configFile);
    }

    @Data
    private static class FileStat {
        private Long lastModified;
        private String originalFileName;
        private String cachedFileName;

    }
}
