package org.codehaus.mojo.properties.utils;

import java.util.Collection;

public final class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}