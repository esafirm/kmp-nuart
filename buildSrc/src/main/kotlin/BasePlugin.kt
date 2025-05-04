package nolambda.stream.nuart.gradle.buildSrc

import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

public open class BasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.createReleaseTask()

        target.addAppPlatformGradlePlugin()
        target.runTestsInHeadlessMode()
        target.configureLogOutput()
    }

    private fun Project.createReleaseTask() {
        tasks.register("release")
    }

    private fun Project.runTestsInHeadlessMode() {
        // Otherwise the java icon keeps popping up in the system tray while running tests.
        tasks.withType(Test::class.java).configureEach {
            it.systemProperty("java.awt.headless", "true")
        }
    }

    private fun Project.configureLogOutput() {
        if (ci) {
            tasks.withType(Test::class.java).configureEach { testTask ->
                testTask.testLogging {
                    it.showExceptions = true
                    it.showCauses = true
                    it.showStackTraces = true
                    it.showStandardStreams = true
                }
            }
        }
    }

    private fun Project.addAppPlatformGradlePlugin() {
        if (!isRoot) {
            plugins.apply(Plugins.APP_PLATFORM)
            plugins.withIds(Plugins.KOTLIN_MULTIPLATFORM, Plugins.KOTLIN_JVM) {
                releaseTask.dependsOn("checkModuleStructureDependencies")
            }
        }
    }
}
