plugins {
    application
    java
    jacoco
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.withType<Test>().configureEach {
    val agentFile = configurations.testRuntimeClasspath.get().find { it.name.contains("byte-buddy-agent") }
    if (agentFile != null) {
        jvmArgs("-javaagent:${agentFile.absolutePath}")
    }
}

tasks.withType<Test>().configureEach {
    val agentFile = configurations.testRuntimeClasspath.get().find { it.name.contains("byte-buddy-agent") }
    if (agentFile != null) {
        jvmArgs("-javaagent:${agentFile.absolutePath}", "-Xshare:off")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.telegram.longpolling)
    implementation(libs.telegram.client)
    implementation(libs.opencsv)

    testImplementation(platform(libs.junit.bom))
    testRuntimeOnly(libs.junit.launcher)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
    implementation(libs.guava)
    implementation(libs.quartz.scheduler)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)

    implementation(libs.logback.classic)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("Main")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    systemProperty("file.encoding", "UTF-8")
}
