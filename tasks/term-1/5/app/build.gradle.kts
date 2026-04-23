plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Stanford CoreNLP
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7:models")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.7:models-english")
    
    // Apache Commons CSV
    implementation("org.apache.commons:commons-csv:1.11.0")
    
    // Логирование
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.12.1")
            
            targets.all {
                testTask.configure {
                    
                    maxHeapSize = "4g"
                    
                  
                    systemProperty("java.awt.headless", "true")
                    systemProperty("file.encoding", "UTF-8")
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

application {
    mainClass = "org.example.App"
}

tasks.withType<JavaExec> {
    systemProperty("java.awt.headless", "true")
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "org.example.App"
    }
}
