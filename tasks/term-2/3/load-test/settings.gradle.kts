// settings.gradle.kts
rootProject.name = "load-test"

// Если вы используете мульти-модульную структуру и хотите подключить lab-1 и lab-2:
// include(":jsonlab")
// project(":jsonlab").projectDir = file("../../1")
//
// include(":httpserverlib")
// project(":httpserverlib").projectDir = file("../../2")

// Опционально: настройки репозиториев на уровне всего build
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}