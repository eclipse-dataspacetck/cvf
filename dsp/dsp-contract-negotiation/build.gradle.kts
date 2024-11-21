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

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Acvf.outputDir=${rootProject.layout.buildDirectory.asFile.get().path}") //set output path where testplan.md is stored
    options.compilerArgs.add("-Acvf.conversion.force=true") // force pre-rendering of mermaid/plantuml diagrams as images (as opposed to: direct embed)
    options.compilerArgs.add("-Acvf.conversion.format=png") // image format for pre-rendering
}

tasks.test {
    systemProperty("dataspacetck.launcher", "org.eclipse.dataspacetck.dsp.system.DspSystemLauncher")
    systemProperty("dataspacetck.dsp.local.connector", "true")
}
