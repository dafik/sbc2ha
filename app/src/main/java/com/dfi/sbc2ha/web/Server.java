package com.dfi.sbc2ha.web;

import com.dfi.sbc2ha.event.bus.ReloadCommand;
import com.dfi.sbc2ha.event.bus.RestartCommand;
import com.dfi.sbc2ha.event.bus.StopCommand;
import com.dfi.sbc2ha.services.config.ConfigProvider;
import com.dfi.sbc2ha.web.service.CacheService;
import com.dfi.sbc2ha.web.service.ConfigService;
import com.dfi.sbc2ha.web.service.ConverterService;
import com.dfi.sbc2ha.web.websocket.DeviceWebsocket;
import com.dfi.sbc2ha.web.websocket.LogWebsocket;
import com.dfi.sbc2ha.web.websocket.StatesWebsocket;
import org.eclipse.jetty.http.HttpStatus;
import org.greenrobot.eventbus.EventBus;
import spark.Request;
import spark.Route;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static spark.Spark.*;


public class Server {
    private final String configFile;
    private final EventBus eventBus = EventBus.getDefault();

    public Server(String configFile) {
        this.configFile = configFile;
        port(8080);
        staticFiles.location("/editor");
        webSocket("/ws/device", DeviceWebsocket.class);
        webSocket("/ws/logs", LogWebsocket.class);
        webSocket("/ws/states", StatesWebsocket.class);

        get("/api/config/json", getConfigJson());
        get("/api/config/yaml", getConfigYaml());
        get("/api/reload", reloadApp());
        get("/api/restart", restartApp());
        get("/api/stop", stopApp());
        get("/api/clear/states", clearStates());
        get("/api/clear/config", clearConfig());

        post("/api/write/config", writeConfig());
        post("/api/write/cache", writeCache());
        post("/api/convert", convert());

        notFound((req, res) -> {
            res.type("application/json");
            return "{\"message\":\"Custom 404\"}";
        });
    }

    private  Route getConfigJson() {
        return (request, res) -> {
            String filePath = ConfigProvider.getCachedFileName(configFile);
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            HttpServletResponse raw = res.raw();

            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();


            return res.raw();
        };
    }

    private Route renderIndex() {
        return (request, res) -> {
            String body = Files.readString(Path.of("/editor/index.html"));
            res.status(200);
            return body;
        };
    }

    private Route convert() {
        return (request, response) -> {
            try {





                List<Path> uploadFiles = getUploadFiles(request);
                if (uploadFiles.size() == 1) {
                    return ConverterService.convert(uploadFiles.get(0));
                }


            } catch (RuntimeException e) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return e.getMessage();
            }
            return "";
        };
    }

    private Route writeCache() {
        return (request, response) -> {
            try {
                String inputFileName = "cache";
                Path config = getUploadFile(request, inputFileName);
                ConfigService.writeCache(config);


            } catch (Exception e) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return e.getMessage();
            }
            return "";
        };
    }

    private Route writeConfig() {
        return (request, response) -> {
            try {
                String inputFileName = "config";
                Path config = getUploadFile(request, inputFileName);
                ConfigService.writeConfig(config);

            } catch (Exception e) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return e.getMessage();
            }
            return "";
        };
    }

    private Route clearConfig() {
        return (request, response) -> {
            boolean b = CacheService.clearConfig();
            if (b) {
                response.status(HttpStatus.NO_CONTENT_204);
                return "";
            } else {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
            return null;
        };
    }

    private Route clearStates() {
        return (request, response) -> {

            boolean b = CacheService.clearStates();
            if (b) {
                response.status(HttpStatus.NO_CONTENT_204);
                return "";
            } else {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            }

            return null;
        };
    }

    private Route getConfigYaml() {
        return (request, res) -> {
            res.type("text/yaml");
            byte[] bytes = Files.readAllBytes(Paths.get(configFile));
            HttpServletResponse raw = res.raw();

            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();


            return res.raw();
        };
    }

    private Route restartApp() {
        return (request, response) -> {
            eventBus.post(new RestartCommand());
            response.status(201);
            return "";
        };
    }

    private Route stopApp() {
        return (request, response) -> {
            eventBus.post(new StopCommand());
            response.status(201);
            return "";
        };

    }

    private Route reloadApp() {
        return (request, response) -> {
            eventBus.post(new ReloadCommand());
            response.status(201);
            return "";
        };
    }


    public void close() {
        stop();
        awaitStop();
    }

    private List<Path> getUploadFiles(Request req) throws IOException, ServletException {
        File uploadDir = new File("/tmp/upload");
        uploadDir.mkdir();


        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));


        HttpServletRequest raw = req.raw();
        Collection<Part> parts = raw.getParts();

        List<Path> files = new ArrayList<>();

        for (Part part : parts) {
            Path tempFile = Files.createTempFile(uploadDir.toPath(), "", part.getSubmittedFileName());
            try (InputStream input = part.getInputStream()) { // getPart needs to use same "inputFileName" as input field in form
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            files.add(tempFile);
        }
        return files;

    }

    private Path getUploadFile(Request req, String inputFileName) throws IOException, ServletException {
        File uploadDir = new File("/tmp/upload");
        uploadDir.mkdir();


        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

        HttpServletRequest raw = req.raw();
        Part part = raw.getPart(inputFileName);
        Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "");
        try (InputStream input = part.getInputStream()) { // getPart needs to use same "inputFileName" as input field in form
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

}

