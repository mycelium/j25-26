plugins {
    java
    application
}

group = "loadtesting"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

application {
    mainClass = "loadtesting.Main"
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation(fileTree("libs") { include("*.jar") })
}

tasks.jar {
    manifest { attributes["Main-Class"] = "loadtesting.Main" }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}