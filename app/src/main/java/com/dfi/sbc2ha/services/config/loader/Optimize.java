package com.dfi.sbc2ha.services.config.loader;

import com.dfi.sbc2ha.util.Jar;
import com.dfi.sbc2ha.helper.ProcessRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class Optimize {
    public static boolean checkIncludes(String inputStream) {
        String text;
        try {
            text = Files.readString(Path.of(inputStream).toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean contains = text.contains("!include");
        if (contains) {
            log.info("config file contains !include");
        }
        return contains;
    }

    public static String convert(String configPath) {
        log.info("trying convert");
        if (!checkPythonYaml()) {
            setupPytonYaml();
        }

        return runPythonConversion(configPath);

    }

    private static String runPythonConversion(String configPath) {
        log.info("call conversion");
        String pwd = System.getProperty("user.dir");

        String pytonDir = getPytonDir();
        String tool = getPytonDir() + "/yaml_util.py";
        if (Jar.isRunInJar() && !Files.exists(Path.of(tool))) {

            InputStream src = ClassLoader.getSystemResourceAsStream("tools/yaml_util.py");
            File target = new File(tool);
            try (FileOutputStream outputStream = new FileOutputStream(target, false)) {
                int read;
                byte[] bytes = new byte[8192];
                while ((read = src.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String script = Jar.isRunInJar() ?
                tool :
                pwd + "/src/main/resources/tools/yaml_util.py";


        List<String> lines = List.of(
                "source " + pytonDir + "/venv/bin/activate",
                "python3 " + script + " " + configPath
        );
        List<String> commands = List.of("/bin/bash", "-c", String.join(";", lines));


        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commands);

        ProcessRunner.runSystemCommand(builder, "Python conversion failed");
        log.info("conversion done");

        getNewPath(configPath);
        return getNewPath(configPath);

    }

    private static String getNewPath(String configPath) {
        File originalFile = new File(configPath);
        File absoluteFile = originalFile.getAbsoluteFile();
        String directory = absoluteFile.getParent();
        String fileName = absoluteFile.getName();

        String file = fileName;
        String extension = "yaml";

        if (fileName.contains(".")) {
            String[] parts = fileName.split("\\.");
            file = parts[0];
        }

        return directory + "/" + String.join(".", List.of(file + "_optimized", extension));
    }

    private static void setupPytonYaml() {
        log.info("python setup start");
        String pytonDir = getPytonDir();
        List<String> lines = List.of(
                "export TMP_YAML=" + pytonDir,
                "mkdir -p $TMP_YAML",
                "python3 -m venv $TMP_YAML/venv",
                "source $TMP_YAML/venv/bin/activate",
                "pip3 install PyYAML"
        );
        List<String> commands = List.of("/bin/bash", "-c", String.join(";", lines));

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(commands);
        ProcessRunner.runSystemCommand(builder, "Setup pyton failed");


        log.info("python setup done");
    }

    private static String getPytonDir() {
        return Jar.getTempDir() + "/python";
    }

    private static boolean checkPythonYaml() {
        Path path = Paths.get(getPytonDir());
        return Files.exists(path);
    }

}
