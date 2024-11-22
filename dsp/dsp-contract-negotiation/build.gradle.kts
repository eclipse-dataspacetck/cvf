/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

dependencies {
    annotationProcessor(project(":tools"))

    implementation(project(":api:core-api"))
    implementation(project(":core"))
    implementation(project(":dsp:dsp-api"))
    testImplementation(project(":dsp:dsp-system"))
}

/**
 * This task runs the annotation processor. By default, the TestPlanGenerator is not executed during normal compilation
 */
tasks.register<JavaCompile>("genTestPlan") {
    // Specify the source files to process
    source = fileTree("src/main/java")

    // Set the classpath for the task (include runtime and compile dependencies)
    classpath = sourceSets["main"].runtimeClasspath

    // Set the output directory for generated files
    destinationDirectory.set(file("build/generated/sources/annotationProcessor"))

    // Set the annotation processor classpath
    options.annotationProcessorPath = configurations.getByName("annotationProcessor")

    // Specify compiler arguments
    options.compilerArgs.addAll(listOf(
        "-processor", "org.eclipse.dataspacetck.annotation.processors.TestPlanGenerator",
        "-Acvf.outputDir=${rootProject.layout.buildDirectory.asFile.get().path}", //set output path where testplan.md is stored
        "-Acvf.conversion.force=true", // force pre-rendering of mermaid/plantuml diagrams as images (as opposed to: direct embed)
        "-Acvf.conversion.format=png", // image format for pre-rendering
        "-Acvf.generate=true", // image format for pre-rendering
    ))
}


tasks.test {
    systemProperty("dataspacetck.launcher", "org.eclipse.dataspacetck.dsp.system.DspSystemLauncher")
    systemProperty("dataspacetck.dsp.local.connector", "true")
}
