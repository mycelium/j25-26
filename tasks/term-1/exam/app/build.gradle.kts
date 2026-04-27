plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.guava)
    implementation("org.slf4j:slf4j-simple:2.0.9")
  
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4:models")
    implementation("edu.stanford.nlp:stanford-corenlp:4.5.4:models-english")
    
   
    implementation("com.opencsv:opencsv:5.8")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
   
    mainClass = "org.example.Server"
}


tasks.named<JavaExec>("run") {
    maxHeapSize = "2g" 
    standardInput = System.`in`
}


tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Runs the multi-threaded sentiment analysis client"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.example.MultiThreadedClient") 
    maxHeapSize = "1g"
    standardInput = System.`in`
}
