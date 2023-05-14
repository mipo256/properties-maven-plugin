package io.polivakha.mojo.properties.loader;

import java.util.List;
import java.util.Properties;

import io.polivakha.mojo.properties.models.Resource;

/**
 * Common class for all {@link PropertiesLoader}'s. Assembles the properties from each resource one by one
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

    /**
     * Method to be implemented by child classes, that will do the actual proeprties loading
     * @param resources - abstract {@link Resource} from which the properties should be loaded
     * @return loaded {@link Properties} object from provided {@link Resource}
     */
    protected abstract Properties loadInternally(RESOURCE resources);
}
