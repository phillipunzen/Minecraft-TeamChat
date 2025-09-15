import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.phillipunzen"
version = "1.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.6-R0.1-SNAPSHOT")
    implementation("redis.clients:jedis:4.4.3")
    implementation("com.google.code.gson:gson:2.10.1")
}

// ShadowJar konfigurieren (Kotlin-DSL korrekt)
tasks.withType<ShadowJar>().configureEach {
    archiveBaseName.set("TeamChat")
    mergeServiceFiles()
}

// Deaktiviere das Standard-JAR, damit Shadow das Fat-JAR erzeugt
tasks.named("jar") {
    enabled = false
}
