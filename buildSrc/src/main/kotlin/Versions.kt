/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_github_johnrengelman_shadow_gradle_plugin: String = "5.2.0" 

    const val com_github_spotbugs_gradle_plugin: String = "2.0.0" // available: "3.0.0"

    const val simplelatlng: String = "1.3.1" 

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" // available: "0.7.0"

    const val kotlintest_runner_junit5: String = "3.3.2" // available: "3.4.2"

    const val mockk: String = "1.9.1" // available: "1.9.3"

    const val commons_lang3: String = "3.7" // available: "3.9"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.61" 

    const val org_jetbrains_kotlin: String = "1.3.61" 

    const val protelis: String = "13.0.3" // available: "13.1.0"

    const val slf4j_simple: String = "1.8.0-beta4" 

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "5.4.1"

        const val currentVersion: String = "6.0.1"

        const val nightlyVersion: String = "6.2-20200101230025+0000"

        const val releaseCandidate: String = "6.1-rc-1"
    }
}
