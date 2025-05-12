pluginManagement {
  repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    gradlePluginPortal()
    mavenCentral()
  }

  plugins {
    kotlin("jvm").version(extra["kotlin.version"] as String)
    id("org.jetbrains.compose").version(extra["compose.version"] as String)
    id("org.jetbrains.kotlin.plugin.compose").version(extra["kotlin.version"] as String)
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "nuart"

include(":app")
include(":home")
include(":templates:public")
include(":templates:impl")
