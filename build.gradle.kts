plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "cx.leo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
}

tasks {
    shadowJar {
        relocate("net.kyori", "cx.leo.velocity.staffchat.adventure")
    }
}