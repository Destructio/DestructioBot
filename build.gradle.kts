import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    kotlin("jvm") version "1.5.31"
}

group = "ru.destructio"
version = "1.3.1"

repositories {
    maven("https://m2.dv8tion.net/releases")
    mavenCentral()
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.2.0")
    implementation("com.sedmelluq:lavaplayer:1.3.77")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j-impl
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")

}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "16"
}


tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "DestructioBot"))
        }
    }
}