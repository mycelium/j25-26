plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7:models")

    implementation("org.slf4j:slf4j-simple:2.0.17")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

tasks.test {
    useJUnitPlatform()
}
