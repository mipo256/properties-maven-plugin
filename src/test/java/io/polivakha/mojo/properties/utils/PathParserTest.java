package io.polivakha.mojo.properties.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class PathParserTest {

    @Test
    public void given2FilesInTargetDirectory_whenOnlyOneMatches_thenReturnThisOnlyFileInStream() throws IOException {
        Path tempDirectory = Files.createTempDirectory("my_temp");
        Path propertiesFile = Path.of(tempDirectory.toString(), "application.properties");

        Files.createFile(propertiesFile);
        Files.createFile(Path.of(tempDirectory.toString(), "config.yml"));
        Stream<Path> pathStream = new PathParser().streamFilesMatchingAntPath(Path.of(tempDirectory.toString(), "*.properties").toString());
        Assertions.assertThat(pathStream).containsOnly(propertiesFile);
    }

    @Test
    public void given3FilesInTargetDirectory_whenNoOneMatches_thenReturnEmptyStream() throws IOException {
        Path tempDirectory = Files.createTempDirectory("my_temp");
        Files.createFile(Path.of(tempDirectory.toString(), "application.yaml"));
        Files.createFile(Path.of(tempDirectory.toString(), "another.xml"));
        Files.createFile(Path.of(tempDirectory.toString(), "config.yml"));

        Stream<Path> pathStream = new PathParser().streamFilesMatchingAntPath(Path.of(tempDirectory.toString(), "*.properties").toString());
        Assertions.assertThat(pathStream).isEmpty();
    }

    @Test
    public void given4FilesInDifferentDirectories_whenTwoFilesMatchInDifferentDirectories_thenReturnStreamOfThese2Files() throws IOException {
        Path parent = Files.createTempDirectory("parent");
        Path one = Files.createTempDirectory(parent, "temp_one");
        Path two = Files.createTempDirectory(parent, "temp_two");

        Files.createFile(Path.of(one.toString(), "application.yaml"));
        Path first = Files.createFile(Path.of(one.toString(), "another.properties"));
        Files.createFile(Path.of(two.toString(), "config.yml"));
        Path second = Files.createFile(Path.of(two.toString(), "application.properties"));

        Stream<Path> pathStream = new PathParser().streamFilesMatchingAntPath(Path.of(parent.toString(), "**", "*.properties").toString());
        Assertions.assertThat(pathStream).containsOnly(first, second);
    }

    @Test
    public void whenEmptyString_thenJustForwardSlashReturned() {
        String s = new PathParser().extractExactDirectory(Path.of(""));

        Assert.assertEquals("/", s);
    }

    @Test
    public void whenOnlyFileNameIsMasked_thenReturnedPathToOwningDirectory() {
        String s = new PathParser().extractExactDirectory(Path.of("/does/not/really/matter/*.properties"));

        Assert.assertEquals("/does/not/really/matter", s);
    }

    @Test
    public void whenTwoAsterisksSomewhereInTheMiddleProvidedWithFileNameMasked_thenReturnedPathUntilDoubleAsterisks() {
        String s = new PathParser().extractExactDirectory(Path.of("/does/not/**/matter/*.properties"));

        Assert.assertEquals("/does/not", s);
    }

    @Test
    public void whenTwoAsterisksSomewhereInTheMiddleProvided_thenTheSamePathReturned() {
        String s = new PathParser().extractExactDirectory(Path.of("/does/not/**/matter"));

        Assert.assertEquals("/does/not", s);
    }

    @Test
    public void whenExactDirectoryProvided_thenTheSamePathReturned() {
        String s = new PathParser().extractExactDirectory(Path.of("/does/not/really/matter"));

        Assert.assertEquals("/does/not/really/matter", s);
    }

    @Test
    public void whenDoubleAsterisksAtRoot_thenJustForwardSlashReturned() {
        String s = new PathParser().extractExactDirectory(Path.of("/**/does/not/really/matter"));

        Assert.assertEquals("/", s);
    }

    @Test
    public void whenQuotationMarkEncounteredAtRoot_thenJustForwardSlashReturned() {
        String s = new PathParser().extractExactDirectory(Path.of("/th?t/does/not/really/matter"));

        Assert.assertEquals("/", s);
    }

    @Test
    public void whenGivenFirstDirectoryNameWithQuotationMark_thenJustForwardSlashReturned() {
        String s = new PathParser().extractExactDirectory(Path.of("/**"));

        Assert.assertEquals("/", s);
    }

    @Test
    public void whenEmptyString_thenResultFalse() {
        boolean result = new PathParser().containsAntTokens("");
        Assert.assertFalse(result);
    }

    @Test
    public void whenGivenLiteralStringWithoutAntTokens_thenResultFalse() {
        boolean result = new PathParser().containsAntTokens("just_some_string");
        Assert.assertFalse(result);
    }

    @Test
    public void whenGivenStringWithQuotationMark_thenResultTrue() {
        boolean result = new PathParser().containsAntTokens("just_s?me_string");
        Assert.assertTrue(result);
    }

    @Test
    public void whenGivenStringWithAsteriskMark_thenResultTrue() {
        boolean result = new PathParser().containsAntTokens("just_some_*");
        Assert.assertTrue(result);
    }

    @Test
    public void whenGivenStringContainingTwoAsterisk_thenResultTrue() {
        boolean result = new PathParser().containsAntTokens("**");
        Assert.assertTrue(result);
    }

    @Test
    public void whenGivenStringContainingMultiple_thenResultTrue() {
        boolean result = new PathParser().containsAntTokens("som?e.*");
        Assert.assertTrue(result);
    }
}