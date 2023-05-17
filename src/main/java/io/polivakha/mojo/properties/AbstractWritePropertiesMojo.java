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

import io.polivakha.mojo.properties.models.StoreProperties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    @Parameter(property = "sort")
    private boolean sort;

    /**
     * @param properties {@link Properties}
     * @param file       {@link File}
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void writeProperties(Properties properties, File file) throws MojoExecutionException {
        try {
            storeWithoutTimestamp(new StoreProperties(properties), file);
        } catch (FileNotFoundException e) {
            getLog().error("Could not create FileOutputStream: " + file);
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error("Error writing properties: " + file);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void storeWithoutTimestamp(StoreProperties properties, File outputFile) throws IOException {

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            if (isSort()) {
                properties.sortedStore(fileOutputStream, null);
            } else {
                properties.store(fileOutputStream, null);
            }
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
}
