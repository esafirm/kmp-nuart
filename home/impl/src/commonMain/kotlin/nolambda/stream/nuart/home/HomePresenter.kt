package nolambda.stream.nuart.home

import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

@Inject
@ContributesBinding(AppScope::class)
class HomePresenterImpl : HomePresenter {
  @Composable
  override fun present(input: Unit): HomePresenter.State {
    return HomePresenter.State(
      isLoading = true,
    )
  }
}
