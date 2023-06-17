package io.polivakha.mojo.properties.models;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileResource extends Resource {
    private final File file;

    public FileResource( File file )
    {
        this.file = file;
    }

    public boolean canBeOpened() {
        return file.exists();
    }

    protected InputStream openStream() throws IOException {
        return new BufferedInputStream( new FileInputStream( file ) );
    }

    public String toString()
    {
        return "File: " + file;
    }
}
