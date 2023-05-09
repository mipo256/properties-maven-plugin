package io.polivakha.mojo.properties.loader;

import java.util.List;
import java.util.Properties;

/**
 * Represents an abstract resource loader that is capable to load properties from some resource
 *
 * @param <RESOURCE> - the abstract resource from which properties should be loaded
 * @author Mikhail Polivakha
 */
public interface PropertiesLoader<RESOURCE> {

    /**
     * Loading the properties from the specified resources. Note that the order of {@code resources}
     * list elements <b>matters</b>, because each next resource, in case of conflict, will override
     * the properties loaded from previous resource. for example, if there is a property 'abc' defined in
     * 3 resources, then the value of 'abc' property in the last resource in the passed list will be contained
     * in the returned {@link Properties} object.
     *
     * @param resources - resources list, from which the {@link Properties} should be loaded
     * @return Properties object, containing the loaded properties
     */
    Properties loadProperties(List<RESOURCE> resources);
}