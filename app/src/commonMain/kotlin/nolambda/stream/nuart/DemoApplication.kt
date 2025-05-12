package nolambda.stream.nuart

import software.amazon.app.platform.scope.RootScopeProvider
import software.amazon.app.platform.scope.Scope
import software.amazon.app.platform.scope.di.addDiComponent

/**
 * Shared class between the platform to manage the root scope. It itself implements the
 * [RootScopeProvider] interface.
 */
class DemoApplication : RootScopeProvider {

  private var _rootScope: Scope? = null

  override val rootScope: Scope
    get() = checkNotNull(_rootScope) { "Must call create() first." }

  /** Creates the root scope and remembers the instance. */
  fun create(appComponent: AppComponent) {
    check(_rootScope == null) { "create() should be called only once." }

    _rootScope = Scope.buildRootScope {
      addDiComponent(appComponent)
    }
  }

  /** Destroys the root scope. */
  fun destroy() {
    rootScope.destroy()
    _rootScope = null
  }
}
