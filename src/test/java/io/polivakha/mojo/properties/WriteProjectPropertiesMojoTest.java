package io.polivakha.mojo.properties;

import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class WriteProjectPropertiesMojoTest {

    private MavenProject mavenProject;
    private WriteProjectProperties writeProjectProperties;
    private WriteActiveProfileProperties writeActiveProfileProperties;

    @Before
    public void setUp() {
        mavenProject = new MavenProject();
        writeProjectProperties = new WriteProjectProperties();
        writeProjectProperties.setProject(mavenProject);
        writeActiveProfileProperties = new WriteActiveProfileProperties();
        writeActiveProfileProperties.setProject(mavenProject);
    }


    @Test
    public void givenSystemProperties_whenWritePropertiesWithSortASC_thenPropertiesInFileASC() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeProjectProperties.setOutputFile(file.toFile());
        writeProjectProperties.setComment("Properties");
        writeProjectProperties.setSort(true);
        mavenProject.getProperties().setProperty("some.version", "1234");
        mavenProject.getProperties().setProperty("another.version", "9098");

        writeProjectProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertEquals("#Properties", bufferedReader.readLine());
            assertEquals("another.version=9098", bufferedReader.readLine());
            assertEquals("some.version=1234", bufferedReader.readLine());
        }

    }

    @Test
    public void givenSystemProperties_whenWritePropertiesWithSortDESC_thenPropertiesInFileDESC() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeProjectProperties.setOutputFile(file.toFile());
        writeProjectProperties.setComment("Properties");
        writeProjectProperties.setSort(false);
        mavenProject.getProperties().setProperty("some.version", "1234");
        mavenProject.getProperties().setProperty("another.version", "9098");

        writeProjectProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertEquals("#Properties", bufferedReader.readLine());
            assertEquals("some.version=1234", bufferedReader.readLine());
            assertEquals("another.version=9098", bufferedReader.readLine());
        }
    }

    @Test
    public void givenSystemPropertiesBySomeProfile_whenWritePropertiesWithSortASC_thenPropertiesInFileASC() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeActiveProfileProperties.setOutputFile(file.toFile());
        writeActiveProfileProperties.setComment("Properties");
        writeActiveProfileProperties.setSort(true);
        Profile profile = new Profile();
        profile.setId("someProfile");
        Properties properties = new Properties();
        properties.setProperty("some.version", "1234");
        properties.setProperty("another.version", "9098");
        profile.setProperties(properties);
        mavenProject.setActiveProfiles(List.of(profile));

        writeActiveProfileProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertEquals("#Properties", bufferedReader.readLine());
            assertEquals("another.version=9098", bufferedReader.readLine());
            assertEquals("some.version=1234", bufferedReader.readLine());
        }
    }

    @Test
    public void givenSystemPropertiesBySomeProfile_whenWritePropertiesWithSortDESC_thenPropertiesInFileDESC() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeActiveProfileProperties.setOutputFile(file.toFile());
        writeActiveProfileProperties.setComment("Properties");
        writeActiveProfileProperties.setSort(false);
        Profile profile = new Profile();
        profile.setId("someProfile");
        Properties properties = new Properties();
        properties.setProperty("some.version", "1234");
        properties.setProperty("another.version", "9098");
        profile.setProperties(properties);
        mavenProject.setActiveProfiles(List.of(profile));

        writeActiveProfileProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertEquals("#Properties", bufferedReader.readLine());
            assertEquals("some.version=1234", bufferedReader.readLine());
            assertEquals("another.version=9098", bufferedReader.readLine());
        }
    }
    @Test
    public void customCommentTest() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeProjectProperties.setOutputFile(file.toFile());
        writeProjectProperties.setComment("SomeParam");

        writeProjectProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertEquals("#SomeParam", bufferedReader.readLine());
        }
    }
}
