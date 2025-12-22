plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.5")          // CoreNLP [web:10][web:13]
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.5:models")   // модели
    // при желании: логгер
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

application {
    mainClass.set("org.example.SentimentApp")
}
