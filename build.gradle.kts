import org.eclipse.dataspacetck.gradle.tckbuild.extensions.TckBuildExtension

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


plugins {
    `java-library`
    `maven-publish`
    signing
    checkstyle
    jacoco
    `jacoco-report-aggregation`
    alias(libs.plugins.docker)
    alias(libs.plugins.nexuspublishing)
    alias(libs.plugins.tck.build)
}

allprojects {

    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.eclipse.dataspacetck.tck-build")

    configure<TckBuildExtension> {
        pom {
            scmConnection = "https://github.com/eclipse-dataspacetck/dsp-tck.git"
            scmUrl = "scm:git:git@github.com:eclipse-dataspacetck/dsp-tck.git"
            projectName = project.name
            description = "DSP Technology Compatibility Kit"
            projectUrl = "https://projects.eclipse.org/projects/technology.dataspacetck"

            developers {
                create("Another", "Another Developer", "another@dev.com")
            }
        }
    }

    tasks.test {
        useJUnitPlatform()
        systemProperty("dataspacetck.launcher", "org.eclipse.dataspacetck.dsp.system.DspSystemLauncher")
    }

    tasks.jar {
        metaInf {
            from("${rootProject.projectDir.path}/LICENSE")
            from("${rootProject.projectDir.path}/DEPENDENCIES")
            from("${rootProject.projectDir.path}/NOTICE.md")
        }
    }

    dependencies {
        implementation(rootProject.libs.json.api)
        implementation(rootProject.libs.jackson.databind)
        implementation(rootProject.libs.jackson.jsonp)
        implementation(rootProject.libs.titanium)
        implementation(rootProject.libs.okhttp)
        implementation(rootProject.libs.junit.jupiter)
        implementation(rootProject.libs.junit.platform.engine)
        implementation(rootProject.libs.mockito.core)
        implementation(rootProject.libs.awaitility)
        testImplementation(rootProject.libs.assertj)
    }

}

subprojects {

    afterEvaluate {
//        publishing {
//            publications.forEach { i ->
//                val mp = (i as MavenPublication)
//                mp.pom {
//                    name.set(project.name)
//                    description.set("Compliance Verification Toolkit")
//                    url.set("https://projects.eclipse.org/projects/technology.dataspacetck")
//
//                    licenses {
//                        license {
//                            name.set("The Apache License, Version 2.0")
//                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                        }
//                        developers {
//                            developer {
//                                id.set("JimMarino")
//                                name.set("Jim Marino")
//                                email.set("jmarino@metaformsystems.com")
//                            }
//                            developer {
//                                id.set("PaulLatzelsperger")
//                                name.set("Paul Latzelsperger")
//                                email.set("paul.latzelsperger@beardyinc.com")
//                            }
//                            developer {
//                                id.set("EnricoRisa")
//                                name.set("Enrico Risa")
//                                email.set("enrico.risa@gmail.com")
//                            }
//                        }
//                        scm {
//                            connection.set("scm:git:git@github.com:eclipse-dataspacetck/cvf.git")
//                            url.set("https://github.com/eclipse-dataspacetck/cvf.git")
//                        }
//                    }
//                }
//            }
//        }

    }
}


// needed for running the dash tool
tasks.register("allDependencies", DependencyReportTask::class)

// disallow any errors
checkstyle {
    maxErrors = 0
}
