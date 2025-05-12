plugins {
  id("nolambda.stream.nuart.app")
}

appPlatform {
  enableComposeUi(true)
  enableKotlinInject(true)
  enableModuleStructure(true)
  enableMoleculePresenters(true)
  addImplModuleDependencies(true)
  addPublicModuleDependencies(false)
}

dependencies {
  androidMainImplementation(libs.androidx.activity)

  commonMainImplementation(project(":templates:impl"))
}
