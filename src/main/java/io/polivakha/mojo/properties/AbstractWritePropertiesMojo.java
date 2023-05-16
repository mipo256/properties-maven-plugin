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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @author Sergey Korotaev
 */
public abstract class AbstractWritePropertiesMojo
        extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(required = true, property = "properties.outputFile")
    private File outputFile;

    @Parameter(property = "sort", defaultValue = "true")
    private boolean sort;

    @Parameter(property = "comment", defaultValue = "Properties")
    private String comment;

    /**
     * @param properties {@link Properties}
     * @param file       {@link File}
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void writeProperties(Properties properties, File file) throws MojoExecutionException {
        try {
            storeWithoutTimestamp(properties, file, getComment());
        } catch (FileNotFoundException e) {
            getLog().error("Could not create FileOutputStream: " + file);
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error("Error writing properties: " + file);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    // https://github.com/apache/maven-archiver/blob/master/src/main/java/org/apache/maven/archiver/PomPropertiesUtil.java#L81
    private void storeWithoutTimestamp(Properties properties, File outputFile, String comment) throws IOException {
        try (PrintWriter pw = new PrintWriter(outputFile, StandardCharsets.ISO_8859_1); StringWriter sw = new StringWriter()) {
            properties.store(sw, comment);

            List<String> lines = new ArrayList<>();
            try (BufferedReader r = new BufferedReader(new StringReader(sw.toString()))) {
                r.lines()
                        .forEach(line -> {
                            if (line.equals('#' + comment)) {
                                pw.println(line);
                            }
                            if (!line.startsWith("#")) {
                                lines.add(line);
                            }
                        });
            }

            lines.stream()
                    .sorted(isSort() ? Comparator.naturalOrder() : Comparator.reverseOrder())
                    .forEach(pw::println);
        }
    }

    /**
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void validateOutputFile() throws MojoExecutionException {
        if (outputFile.isDirectory()) {
            throw new MojoExecutionException("outputFile must be a file and not a directory");
        }
        // ensure path exists
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
    }

    /**
     * @return {@link MavenProject}
     */
    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    /**
     * @return {@link #outputFile}
     */
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
