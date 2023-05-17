package io.polivakha.mojo.properties;

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

public class WriteProjectPropertiesMojoTest {

    private MavenProject mavenProject;
    private WriteProjectProperties writeProjectProperties;

    @Before
    public void setUp() {
        mavenProject = new MavenProject();
        writeProjectProperties = new WriteProjectProperties();
        writeProjectProperties.setProject(mavenProject);
    }


    @Test
    public void givenSystemProperties_whenWritePropertiesWithSortASC_thenPropertiesInFileASC() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeProjectProperties.setOutputFile(file.toFile());
        writeProjectProperties.setSort(true);
        mavenProject.getProperties().setProperty("some.version", "1234");
        mavenProject.getProperties().setProperty("another.version", "9098");
        mavenProject.getProperties().setProperty("other.version", "8080");

        writeProjectProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            assertTrue(bufferedReader.readLine().contains("#"));
            assertEquals("another.version=9098", bufferedReader.readLine());
            assertEquals("other.version=8080", bufferedReader.readLine());
            assertEquals("some.version=1234", bufferedReader.readLine());
        }

    }

    @Test
    public void givenSystemProperties_whenWritePropertiesWithoutSort_thenPropertiesInFileInOrderOfAddition() throws Exception {
        Path temp = Files.createTempDirectory("mytmp");
        Path file = Files.createFile(Paths.get(temp.toString(), "properties-from-pom.properties"));
        writeProjectProperties.setOutputFile(file.toFile());
        mavenProject.getProperties().setProperty("some.version", "1234");
        mavenProject.getProperties().setProperty("another.version", "9098");
        mavenProject.getProperties().setProperty("other.version", "8080");

        writeProjectProperties.execute();


        try (BufferedReader bufferedReader = Files.newBufferedReader(file)) {
            Assertions.assertThat(bufferedReader.lines()).containsAll(getPropertiesList(mavenProject.getProperties()));
        }
    }

    private List<String> getPropertiesList(Properties properties) {
        return properties.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.toList());
    }
}
