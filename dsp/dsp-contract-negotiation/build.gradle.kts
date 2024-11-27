import org.eclipse.dataspacetck.gradle.plugins.tckgen.TckGeneratorExtension

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

plugins {
    id("org.eclipse.dataspacetck.tck-generator") version ("0.0.1")
}

dependencies {
    implementation(libs.tck.common.api)
    implementation(project(":core"))
    implementation(project(":dsp:dsp-api"))
    testImplementation(project(":dsp:dsp-system"))
}

configure<TckGeneratorExtension> {
    generatorVersion = project.version.toString()
}

tasks.test {
    systemProperty("dataspacetck.launcher", "org.eclipse.dataspacetck.dsp.system.DspSystemLauncher")
    systemProperty("dataspacetck.dsp.local.connector", "true")
}
