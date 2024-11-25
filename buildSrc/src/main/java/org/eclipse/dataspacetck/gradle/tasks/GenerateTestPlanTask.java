/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.gradle.tasks;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.work.InputChanges;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class GenerateTestPlanTask extends JavaCompile {
    private final List<String> ALLOWED_FORMATS = List.of("png", "svg");
    private String imageFormat = "svg";
    private boolean forceConversion = true;
    private String outputDirectory;

    public GenerateTestPlanTask() {
        outputDirectory = getProject().getRootProject().getLayout().getBuildDirectory().getAsFile().get().getAbsolutePath();
        source(getProject().fileTree("src/main/java"));

        setClasspath(getProject().getExtensions().getByType(SourceSetContainer.class).getByName("main").getRuntimeClasspath());

        getDestinationDirectory().set(getProject().getLayout().getBuildDirectory().dir("generated/source/annotationProcessor"));

        getOptions().setAnnotationProcessorPath(getProject().getConfigurations().getByName("annotationProcessor"));
    }

    @Override
    public @NotNull String getDescription() {
        return "Generates a test plan by processing test annotations. Use the properties of this task to control image rendering.";
    }

    @Input
    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        imageFormat = imageFormat.toLowerCase().trim();
        if (ALLOWED_FORMATS.contains(imageFormat))
            this.imageFormat = imageFormat;
        else {
            throw new IllegalArgumentException(imageFormat + " is not a valid image format, please use one of " + ALLOWED_FORMATS);
        }
    }

    @Input
    public boolean getForceConversion() {
        return forceConversion;
    }

    public void setForceConversion(boolean forceConversion) {
        this.forceConversion = forceConversion;
    }

    @Input
    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @OutputDirectory
    public File getOutputDir() {
        return new File(outputDirectory);
    }

    @Override
    protected void compile(InputChanges inputs) {
        if (forceConversion) {
            getLogger().info("Will convert all diagrams to {} images and embed those.", imageFormat);
        } else {
            getLogger().info("Will directly embed diagram source code in the test plan document.");
        }
        // add compiler arguments lazily, when all properties are sure to have been assigned.
        getOptions().getCompilerArgs().addAll(
                List.of("-processor", "org.eclipse.dataspacetck.annotation.processors.TestPlanGenerator",
                        "-Acvf.outputDir=" + outputDirectory, //set output path where testplan.md is stored
                        "-Acvf.conversion.force=" + forceConversion, // force pre-rendering of mermaid/plantuml diagrams as images (as opposed to: direct embed)
                        "-Acvf.conversion.format=" + imageFormat, // image format for pre-rendering
                        "-Acvf.generate=true" // enable. always true.
                )
        );
        super.compile(inputs);
    }
}
