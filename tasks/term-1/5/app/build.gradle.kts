plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Stanford CoreNLP для анализа тональности
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7:models")
    
    // Apache Commons CSV для чтения CSV файлов
    implementation("org.apache.commons:commons-csv:1.10.0")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.12.1")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.lab5.App"
}
