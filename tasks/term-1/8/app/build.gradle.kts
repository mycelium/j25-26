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
    // DL4J
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-M2.1")
    // ND4J backend (CPU)
    implementation("org.nd4j:nd4j-native-platform:1.0.0-M2.1")
    // Datavec для работы с датасетами (на всякий случай)
    implementation("org.datavec:datavec-api:1.0.0-M2.1")

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
