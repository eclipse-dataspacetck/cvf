
plugins {
    `java-library`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    gradlePluginPortal()
    mavenLocal()
}

dependencies {
    implementation(project(":api:core-api"))
}
