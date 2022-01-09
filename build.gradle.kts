plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    kotlin("jvm") version "1.5.31"
}

group = "ru.destructio"
version = "1.3.2"

repositories {
    maven("https://m2.dv8tion.net/releases")
    mavenCentral()
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.2.0")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
}

tasks{
    test{
        useJUnit()
    }
    compileKotlin{
        kotlinOptions.jvmTarget = "16"
    }
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "MainKt"))
        }
    }
}