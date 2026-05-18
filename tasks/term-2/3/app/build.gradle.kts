plugins {
    application
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java {
            srcDirs(
                "src/main/java",
                "${rootDir}/../1/lib/src/main/java",
                "${rootDir}/../2/lib/src/main/java"
            )
        }
    }
}

dependencies {
    implementation(libs.gson)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass = "org.example.loadtest.Main"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.example.loadtest.Main"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
