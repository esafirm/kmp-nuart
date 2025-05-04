package nolambda.stream.nuart.gradle.buildSrc

internal object Plugins {
  const val ANDROID_APP = "com.android.application"
  const val ANDROID_LIBRARY = "com.android.library"
  const val APP_PLATFORM = "software.amazon.app.platform"
  const val BINARY_COMPAT_VALIDATOR = "org.jetbrains.kotlinx.binary-compatibility-validator"
  const val COMPOSE_COMPILER = "org.jetbrains.kotlin.plugin.compose"
  const val COMPOSE_MULTIPLATFORM = "org.jetbrains.compose"
  const val DETEKT = "io.gitlab.arturbosch.detekt"
  const val KOTLIN_MULTIPLATFORM = "org.jetbrains.kotlin.multiplatform"
  const val KOTLIN_HIERARCHY = "io.github.terrakok.kmp-hierarchy"
  const val KOTLIN_JVM = "org.jetbrains.kotlin.jvm"
  const val KSP = "com.google.devtools.ksp"
  const val KTFMT = "com.ncorti.ktfmt.gradle"
  const val MAVEN_PUBLISH = "com.vanniktech.maven.publish"
}
