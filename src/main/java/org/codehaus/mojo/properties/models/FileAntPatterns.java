package org.codehaus.mojo.properties.models;

import java.util.List;

/**
 * This class represents the Apache Ant directory patterns
 *
 * @apiNote <a href="https://gist.github.com/luanvuhlu/894a2194f8e67761cd1a4d11961cbf37">Ant Path matching strategy</a>
 * @author Mikhail Polivakha
 */
public class FileAntPatterns {

    private List<FilePattern> filePatterns;

    public List<FilePattern> getFilePatterns() {
        return filePatterns;
    }

    public void setFilePatterns(List<FilePattern> filePatterns) {
        this.filePatterns = filePatterns;
    }

    static class FilePattern {

        private String pattern;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
}