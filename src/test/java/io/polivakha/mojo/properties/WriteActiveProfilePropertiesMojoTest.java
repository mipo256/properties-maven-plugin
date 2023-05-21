package io.polivakha.mojo.properties;

import org.apache.maven.model.Profile;
import org.apache.maven.project.MavenProject;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WriteActiveProfilePropertiesMojoTest {

    private MavenProject mavenProject;
    private WriteActiveProfileProperties writeActiveProfileProperties;

    @Before
    public void setUp() {
        mavenProject = new MavenProject();
        writeActiveProfileProperties = new WriteActiveProfileProperties();
        writeActiveProfileProperties.setProject(mavenProject);
    }

    @Test
    public void givenSystemPropertiesBySomeProfile_whenWritePropertiesWithSortASC_thenPropertiesInFileASC() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeActiveProfileProperties.setOutputFile(file.toFile());
        writeActiveProfileProperties.setSort(true);
        Profile profile = new Profile();
        profile.setId("someProfile");
        Properties properties = new Properties();
        properties.setProperty("some.version", "1234");
        properties.setProperty("another.version", "9098");
        properties.setProperty("other.version", "8080");
        profile.setProperties(properties);
        mavenProject.setActiveProfiles(List.of(profile));

        writeActiveProfileProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertTrue(bufferedReader.readLine().contains("#"));
            assertEquals("another.version=9098", bufferedReader.readLine());
            assertEquals("other.version=8080", bufferedReader.readLine());
            assertEquals("some.version=1234", bufferedReader.readLine());
        }
    }

    @Test
    public void givenSystemPropertiesBySomeProfile_whenWritePropertiesWithoutSort_thenPropertiesInFileInOrderOfAddition() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeActiveProfileProperties.setOutputFile(file.toFile());
        writeActiveProfileProperties.setSort(false);
        Profile profile = new Profile();
        profile.setId("someProfile");
        Properties properties = new Properties();
        properties.setProperty("some.version", "1234");
        properties.setProperty("another.version", "9098");
        properties.setProperty("other.version", "8080");
        profile.setProperties(properties);
        mavenProject.setActiveProfiles(List.of(profile));

        writeActiveProfileProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            Assertions.assertThat(bufferedReader.lines()).containsAll(getPropertiesList(properties));
        }
    }

    private List<String> getPropertiesList(Properties properties) {
        return properties.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());
    }
}
