package io.polivakha.mojo.properties.utils;

import org.apache.maven.plugin.logging.Log;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author Tema Nikulin
 */
public class DuplicatePropertyVerifier {

    public void verifyExistedProperties(Set<Entry<Object, Object>> oldProperties, Properties newProperties, Log log) {
        if (log.isWarnEnabled()) {

            oldProperties.forEach(oldProperty -> {
                String propertyKey = (String) oldProperty.getKey();
                String newPropertyValue = newProperties.getProperty(propertyKey);

                if (newPropertyValue != null && !newPropertyValue.equals(oldProperty.getValue())) {
                    log.warn(String.format("Property %s is already defined. New value %s redefined current value %s", propertyKey, newPropertyValue, oldProperty.getValue()));
                }
            });

        }

    }

}
