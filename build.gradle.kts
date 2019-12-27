import java.net.URL

plugins {
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "it.unibo"
version = "0.1"

repositories {
    mavenCentral()
}

fun downloadLibFromUrl(libName: String , libUrl: String, libSaveDir: String = "${projectDir.absolutePath}/build/libs") {
    val folder = File(libSaveDir)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    val file = File("$libSaveDir/$libName")
    if (!file.exists()) {
        URL(libUrl).openStream().readAllBytes().also { file.appendBytes(it) }
    }
    dependencies.add("implementation", files(file.absolutePath))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.protelis:protelis:${extra["protelisVersion"].toString()}")
    implementation("com.javadocmd:simplelatlng:${extra["simplelatlng"].toString()}")
    implementation("org.apache.commons:commons-lang3:${extra["commons-lang3"].toString()}")
    downloadLibFromUrl(extra["MqttClientWrapperLib"].toString(), extra["MqttClientWrapperUrl"].toString())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${extra["kotlinTestVersion"].toString()}")
    testImplementation("io.mockk:mockk:1.9.1")
}

tasks.shadowJar.configure {
    // removes "-all" from the jar name
    archiveClassifier.set("")
    exclude ("**/*.kotlin_metadata")
    exclude ("**/*.kotlin_module")
    exclude ("**/*.kotlin_builtins")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform {
        maxParallelForks = 1
    }
    maxParallelForks = 1
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}