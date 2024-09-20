plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "me.x150"
version = "1.4.8"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        instrumentationTools()
        intellijIdeaCommunity("2024.2")
    }
}

tasks {
    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set(provider {null})
    }

    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
