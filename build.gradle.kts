plugins {
    `java-library`
}



allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply(plugin = "java-library")

    tasks.test {
        useJUnitPlatform()
        systemProperty("cvf.launcher", "cvf.sample.tf.gx.launcher.GxSystemLauncher")
    }

    dependencies {
        implementation("org.junit.jupiter:junit-jupiter:5.8.0")
        implementation("org.junit.platform:junit-platform-suite-engine:1.8.1")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
        implementation("com.squareup.okhttp3:okhttp:4.10.0")
        implementation("org.mockito:mockito-core:4.8.1")
        implementation("org.awaitility:awaitility:4.2.0")

        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jakarta-jsonp:2.14.1")
        implementation("com.apicatalog:titanium-json-ld:1.3.1")
        implementation("org.glassfish:jakarta.json:2.0.0")

    }

}

// needed for running the dash tool
tasks.register("allDependencies", DependencyReportTask::class)