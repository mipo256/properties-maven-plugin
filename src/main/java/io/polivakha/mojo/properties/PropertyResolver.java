package io.polivakha.mojo.properties;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

public class PropertyResolver {

    public String getPropertyValue(String key, Properties mavenProjectProperties, Properties environment) {
        return this.getPropertyValue(key, mavenProjectProperties, environment, new CircularDefinitionPreventer());
    }

    /**
     * Retrieves a property value, replacing values like ${token} using the Properties to look them up. Shamelessly
     * adapted from:
     * http://maven.apache.org/plugins/maven-war-plugin/xref/org/apache/maven/plugin/war/PropertyUtils.html It will
     * leave unresolved properties alone, trying for System properties, and environment variables and implements
     * reparsing (in the case that the value of a property contains a key), and will not loop endlessly on a pair like
     * test = ${test}
     *
     * @param key property key
     * @param mavenProjectProperties project properties
     * @param environment environment variables
     * @return resolved property value, or property placeholder, if it was not resolved
     * @throws IllegalArgumentException when properties are circularly defined
     */
    public String getPropertyValue(String key, Properties mavenProjectProperties, Properties environment, CircularDefinitionPreventer circularDefinitionPreventer) {

        if (circularDefinitionPreventer.isPropertyAlreadyVisited(key)) {
            circularDefinitionPreventer.throwCircularDefinitionException();
        }

        String rawValue = fromPropertiesThenSystemThenEnvironment(key, mavenProjectProperties, environment);

        if (StringUtils.isEmpty(rawValue)) {
            return null;
        }

        ExpansionBuffer buffer = new ExpansionBuffer(rawValue);
        String newKey;

        while ((newKey = buffer.extractNextPropertyKey()) != null) {
            buffer.moveResolvedPartToNextProperty();
            String newValue = getPropertyValue(newKey, mavenProjectProperties, environment, circularDefinitionPreventer.cloneWithAdditionalKey(key));
            if (newValue == null) {
                buffer.add("${" + newKey + "}");
            } else {
                buffer.add(newValue);
            }
        }

        return buffer.getFullyResolved();
    }

    private String fromPropertiesThenSystemThenEnvironment( String key, Properties properties, Properties environment ) {
        String value = StringUtils.defaultIfEmpty(
          properties.getProperty(key),
          System.getProperty(key)
        );

        // try environment variable
        if ( value == null && key.startsWith( "env." ) && environment != null ) {
            value = environment.getProperty( key.substring( 4 ) );
        }

        return value;
    }
}
