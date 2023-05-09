package io.polivakha.mojo.properties.models;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;

public class UrlResource extends Resource {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final String SLASH_PREFIX = "/";

    private final URL url;

    private boolean isMissingClasspathResouce = false;

    private String classpathUrl;

    public UrlResource( String url ) throws MojoExecutionException {
        if ( url.startsWith( CLASSPATH_PREFIX ) ) {
            String resource = url.substring( CLASSPATH_PREFIX.length() );
            if ( resource.startsWith( SLASH_PREFIX ) ) {
                resource = resource.substring( 1 );
            }
            this.url = getClass().getClassLoader().getResource( resource );
            if ( this.url == null ) {
                isMissingClasspathResouce = true;
                classpathUrl = url;
            }
        } else {
            try {
                this.url = new URL( url );
            } catch ( MalformedURLException e ) {
                throw new MojoExecutionException( "Badly formed URL " + url + " - " + e.getMessage() );
            }
        }
    }

    public boolean canBeOpened() {
        if ( isMissingClasspathResouce ) {
            return false;
        }
        try {
            openStream();
        }
        catch ( IOException e ) {
            return false;
        }
        return true;
    }

    protected InputStream openStream() throws IOException {
        return new BufferedInputStream( url.openStream() );
    }

    public String toString() {
        if ( !isMissingClasspathResouce ) {
            return "URL " + url.toString();
        }
        return classpathUrl;
    }
}
