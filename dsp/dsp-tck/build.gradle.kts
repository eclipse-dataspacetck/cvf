/*
 *
 *   Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft
 *
 *   See the NOTICE file(s) distributed with this work for additional
 *   information regarding copyright ownership.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Apache License, Version 2.0 which is available at
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *   License for the specific language governing permissions and limitations
 *   under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 *
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.eclipse.dataspacetck.gradle.plugins.tckbuild.DockerExtension

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":dsp:dsp-api"))
    implementation(project(":dsp:dsp-system"))
    implementation(project(":dsp:dsp-contract-negotiation"))
    implementation(project(":runtimes:tck-runtime"))
    implementation(libs.junit.platform.launcher)
    testImplementation(project(":dsp:dsp-contract-negotiation"))
}


configure<DockerExtension>{
    jarFilePath = "build/libs/${project.name}-runtime.jar"
}
tasks.withType<ShadowJar> {
    exclude("**/pom.properties", "**/pom.xml")
    mergeServiceFiles()
    archiveFileName.set("${project.name}-runtime.jar") // should be something other than "dsp-tck.jar", to avoid erroneous task dependencies

}

application {
    mainClass.set("org.eclipse.dataspacetck.dsp.suite.DspTckSuite")
}