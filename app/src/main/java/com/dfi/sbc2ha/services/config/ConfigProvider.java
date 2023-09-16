package com.dfi.sbc2ha.services.config;

import com.dfi.sbc2ha.Sbc2ha;
import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.dfi.sbc2ha.services.config.loader.ConfigLoaderJackson;
import com.dfi.sbc2ha.util.Jar;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConfigProvider {
    private static final ObjectMapper mapper = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
            .configure(EnumFeature.WRITE_ENUMS_TO_LOWERCASE, true)
            .addModule(new JavaTimeModule())
            .build();
    private static Map<String, FileStat> statMap;

    public static AppConfig loadConfig(String configLocation) throws IOException, URISyntaxException {

        log.info("Loading config");

        File configFile = resolveFile(configLocation);
        configLocation = configFile.getPath();


        if (isCached(configLocation)) {
            if (isNotModified(configFile)) {
                try {
                    return loadFromCache(configFile);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        }

        AppConfig appConfig;
        if (isBoneIoConfig(configLocation)) {
            if (isNotConverted(configLocation)) {
                appConfig = convertFile(configLocation);
            } else {
                configLocation = "";
                appConfig = ConfigLoaderJackson.loadConfig(configLocation);
            }
        } else {
            appConfig = ConfigLoaderJackson.loadConfig(configLocation);
        }


        saveCache(configLocation, appConfig);


        return appConfig;
    }

    private static AppConfig convertFile(String configLocation) {
        return null;
    }

    private static boolean isNotConverted(String configLocation) {
        return false;
    }

    private static boolean isBoneIoConfig(String configLocation) {
        return false;
    }

    private static File resolveFile(String configLocation) throws FileNotFoundException, URISyntaxException {
        File absoluteFile = new File(configLocation).getAbsoluteFile();
        if (absoluteFile.exists()) {
            return absoluteFile;
        }
        URL resource = ConfigProvider.class.getClassLoader().getResource(configLocation);

        if (resource == null) {
            throw new FileNotFoundException(configLocation);
        }

        return new File(resource.toURI().getPath());

    }


    public static void saveCache(String originalLocation, AppConfig appConfig) {
        File originalFile = new File(originalLocation).getAbsoluteFile();

        String cacheFileName = getCacheFileName(originalFile);
        log.info("save in cache file: {}", cacheFileName);

        File cacheFile = new File(cacheFileName);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(cacheFile, appConfig);
        } catch (IOException e) {
            e.printStackTrace();

        }
        try {
            updateStats(originalLocation, originalFile.lastModified(), cacheFile.getPath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean clearCache() {
        String configFile = Sbc2ha.getConfigFile();
        File originalFile = new File(configFile).getAbsoluteFile();
        String cacheFileName = getCacheFileName(originalFile);
        File configCache = new File(cacheFileName).getAbsoluteFile();
        try {
            new FileWriter(configCache, false).close();
            statMap.remove(configFile);
            saveStats(statMap);
            log.info("clear cache succeed");
            return true;
        } catch (IOException e) {
            log.error("clear cache failed", e);
        }
        return false;

    }

    private static void saveCacheYaml(String originalLocation, AppConfig appConfig) {


        File originalFile = new File(originalLocation).getAbsoluteFile();

        String cacheFileName = getCacheFileNameYaml(originalFile);
        log.info("save in cache file: {}", cacheFileName);

        File cacheFile = new File(cacheFileName);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(cacheFile, appConfig);
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
        String statsFileLocation = Jar.getTempDir() + "/cacheStats.json";
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
        String statsFileLocation = Jar.getTempDir() + "/cacheStats.json";
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
        String directory = Jar.getTempDir();
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
        String directory = Jar.getTempDir();
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
        String cacheFileName = getCachedFileName(configFile.getPath());

        log.info("Loading from cache file {}", cacheFileName);
        return mapper.readValue(new FileInputStream(cacheFileName), AppConfig.class);
    }

    public static String getCachedFileName(String configFile) {
        return getStats().get(configFile).cachedFileName;
    }

    public static AppConfig loadFromUploadedCache(Path configFile) throws IOException {
        String cacheFileName = configFile.toString();

        log.info("Loading from cache file {}", cacheFileName);
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
