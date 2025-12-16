plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.0")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.0:models")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "sentiment.analyzer.App"
}