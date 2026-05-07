plugins {
    java
    application
}

group = "com.labs"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // JSON парсер для сравнения
    implementation("com.google.code.gson:gson:2.10.1")
    // Файловая БД для Request 1
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

application {
    mainClass.set("com.labs.Main")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}