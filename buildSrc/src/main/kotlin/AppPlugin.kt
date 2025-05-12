package nolambda.stream.nuart.gradle.buildSrc

import nolambda.stream.nuart.gradle.buildSrc.KmpPlugin.Companion.composeMultiplatform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.internal.VersionNumber
import org.jetbrains.compose.desktop.DesktopExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import kotlin.math.max

public open class AppPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(Plugins.ANDROID_APP)
    target.plugins.apply(Plugins.COMPOSE_COMPILER)

    target.plugins.apply(BasePlugin::class.java)
    target.plugins.apply(KmpPlugin::class.java)
    target.plugins.apply(BaseAndroidPlugin::class.java)

    target.configureAndroidSettings()
    target.makeSingleVariant()

    target.plugins.withId(Plugins.COMPOSE_MULTIPLATFORM) { target.configureDesktopApp() }
  }

  private fun Project.configureAndroidSettings() {
    android.defaultConfig.minSdk = 26
  }

  private fun Project.makeSingleVariant() {
    // Disable the release build type in the app module. We only need one build type
    // and everything else is overhead.
    androidComponents.beforeVariants { variant ->
      if (variant.buildType != "debug") {
        variant.enable = false
      }
    }
  }

  internal companion object {
    fun Project.configureDesktopApp() {
      composeMultiplatform.extensions.getByType(DesktopExtension::class.java).application.apply {
        mainClass = "nolambda.stream.nuart.MainKt"

        nativeDistributions.targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        nativeDistributions.packageName = "nolambda.stream.nuart"

        // During development the major version is 0, e.g. '0.0.1'. DMG must use a
        // major version equal or greater than 1:
        //
        // Illegal version for 'Dmg': '0.0.1' is not a valid build version.
        val version = VersionNumber.parse(versionName)
        nativeDistributions.packageVersion =
          VersionNumber(max(1, version.major), version.minor, version.patch, null).toString()
      }
    }
  }
}
