plugins {
    java
    application
}

group = "ru.j25"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("com.google.code.gson:gson:2.11.0")
}

sourceSets {
    main {
        java {
            srcDir("../1")
            srcDir("../2")
            exclude("**/Main.java")
        }
    }
}

application {
    mainClass.set("loadtest.Server")
}

tasks.named<JavaExec>("run") {
    args = listOf(
        "--port=${project.findProperty("port") ?: "8080"}",
        "--threads=${project.findProperty("threads") ?: "10"}",
        "--virtual=${project.findProperty("virtual") ?: "false"}",
        "--gson=${project.findProperty("gson") ?: "false"}"
    )
    standardInput = System.`in`
}

tasks.register<JavaExec>("runLoadTest") {
    group = "application"
    description = "Run HTTP load test"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("loadtest.LoadTestRunner")
    args = listOf(
        "--host=${project.findProperty("host") ?: "localhost"}",
        "--port=${project.findProperty("port") ?: "8080"}",
        "--threads=${project.findProperty("threads") ?: "50"}",
        "--requests=${project.findProperty("requests") ?: "1000"}"
    )
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "loadtest.Server"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.EC")
}
