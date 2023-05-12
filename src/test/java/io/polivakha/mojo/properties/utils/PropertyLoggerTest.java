package io.polivakha.mojo.properties.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PropertyLoggerTest {

    private PropertyLogger propertyLogger;
    @Before
    public void setUp() {
        propertyLogger = new PropertyLogger();
    }

    @Test
    public void givenOneExistedKey_whenLoggingExistedProperties_thenShouldReturnOneLogWarnMessage() throws IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(PropertyLogger.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        propertyLogger.getExistedPropertiesKeys().add("existed.key");
        Path tempDirectory = Files.createTempDirectory("log-temp");
        Path file = Files.createFile(Paths.get(tempDirectory.toString(), "log-file.properties"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            writer.write("existed.key=1337\n");
            writer.write("non-existed.key=1337");
        }
        InputStream inputStream = Files.newInputStream(file);


        propertyLogger.verifyExistedProperties(inputStream);

        List<ILoggingEvent> logsList = listAppender.list;
        Assert.assertEquals(Level.WARN, logsList.get(0).getLevel());
        assertEquals(2, propertyLogger.getExistedPropertiesKeys().size());
    }
}