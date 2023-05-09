package org.codehaus.mojo.properties.loaders;

import java.util.List;
import java.util.Properties;

/**
 * Common class for all {@link PropertiesLoader's}. Assembles the properties from each resource one by one
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractPropertiesLoader<RESOURCE> implements PropertiesLoader<RESOURCE> {

    @Override
    public Properties loadProperties(List<RESOURCE> resources) {
        Properties result = new Properties();
        for (RESOURCE resource : resources) {
            Properties properties = loadInternally(resource);
            result.putAll(properties);
        }
        return result;
    }

    protected abstract Properties loadInternally(RESOURCE resources);
}
