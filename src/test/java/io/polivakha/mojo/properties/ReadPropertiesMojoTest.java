package io.polivakha.mojo.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class ReadPropertiesMojoTest {
    private static final String NEW_LINE = System.lineSeparator();

    private MavenProject projectStub;
    private ReadPropertiesMojo readPropertiesMojo;

    @Before
    public void setUp() {
        projectStub = new MavenProject();
        readPropertiesMojo = new ReadPropertiesMojo();
        readPropertiesMojo.setProject( projectStub );
    }

    @Test
    public void givenTwoPropertiesFiles_whenResolvingExistingProperties_thenMavenProjectContainsThem() throws IOException, MojoExecutionException, MojoFailureException {
        Path tempDirectory = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(tempDirectory.toString(), "temp-file.properties"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.toFile()))) {
            writer.write("some.version=1234\n");
            writer.write("another.version=9098");
        }

        readPropertiesMojo.setIncludes(new String[]{Path.of(tempDirectory.toString(), "*.properties").toString()});
        readPropertiesMojo.execute();

        String firstValue = projectStub.getProperties().getProperty("some.version");
        String secondValue = projectStub.getProperties().getProperty("another.version");
        String nonExistingProperty = projectStub.getProperties().getProperty("non.existing.version");

        Assertions.assertThat(firstValue).isEqualTo("1234");
        Assertions.assertThat(secondValue).isEqualTo("9098");
        Assertions.assertThat(nonExistingProperty).isNull();
    }

    @Test
    public void givenSeveralPropertyFiles_whenReadingFromBoth_thenPropertiesFromAllFilesRead() throws IOException, MojoExecutionException, MojoFailureException {
        Path tempDirectory = Files.createTempDirectory("mytmp");
        Path firstFile = Files.createFile(Paths.get(tempDirectory.toString(), "first.properties"));
        Path secondFile = Files.createFile(Paths.get(tempDirectory.toString(), "second.properties"));

        try (FileWriter writer = new FileWriter(firstFile.toFile())) {
            writer.write("first.version=1.0.1\n");
            writer.write("second.version=1.4.2");
        }

        try (FileWriter writer = new FileWriter(secondFile.toFile())) {
            writer.write("third.version=5.8.8\n");
        }

        readPropertiesMojo.setIncludes(new String[]{Path.of(tempDirectory.toString(), "*.properties").toString()});
        readPropertiesMojo.execute();

        String one = projectStub.getProperties().getProperty("first.version");
        String two = projectStub.getProperties().getProperty("second.version");
        String three = projectStub.getProperties().getProperty("third.version");

        Assertions.assertThat(one).isEqualTo("1.0.1");
        Assertions.assertThat(two).isEqualTo("1.4.2");
        Assertions.assertThat(three).isEqualTo("5.8.8");
    }

    @Test
    public void givenTwoPropertiesFileWithTwoPatterns_whenResolvingExistingProperties_thenLastPatternFilesTakePrecedence() throws IOException, MojoExecutionException, MojoFailureException {
        Path parentFirst = Files.createTempDirectory("parent1");
        Path parenSecond = Files.createTempDirectory("parent2");

        Path firstFile = Files.createFile(Paths.get(parentFirst.toString(), "first.properties"));
        Path secondFile = Files.createFile(Paths.get(parenSecond.toString(), "second.properties"));

        try (FileWriter writer = new FileWriter(firstFile.toFile())) {
            writer.write("first.version=1.0.1\n");
            writer.write("second.version=1.4.2");
        }

        try (FileWriter writer = new FileWriter(secondFile.toFile())) {
            writer.write("third.version=5.8.8\n");
            writer.write("first.version=0.0.1\n");
        }

        readPropertiesMojo.setIncludes(
          new String[]{
            Path.of(parentFirst.toString(), "*.properties").toString(),
            Path.of(parenSecond.toString(), "*.properties").toString()
          }
        );

        readPropertiesMojo.execute();

        String one = projectStub.getProperties().getProperty("first.version");
        String two = projectStub.getProperties().getProperty("second.version");
        String three = projectStub.getProperties().getProperty("third.version");

        Assertions.assertThat(one).isEqualTo("0.0.1");
        Assertions.assertThat(two).isEqualTo("1.4.2");
        Assertions.assertThat(three).isEqualTo("5.8.8");
    }

    @Test
    public void readPropertiesWithoutKeyprefix() throws Exception {
        try ( FileReader fr = new FileReader( getPropertyFileForTesting() ) )
        {
            // load properties directly for comparison later
            Properties testProperties = new Properties();
            testProperties.load( fr );

            // do the work
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting()} );
            readPropertiesMojo.execute();

            // check results
            Properties projectProperties = projectStub.getProperties();
            assertNotNull( projectProperties );
            // it should not be empty
            assertNotEquals( 0, projectProperties.size() );

            // we are not adding prefix, so properties should be same as in file
            assertEquals( testProperties.size(), projectProperties.size() );
            assertEquals( testProperties, projectProperties );
        }
    }

    @Test
    public void readPropertiesWithKeyprefix() throws Exception {
        String keyPrefix = "testkey-prefix.";

        try ( FileReader fs1 = new FileReader( getPropertyFileForTesting( keyPrefix ) );
              FileReader fs2 = new FileReader( getPropertyFileForTesting() ) ) {

            Properties testPropertiesWithoutPrefix = new Properties();
            testPropertiesWithoutPrefix.load( fs2 );

            // do the work
            readPropertiesMojo.setKeyPrefix( keyPrefix );
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting()} );
            readPropertiesMojo.execute();

            // load properties directly and add prefix for comparison later
            Properties testPropertiesPrefix = new Properties();
            testPropertiesPrefix.load( fs1 );

            // check results
            Properties projectProperties = projectStub.getProperties();
            assertNotNull( projectProperties );
            // it should not be empty
            assertNotEquals( 0, projectProperties.size() );

            // we are adding prefix, so prefix properties should be same as in projectProperties
            assertEquals( testPropertiesPrefix.size(), projectProperties.size() );
            assertEquals( testPropertiesPrefix, projectProperties );

            // properties with and without prefix shouldn't be same
            assertNotEquals( testPropertiesPrefix, testPropertiesWithoutPrefix );
            assertNotEquals( testPropertiesWithoutPrefix, projectProperties );
        }
    }

    private File getPropertyFileForTesting() throws IOException {
        return getPropertyFileForTesting( null );
    }

    private File getPropertyFileForTesting( String keyPrefix ) throws IOException {
        File f = File.createTempFile( "prop-test", ".properties" );
        f.deleteOnExit();

        String prefix = keyPrefix == null ? "" : keyPrefix;

        try (FileWriter writer = new FileWriter( f )) {
            writer.write( prefix + "test.property1=value1" + NEW_LINE );
            writer.write( prefix + "test.property2=value2" + NEW_LINE );
            writer.write( prefix + "test.property3=value3" + NEW_LINE );
            writer.flush();
        }

        return f;
    }
}
