package io.polivakha.mojo.properties.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tema Nikulin
 */
public class PropertyLogger {

    private static final Logger log = LoggerFactory.getLogger(PropertyLogger.class);
    private final Set<String> existedPropertiesKeys = new HashSet<>();

    /**
     * Log warn message if new property already was stored in Properties
     */
    public void verifyExistedProperties(InputStream stream) throws IOException {
        Set<Pair<String, String>> newPropertiesKeys = getNewPropertiesKeys(stream);

        newPropertiesKeys.forEach(newProperty -> {
            if (existedPropertiesKeys.contains(newProperty.getKey())) {
                log.warn("Property {} is already defined. New value of property: {} override current value", newProperty.getKey(), newProperty.getValue());
            } else {
                existedPropertiesKeys.add(newProperty.getKey());
            }
        });
    }

    private Set<Pair<String, String>> getNewPropertiesKeys(InputStream stream) throws IOException {
        List<String> newProperties = IOUtils.readLines(stream, StandardCharsets.UTF_8);

        return newProperties.stream()
                .map(property -> {
                    List<String> list = Arrays.asList(property.split("="));
                    Assert.isTrue(list.size() == 2, "Invalid property definition");

                    return Pair.of(list.get(0), list.get(1));
                })
                .collect(Collectors.toSet());
    }

    Set<String> getExistedPropertiesKeys() {
        return existedPropertiesKeys;
    }
}
