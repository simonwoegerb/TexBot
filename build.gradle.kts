plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("application")

}

group = "eu.simonw.texbot"
version = "1.0-SNAPSHOT"
val jdaversion = "5.6.1"

repositories {
    mavenCentral()
}
application {
    mainClass.set("eu.simonw.texbot.TexBot") // Replace with your fully qualified main class name
}

tasks.shadowJar {
    archiveBaseName.set("bot")
    archiveClassifier.set("") // no -all suffix
    archiveVersion.set("")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}


tasks.build {
    dependsOn(tasks.shadowJar)
}


testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter("5.13.4")
        }
    }
}


dependencies {
    // SLF4J API
    implementation("org.slf4j:slf4j-api:2.0.13")

    // SLF4J binding for Log4j 2
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    // Core Log4j dependencies
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-api:2.23.1")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("net.dv8tion:JDA:$jdaversion") { // replace $version with the latest version

        exclude(module="opus-java") // required for encoding audio into opus, not needed if audio is already provided in opus encoding
        exclude(module="tink") // required for encrypting and decrypting audio
    }
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}