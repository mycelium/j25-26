plugins {
    application
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-M2")
    implementation("org.nd4j:nd4j-native-platform:1.0.0-M2")
    implementation("org.datavec:datavec-api:1.0.0-M2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
}


tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("org.example.App")
}
