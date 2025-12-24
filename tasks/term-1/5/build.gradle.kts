plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
}

application {
    mainClass = "org.example.App"
}
 