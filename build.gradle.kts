import com.github.spotbugs.SpotBugsTask

plugins {
    kotlin("jvm") version Versions.org_jetbrains_kotlin_jvm_gradle_plugin
    id("com.github.johnrengelman.shadow") version Versions.com_github_johnrengelman_shadow_gradle_plugin
    id("com.github.spotbugs") version Versions.com_github_spotbugs_gradle_plugin
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
}

group = "it.unibo"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Libs.kotlin_stdlib_jdk8)
    implementation(Libs.protelis)
    implementation(Libs.simplelatlng)
    implementation(Libs.commons_lang3)
    implementation(files(Util.downloadLibFromUrl(ExternalLib.mqtt_client_wrapper)))
    testImplementation(Libs.kotlintest_runner_junit5)
    testImplementation(Libs.mockk)
}

spotbugs {
    effort = "max"
    reportLevel = "low"
    isShowProgress = true
    val excludeFile = File("${rootProject.projectDir}/config/spotbugs/excludes.xml")
    if (excludeFile.exists()) {
        excludeFilter = excludeFile
    }
}
tasks.withType<SpotBugsTask> {
    reports {
        xml.isEnabled = false
        html.isEnabled = true
    }
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