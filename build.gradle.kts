plugins {
    java
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
}

// Ressourcen (plugin.yml, config.yml) werden standardmäßig eingebunden
// tasks.processResources { from("src/main/resources") }

tasks.jar {
    archiveBaseName.set("TeamChat")
}
