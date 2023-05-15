package io.polivakha.mojo.properties.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

/**
 * This class parses the file system tree to find files matching specific ant pattern
 *
 * @author Mikhail Polivakha
 */
public class PathParser {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher(File.pathSeparator);

    private final Pattern characterClassesRegexp = Pattern.compile(".*:\\[.*]");

    /**
     * Searches for all files available that match provided {@code antPathPattern}
     *
     * @param antPathPattern - ant path pattern, must not be null
     * @return Stream of {@link Path}'s, that are files (not directories), that match provided ant path.
     *         It is the responsibility of the caller method to close the stream
     *
     * @throws IOException in case of any file system errors
     */
    public Stream<Path> streamFilesMatchingAntPath(String antPathPattern) throws IOException {

        Assert.notNull(antPathPattern, "Provided ant path is null");

        String s = extractExactDirectory(Path.of(antPathPattern));

        return Files
          .walk(Path.of(s), FileVisitOption.FOLLOW_LINKS)
          .filter(currentFile -> {

              if (currentFile.toFile().isDirectory()) {
                  return false;
              }

              return antPathMatcher.match(antPathPattern, currentFile.toString());
          });
    }

    public String extractExactDirectory(Path path) {
     
        Assert.notNull(path, "Passed path must not be null");

        if (path.toString().isEmpty()) {
            return "/";
        }

        String stringPath = path.normalize().toString();

        return extractMostExactDirectoryFromNormalizedPath(stringPath);
    }

    private String extractMostExactDirectoryFromNormalizedPath(String stringPath) {
        StringBuilder result = new StringBuilder("/");

        int leftPointer = 1;

        int nextForwardSlashIndex = 1;

        while ((nextForwardSlashIndex = stringPath.indexOf("/", nextForwardSlashIndex)) != -1) {
            String pathComponent = stringPath.substring(leftPointer, nextForwardSlashIndex);
            if (containsAntTokens(pathComponent)) {
                return removeTrailingSlashIfPresent(result);
            }
            result.append(pathComponent).append("/");
            nextForwardSlashIndex++;
            leftPointer = nextForwardSlashIndex;
        }

        String tail = stringPath.substring(leftPointer);

        if (!containsAntTokens(tail)) {
            result.append(tail);
        }

        return removeTrailingSlashIfPresent(result);
    }

    private String removeTrailingSlashIfPresent(StringBuilder result) {
        if (result.toString().endsWith("/") && result.length() > 1) {
            return result.substring(0, result.length() - 1);
        }
        return result.toString();
    }

    public boolean containsAntTokens(String pathComponent) {
        if (pathComponent == null || pathComponent.isEmpty()) {
            return false;
        }

        if (pathComponent.contains("**") || pathComponent.contains("*") || pathComponent.contains("?")) {
            return true;
        }

        return characterClassesRegexp.matcher(pathComponent).find();
    }
}
