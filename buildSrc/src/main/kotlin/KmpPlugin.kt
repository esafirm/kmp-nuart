package nolambda.stream.nuart.gradle.buildSrc

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTask
import com.ncorti.ktfmt.gradle.KtfmtExtension
import guru.nidi.graphviz.engine.Format
import io.github.terrakok.KmpHierarchyConfig
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import nolambda.stream.nuart.gradle.buildSrc.Platform.Companion.allPlatforms
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceTask
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public open class KmpPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(Plugins.KOTLIN_MULTIPLATFORM)

        target.configureCommonKotlin()
        target.configureCoroutines()
        target.configureKtfmt()
        target.configureTests()
        target.configureDetekt()

        target.plugins.withId(Plugins.KSP) { target.patchIosKspForCi() }

        target.addExtraSourceSets()
        target.configureHierarchyPlugin()
    }

    private fun Project.configureCommonKotlin() {
        kmpExtension.applyDefaultHierarchyTemplate()

        dependencies.add(
            "commonMainApi",
            dependencies.platform(libs.findLibrary("kotlin.bom").get().get().toString()),
        )

        // Only for tests.
        kmpExtension.sourceSets
            .getByName("commonTest")
            .languageSettings
            .optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")

        kmpExtension.targets.configureEach { target ->
            target.compilations.configureEach { compilation ->
                compilation.compileTaskProvider.configure { task ->
                    with(task.compilerOptions) {
                        if ("test" in task.name.lowercase()) {
                            freeCompilerArgs.add("-Xexpect-actual-classes")
                            freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                        }

                        // We need to rename the KLib library for iOS to avoid duplicate names. By
                        // default project.name is used, which conflicts with our module structure
                        // where many modules are named "public" or "impl". If that happens during
                        // compilation only code from one module is found.
                        //
                        // There is currently no DSL to set the KLib name. For more details see
                        // https://youtrack.jetbrains.com/issue/KT-38719.
                        freeCompilerArgs.add("-module-name")
                        freeCompilerArgs.add("$safePathString.${compilation.compilationName}")

                        extraWarnings.set(true)

                        // This warnings is reported in generated code.
                        freeCompilerArgs.add("-Xsuppress-warning=REDUNDANT_VISIBILITY_MODIFIER")

                        // This is a false positive. It reports warnings for lambdas where the
                        // "it" parameter isn't used and suggests to rename it to '_'.
                        freeCompilerArgs.add("-Xsuppress-warning=UNUSED_ANONYMOUS_PARAMETER")

                        // False positive for lambda with Unit as return type.
                        freeCompilerArgs.add("-Xsuppress-warning=REDUNDANT_RETURN_UNIT_TYPE")
                    }
                }
            }
        }

        allPlatforms().forEach { platform -> platform.configurePlatform() }
    }

    private fun Project.configureCoroutines() {
        kmpExtension.sourceSets.getByName("commonMain").dependencies {
            implementation(libs.findLibrary("coroutines.core").get().get().toString())
        }

        testingSourceSets.forEach { sourceSetName ->
            kmpExtension.sourceSets.getByName(sourceSetName).dependencies {
                api(libs.findLibrary("coroutines.test").get().get().toString())
                api(libs.findLibrary("turbine").get().get().toString())
            }
        }

        allPlatforms().forEach { platform -> platform.configureCoroutines() }
    }

    private fun Project.configureTests() {
        testingSourceSets.forEach { sourceSetName ->
            kmpExtension.sourceSets.getByName(sourceSetName).dependencies {
                api(kotlin("test"))
                api(libs.findLibrary("assertk").get().get().toString())
            }
        }

        releaseTask.configure { task ->
            task.dependsOn(allPlatforms().mapNotNull { it.unitTestTaskName })
        }
    }

    private fun Project.addExtraSourceSets() {
        val platforms = allPlatforms()
        if (platforms.any { it is Platform.Ios } && platforms.any { it is Platform.DesktopPlatform }) {
            setOf("Main", "Test").forEach { suffix ->
                val common = kmpExtension.sourceSets.getByName("common$suffix")

                val appleAndDesktop = kmpExtension.sourceSets.create("appleAndDesktop$suffix")
                appleAndDesktop.dependsOn(common)

                kmpExtension.sourceSets.named("apple$suffix").configure { it.dependsOn(appleAndDesktop) }
                kmpExtension.sourceSets.named("desktop$suffix").configure { it.dependsOn(appleAndDesktop) }
            }
        }
    }

    private fun Project.configureHierarchyPlugin() {
        plugins.apply(Plugins.KOTLIN_HIERARCHY)

        (extensions.getByType(KotlinMultiplatformExtension::class.java) as ExtensionAware)
            .extensions
            .getByType(KmpHierarchyConfig::class.java)
            .run {
                formats(Format.PNG, Format.SVG)
                withTestHierarchy = true
            }
    }

    internal companion object {
        val Project.kmpExtension: KotlinMultiplatformExtension
            get() = extensions.getByType(KotlinMultiplatformExtension::class.java)

        val Project.composeDependencies: ComposePlugin.Dependencies
            get() = ComposePlugin.Dependencies(this)

        val Project.composeMultiplatform: ComposeExtension
            get() = extensions.getByType(ComposeExtension::class.java)

        fun Project.enableCompose() {
            plugins.apply(Plugins.COMPOSE_COMPILER)
            plugins.apply(Plugins.COMPOSE_MULTIPLATFORM)

            kmpExtension.sourceSets.getByName("commonMain").dependencies {
                implementation(composeDependencies.runtime)
                implementation(composeDependencies.foundation)
            }

            allPlatforms().forEach { platform -> platform.configureCompose() }
        }

        fun Project.enableDi() {
            plugins.apply(Plugins.KSP)

            val kspExtension = extensions.getByType(KspExtension::class.java)

            // Disable this processor, because we implement our own version in order to support the
            // Scoped interface.
            kspExtension.arg(
                "software.amazon.lastmile.kotlin.inject.anvil.processor." + "ContributesBindingProcessor",
                "disabled",
            )

            tasks.withType(KspTask::class.java).configureEach { kspTask ->
                if (kspTask is KotlinCompile) {
                    kspTask.compilerOptions.jvmTarget.set(javaTarget)
                }
            }

            if (isKmpModule) {
                kmpExtension.sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.findLibrary("kotlin.inject.runtime").get().get().toString())
                    implementation(libs.findLibrary("kotlin.inject.anvil.runtime").get().get().toString())
                    implementation(
                        libs.findLibrary("kotlin.inject.anvil.runtime.optional").get().get().toString()
                    )
                    if (path != ":kotlin-inject:public") {
                        implementation(project(":kotlin-inject:public"))
                        if (!path.startsWith(":kotlin-inject-extensions:contribute:")) {
                            implementation(project(":kotlin-inject-extensions:contribute:public"))
                        }
                    }
                }
            } else {
                dependencies.add(
                    "implementation",
                    libs.findLibrary("kotlin.inject.runtime").get().get().toString(),
                )
                dependencies.add(
                    "implementation",
                    libs.findLibrary("kotlin.inject.anvil.runtime").get().get().toString(),
                )
                dependencies.add(
                    "implementation",
                    libs.findLibrary("kotlin.inject.anvil.runtime.optional").get().get().toString(),
                )
                if (path != ":kotlin-inject:public") {
                    dependencies.add("implementation", project(":kotlin-inject:public"))
                    if (!path.startsWith(":kotlin-inject-extensions:contribute:")) {
                        dependencies.add(
                            "implementation",
                            project(":kotlin-inject-extensions:contribute:public"),
                        )
                    }
                }
            }

            fun DependencyHandler.addKspProcessorDependencies(kspConfigurationName: String) {
                add(kspConfigurationName, libs.findLibrary("kotlin.inject.ksp").get().get().toString())
                add(
                    kspConfigurationName,
                    libs.findLibrary("kotlin.inject.anvil.compiler").get().get().toString(),
                )

                // Avoid creating a circular dependency.
                if (
                    path != ":kotlin-inject:public" &&
                    !path.startsWith(":kotlin-inject-extensions:contribute:")
                ) {
                    add(kspConfigurationName, project(":kotlin-inject-extensions:contribute:public"))
                    add(
                        kspConfigurationName,
                        project(":kotlin-inject-extensions:contribute:impl-code-generators"),
                    )
                }
            }

            if (isKmpModule) {
                kmpExtension.targets.configureEach {
                    if (it.name != "metadata") {
                        dependencies.addKspProcessorDependencies("ksp${it.name.capitalize()}")
                        dependencies.addKspProcessorDependencies("ksp${it.name.capitalize()}Test")
                    }
                }
            } else {
                dependencies.addKspProcessorDependencies("ksp")
            }
        }

        fun Project.enableMolecule() {
            plugins.apply(Plugins.COMPOSE_COMPILER)
            kmpExtension.sourceSets.getByName("commonMain").dependencies {
                implementation(libs.findLibrary("molecule.runtime").get().get().toString())
            }
        }

        fun Project.configureKtfmt() {
            plugins.apply(Plugins.KTFMT)

            extensions.getByType(KtfmtExtension::class.java).apply {
                googleStyle()
                manageTrailingCommas.set(true)
                removeUnusedImports.set(true)
            }

            releaseTask.configure { releaseTask -> releaseTask.dependsOn("ktfmtCheck") }
        }

        private fun Project.configureDetekt() {
            plugins.apply(Plugins.DETEKT)

            fun SourceTask.configureDefaultDetektTask() {
                // The :detekt task in a multiplatform project doesn't do anything, it has no
                // sources configured. Instead, the Detekt plugin creates a Gradle task for each
                // source set, which then need to be called manually. This is annoying and tedious.
                //
                // We make the default :detekt task analyze all .kt files, which is faster,
                // because only a single task runs, and we avoid all the wiring.
                setSource(layout.files("src"))
                exclude("**/*.kts")
                exclude("**/api/**")
                exclude("**/build/**")
                exclude("**/detekt/**")
            }

            // Make Detekt use the right version of Java
            tasks.withType(Detekt::class.java).configureEach { detekt ->
                detekt.jvmTarget = javaVersion.toString()

                if (detekt.name == "detekt") {
                    detekt.configureDefaultDetektTask()
                }
            }
            tasks.withType(DetektCreateBaselineTask::class.java).configureEach {
                it.jvmTarget = javaVersion.toString()

                if (it.name == "detektBaseline") {
                    it.configureDefaultDetektTask()
                }
            }
            with(extensions.getByType(DetektExtension::class.java)) {
                // From the Groovy DSL at https://detekt.github.io/detekt/gradle.html#groovy-dsl-3
                // This produces baselines named "detekt-baseline.xml"
                baseline = file("detekt/detekt-baseline.xml")
                // Config overrides
                config.from(rootProject.file("gradle/detekt-config.yml"))
                buildUponDefaultConfig = true
            }

            releaseTask.configure { releaseTask -> releaseTask.dependsOn("detekt") }
        }

        fun Project.patchIosKspForCi() {
            // TODO: Remove this workaround once https://github.com/google/ksp/issues/2243
            //  is resolved.
            //
            // KSP disables its tasks, when the platform is not supported by the host. However,
            // we enable the experimental feature kotlin.native.enableKlibsCrossCompilation=true in
            // order to build and test iOS libraries on Linux machines. KSP doesn't know about this
            // setting, so we have to enable the KSP tasks for iOS again.
            if (ci) {
                afterEvaluate {
                    tasks
                        .filter { task ->
                            task.name.startsWith("kspKotlinIos") || task.name.startsWith("kspTestKotlinIos")
                        }
                        .forEach { task -> task.setOnlyIf { true } }
                }
            }
        }

        private val Project.testingSourceSets
            get() = buildList { add("commonTest") }
    }
}
