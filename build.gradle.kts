import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    kotlin("jvm") version "1.5.31"
}

group = "ru.destructio"
version = "1.2-Logless"

repositories {
    maven("https://m2.dv8tion.net/releases")
    mavenCentral()
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.1.7")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    //implementation("log4j:log4j:1.2.17")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "15"
}


tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "DestructioBot"))
        }
    }
}