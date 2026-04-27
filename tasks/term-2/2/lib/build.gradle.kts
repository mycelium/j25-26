plugins {
    `java-library`
    `maven-publish`
}

group = "com.httpserverlib"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.toVersion("24")
    targetCompatibility = JavaVersion.toVersion("24")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    doFirst {
        val javaVersion = System.getProperty("java.version")
        val majorVersion = javaVersion.substringBefore('.').toIntOrNull() ?: 0
        if (majorVersion < 24) {
            throw org.gradle.api.GradleException("Java 24 or higher is required. Current version: $javaVersion")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}