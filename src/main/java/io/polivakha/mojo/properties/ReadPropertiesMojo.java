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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import io.polivakha.mojo.properties.models.FileResource;
import io.polivakha.mojo.properties.models.Resource;
import io.polivakha.mojo.properties.models.UrlResource;
import io.polivakha.mojo.properties.utils.PathParser;

/**
 * The read-project-properties goal reads property files and URLs and stores the properties as project properties. It
 * serves as an alternate to specifying properties in pom.xml. It is especially useful when making properties defined in
 * a runtime resource available at build time.
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @author <a href="mailto:Krystian.Nowak@gmail.com">Krystian Nowak</a>
 * @author Mikhail Polivakha
 */
@Mojo( name = "read-project-properties", defaultPhase = LifecyclePhase.NONE, threadSafe = true )
public class ReadPropertiesMojo extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    /**
     * The properties files that will be used when reading properties.
     */
    @Parameter
    private File[] files = new File[0];

    @Parameter(name = "includes", required = false, alias = "includes")
    private String[] includes = new String[0];

    private final PathParser pathParser;

    public ReadPropertiesMojo() {
        this.pathParser = new PathParser();
    }

    /**
     * @param files The files to set for tests.
     */
    public void setFiles( File[] files ) {
        if (files == null) {
            this.files = new File[0];
        } else {
            this.files = new File[files.length];
            System.arraycopy( files, 0, this.files, 0, files.length );
        }
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    /**
     * The URLs that will be used when reading properties. These may be non-standard URLs of the form
     * <code>classpath:com/company/resource.properties</code>. Note that the type is not <code>URL</code> for this
     * reason and therefore will be explicitly checked by this Mojo.
     */
    @Parameter
    private String[] urls = new String[0];

    /**
     * If the plugin should be quiet if any of the files was not found
     */
    @Parameter( defaultValue = "false" )
    private boolean quiet;

    /**
     * Prefix that will be added before name of each property.
     * Can be useful for separating properties with same name from different files.
     */
    @Parameter
    private String keyPrefix = null;

    public void setKeyPrefix( String keyPrefix ) {
        this.keyPrefix = keyPrefix;
    }

    @Parameter( defaultValue = "false", property = "prop.skipLoadProperties" )
    private boolean skipLoadProperties;

    /**
     * Used for resolving property placeholders.
     */
    private final PropertyResolver resolver = new PropertyResolver();

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if ( !skipLoadProperties ) {
            loadFiles();
            loadUrls();
            loadFilesByPattern();
            resolveProperties();
        } else {
            getLog().warn( "The properties are ignored" );
        }
    }

    private void loadFiles() throws MojoExecutionException {
        for ( File file : files ) {
            load( new FileResource( file ) );
        }
    }

    private void loadFilesByPattern() throws MojoExecutionException {
        if (includes == null) {
            return;
        }

        for (String antPattern : includes) {
            if (antPattern == null || antPattern.isEmpty()) {
                throw new MojoExecutionException("Provided <pattern/> element value is empty. Please, put corresponding ant path pattern in this element");
            }

            try (Stream<Path> pathStream = pathParser.streamFilesMatchingAntPath(antPattern)) {

                List<FileResource> fileResources = pathStream.map(Path::toFile)
                  .peek(it -> getLog().debug(String.format("Found potential properties file '%s' by ant path pattern : '%s'", it, antPattern)))
                  .map(FileResource::new)
                  .collect(Collectors.toList());

                for (FileResource fileResource : fileResources) {
                    loadProperties(fileResource);
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Error while traversing file tree to find properties files by ant pattern", e);
            }
        }
    }

    private void loadUrls() throws MojoExecutionException {
        for ( String url : urls ) {
            load( new UrlResource( url ) );
        }
    }

    private void load( Resource resource ) throws MojoExecutionException {
        if ( resource.canBeOpened() ) {
            loadProperties( resource );
        } else {
            missing( resource );
        }
    }

    private void loadProperties( Resource resource ) throws MojoExecutionException {
        try {
            getLog().debug( "Loading properties from " + resource );

            try ( InputStream stream = resource.getInputStream() ) {
                if ( keyPrefix != null ) {
                    Properties properties = new Properties();
                    properties.load( stream );
                    Properties projectProperties = project.getProperties();
                    for ( String key : properties.stringPropertyNames() ) {
                        projectProperties.put( keyPrefix + key, properties.get( key ) );
                    }
                } else {
                    project.getProperties().load( stream );
                }
            }
        } catch ( IOException e ) {
            throw new MojoExecutionException( "Error reading properties from " + resource, e );
        }
    }

    private void missing( Resource resource )
        throws MojoExecutionException
    {
        if ( quiet )
        {
            getLog().info( "Quiet processing - ignoring properties cannot be loaded from " + resource );
        }
        else
        {
            throw new MojoExecutionException( "Properties could not be loaded from " + resource );
        }
    }

    private void resolveProperties() throws MojoExecutionException, MojoFailureException {
        Properties environment = loadSystemEnvironmentPropertiesWhenDefined();
        Properties projectProperties = project.getProperties();

        for (Object key : projectProperties.keySet()) {
            projectProperties.setProperty( (String) key, getPropertyValue( (String) key, projectProperties, environment ) );
        }
    }

    private Properties loadSystemEnvironmentPropertiesWhenDefined() throws MojoExecutionException {

        boolean useEnvVariables = project.getProperties()
          .values()
          .stream()
          .anyMatch(o -> ((String) o).startsWith("${env."));

        Properties environment = null;
        if ( useEnvVariables ) {
            try {
                environment = getSystemEnvVars();
            } catch ( IOException e ) {
                throw new MojoExecutionException( "Error getting system environment variables: ", e );
            }
        }
        return environment;
    }

    private String getPropertyValue( String propertyName, Properties mavenPropertiesFromResource, Properties processEnvironment) throws MojoFailureException {
        try {
            return resolver.getPropertyValue(propertyName, mavenPropertiesFromResource, processEnvironment);
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    /**
     * Override-able for test purposes.
     * 
     * @return The shell environment variables, can be empty but never <code>null</code>.
     * @throws IOException If the environment variables could not be queried from the shell.
     */
    Properties getSystemEnvVars() throws IOException {
        return CommandLineUtils.getSystemEnvVars();
    }

    /**
     * Default scope for test access.
     * 
     * @param quiet Set to <code>true</code> if missing files can be skipped.
     */
    void setQuiet( boolean quiet )
    {
        this.quiet = quiet;
    }

    /**
     *
     * @param skipLoadProperties Set to <code>true</code> if you don't want to load properties.
     */
    void setSkipLoadProperties( boolean skipLoadProperties )
    {
        this.skipLoadProperties = skipLoadProperties;
    }

    /**
     * Default scope for test access.
     * 
     * @param project The test project.
     */
    void setProject( MavenProject project )
    {
        this.project = project;
    }
}
