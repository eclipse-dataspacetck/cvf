dependencies {
    implementation(project(":core"))
    implementation(project(":ids:ids-system"))
}

tasks.test {
    systemProperty("cvf.launcher", "cvf.ids.system.IdsSystemLauncher")
    systemProperty("cvf.ids.local.connector", "true")
}
