package io.polivakha.mojo.properties.models;

import java.io.IOException;
import java.io.InputStream;

public abstract class Resource {

    private InputStream stream;

    public abstract boolean canBeOpened();

    protected abstract InputStream openStream() throws IOException;

    public InputStream getInputStream() throws IOException {
        if ( stream == null ) {
            stream = openStream();
        }
        return stream;
    }
}
