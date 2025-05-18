package nolambda.stream.nuart.home

import androidx.compose.runtime.Composable
import software.amazon.app.platform.inject.ContributesRenderer
import software.amazon.app.platform.renderer.ComposeRenderer

@ContributesRenderer
class HomeRenderer : ComposeRenderer<HomePresenter.State>() {
  @Composable
  override fun Compose(model: HomePresenter.State) {
    HomeScreen(state = model)
  }
}

@Composable
private fun HomeScreen(state: HomePresenter.State) {

}
