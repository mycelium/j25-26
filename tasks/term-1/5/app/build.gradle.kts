plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)

    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4:models")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4:models-english")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.12.1")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    mainClass = "org.example.App"
}
