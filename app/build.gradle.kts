plugins {
  id("nolambda.stream.nuart.app")
}

appPlatform {
  enableComposeUi(true)
  enableKotlinInject(true)
  enableModuleStructure(true)
  enableMoleculePresenters(true)
  addImplModuleDependencies(true)
}

dependencies {
  androidMainImplementation(libs.androidx.activity)

  commonMainImplementation(project(":templates:impl"))
  commonMainImplementation(project(":home:impl"))
}
