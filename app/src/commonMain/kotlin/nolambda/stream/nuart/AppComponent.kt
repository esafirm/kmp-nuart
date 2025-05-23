package nolambda.stream.nuart

import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import software.amazon.app.platform.scope.Scoped
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.ForScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

/**
 * Shared interface for the app component. The final components live in the platform specific source
 * folders in order to have access to platform specific code.
 */
@ContributesTo(AppScope::class)
@SingleIn(AppScope::class)
interface AppComponent {
  /**
   * Provide at least one implementation in the scope, otherwise kotlin-inject will complain. The
   * sample app actually doesn't have a [Scoped] instance in the app scope, that's why this is
   * needed.
   */
  @Provides
  @IntoSet
  @ForScope(AppScope::class)
  fun provideEmptyScoped(): Scoped = Scoped.NO_OP
}
