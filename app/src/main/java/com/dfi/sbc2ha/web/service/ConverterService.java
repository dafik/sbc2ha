package com.dfi.sbc2ha.web.service;

import com.dfi.sbc2ha.config.sbc2ha.definition.AppConfig;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.dfi.sbc2ha.config.converter.Converter.convertFile;

@Slf4j
public class ConverterService {
    public static String convert(Path path) {

        try {


            String location = path.toString();
            File file = new File(location);
            String mimeType = URLConnection.getFileNameMap().getContentTypeFor(location);
            //MagicMatch match = Magic.getMagicMatch(file, true);
            //String mimeType = match.getMimeType();

            if (mimeType.equals("application/zip")) {
                location = unzip(file)+"/config.yaml";
            }


            AppConfig appConfig = convertFile(location, "boneio", null, null);

            String s = JsonMapper.builder()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                    .configure(EnumFeature.WRITE_ENUMS_TO_LOWERCASE, true)
                    .addModule(new JavaTimeModule())
                    .build()
                    .writerWithDefaultPrettyPrinter().writeValueAsString(appConfig);


            String s1 = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(appConfig);
            return s;
        } catch (Exception e) {
            log.error("error convert file", e);
            throw new RuntimeException(e);

        }
    }

    private static String unzip(File file) throws IOException {
        File destDir = new File("/tmp/convert-" + file);

        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
        return destDir.toString();
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}