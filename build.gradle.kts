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
    `java-library`
    checkstyle
    jacoco
    `jacoco-report-aggregation`
}



allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply(plugin = "java-library")
    apply(plugin = "checkstyle")

    tasks.test {
        useJUnitPlatform()
        systemProperty("cvf.launcher", "cvf.sample.tf.gx.system.GxSystemLauncher")
    }

    dependencies {
        implementation("org.junit.jupiter:junit-jupiter:5.8.0")
        implementation("org.junit.platform:junit-platform-suite-engine:1.8.1")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
        implementation("com.squareup.okhttp3:okhttp:4.10.0")
        implementation("org.mockito:mockito-core:5.4.0")
        implementation("org.awaitility:awaitility:4.2.0")

        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jakarta-jsonp:2.14.1")
        implementation("com.apicatalog:titanium-json-ld:1.3.1")
        implementation("org.glassfish:jakarta.json:2.0.0")

    }

}

// needed for running the dash tool
tasks.register("allDependencies", DependencyReportTask::class)

// disallow any errors
checkstyle {
    maxErrors = 0
}