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

import java.util.LinkedHashSet;
import java.util.Set;

import io.polivakha.mojo.properties.exception.PropertyCircularDefinitionException;

public class CircularDefinitionPreventer {

    private final Set<String> keysUsed;

    public CircularDefinitionPreventer(Set<String> usedKeySet) {
        this.keysUsed = usedKeySet;
    }

    public CircularDefinitionPreventer() {
        this(new LinkedHashSet<>());
    }

    /**
     * Checks if property is already visited
     * @param key - key which defines the property
     * @return true if property was already visited during value resolution, false otherwise
     */
    public boolean isPropertyAlreadyVisited(String key) {
        return keysUsed.contains(key);
    }

    /**
     * Check that the expanded property does not provide a circular definition.
     * For instance:
     * <p>
     * some.key = ${some.property}
     * some.property = ${some.key}
     * <p>
     * This is a circular properties definition
     * @param key The key.
     * @return {@link CircularDefinitionPreventer}
     */
    public CircularDefinitionPreventer cloneWithAdditionalKey( String key ) {
        var keysUsedCopy = new LinkedHashSet<>(keysUsed);
        keysUsedCopy.add(key);
        return new CircularDefinitionPreventer(keysUsedCopy);
    }

    public void throwCircularDefinitionException() {
        StringBuilder buffer = new StringBuilder( "Circular property definition detected: \n");
        keysUsed.forEach(key -> buffer.append(key).append(" --> "));
        buffer.append(keysUsed.stream().findFirst());
        throw new PropertyCircularDefinitionException( buffer.toString() );
    }
}
