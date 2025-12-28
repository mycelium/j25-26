plugins {
    id("java")
    application
}

application {
    mainClass.set("org.example.App")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.deeplearning4j:deeplearning4j-core:1.0.0-beta7")
    implementation("org.nd4j:nd4j-native-platform:1.0.0-beta7")
    implementation("org.slf4j:slf4j-simple:1.7.36")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.util=ALL-UNNAMED"
    )
}