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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

class ExpansionBuffer {

    private boolean isFullyResolved;

    private final StringBuilder resolved = new StringBuilder();

    /**
     * RegExp pattern to locate the leftest nested property
     */
    private static final Pattern NESTED_PROPERTY_PATTERN = Pattern.compile("\\$\\{(.*?)}");

    @NonNull
    private String unresolved;

    public ExpansionBuffer(String unresolved) {
        this.unresolved = StringUtils.defaultString(unresolved);
        this.isFullyResolved = !hasMoreLegalPlaceholders();
    }

    private boolean hasMoreLegalPlaceholders() {
        return NESTED_PROPERTY_PATTERN.matcher(unresolved).matches();
    }

    @Nullable
    public String extractNextPropertyKey() {
        String nextKeyToSearchFor = null;
        Matcher matcher = NESTED_PROPERTY_PATTERN.matcher(unresolved);
        if (matcher.find()) {
            nextKeyToSearchFor = matcher.group(1);
        } else {
            resolved.append(unresolved);
            isFullyResolved = true;
        }
        return nextKeyToSearchFor;
    }

    public void moveResolvedPartToNextProperty() {
        int start = unresolved.indexOf("${");
        resolved.append(unresolved, 0, start);
        unresolved = unresolved.substring(unresolved.indexOf("}", start) + 1);
    }

    public String getFullyResolved() {
        if (!isFullyResolved) {
            throw new IllegalStateException("Property value is not fully resolved yet");
        }
        return resolved.toString();
    }

    @Override
    public String toString() {
        return "ExpansionBuffer{" + "isFullyResolved=" + isFullyResolved + ", resolved=" + resolved + ", unresolved='" + unresolved + '\'' + '}';
    }

    public void add(String resolvedProperty) {
        resolved.append(resolvedProperty);
    }
}