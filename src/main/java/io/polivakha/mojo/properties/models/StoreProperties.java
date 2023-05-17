package io.polivakha.mojo.properties.models;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class StoreProperties extends Properties {

    public StoreProperties(Properties defaults) {
        this.putAll(defaults);
    }

    public void sortedStore(OutputStream out, String comments) throws IOException {

        Properties sortedProps = new Properties() {
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                Set<Map.Entry<Object, Object>> sortedSet = new TreeSet<>(Comparator.comparing(o -> o.getKey().toString()));
                sortedSet.addAll(super.entrySet());
                return sortedSet;
            }

            @Override
            public Set<Object> keySet() {
                return new TreeSet<>(super.keySet());
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }

        };

        sortedProps.putAll(this);
        sortedProps.store(out, comments);
    }
}
