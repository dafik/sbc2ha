package com.dfi.sbc2ha.util;

import com.dfi.sbc2ha.config.sbc2ha.definition.LoggerConfig;
import com.dfi.sbc2ha.services.log.ReconfigurableLoggingProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {
    public static final String TINYLOG_LEVEL_PROPERTY = "tinylog.level";
    public static final String TINYLOG_LEVEL_PREFIX_PROPERTY = "tinylog.level@";
    public static final String TINYLOG_WRITER_CONSOLE_PREFIX_PROPERTY = "tinylog.writerConsole.";

    public static void configureLogging(LoggerConfig logger) {
        if (logger == null) {
            return;
        }

        if (logger.getDefaultLevel() != null) {
            System.setProperty(TINYLOG_LEVEL_PROPERTY, logger.getDefaultLevel().toString());
        } else {
            System.clearProperty(TINYLOG_LEVEL_PROPERTY);
        }
        System.getProperties().forEach((o, o2) -> {
            String key = o.toString();
            if (key.startsWith(TINYLOG_LEVEL_PREFIX_PROPERTY) || key.startsWith(TINYLOG_WRITER_CONSOLE_PREFIX_PROPERTY)) {
                System.clearProperty(key);
            }
        });

        logger.getLogs().forEach((pkg, level) -> {
            System.setProperty(TINYLOG_LEVEL_PREFIX_PROPERTY + pkg, level.level);
        });
        logger.getWriter().forEach((pkg, value) -> {
            System.setProperty(TINYLOG_WRITER_CONSOLE_PREFIX_PROPERTY + pkg, value);
        });

        try {
            ReconfigurableLoggingProvider.reload();
        } catch (InterruptedException e) {
            log.info("on interrupt");
        } catch (ReflectiveOperationException e) {
            log.error("configure loging failed interrupt", e);
        }
    }
}
