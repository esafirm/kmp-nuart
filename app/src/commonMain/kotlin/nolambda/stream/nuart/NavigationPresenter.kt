package nolambda.stream.nuart

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Inject
import software.amazon.app.platform.inject.ContributesRenderer
import software.amazon.app.platform.presenter.BaseModel
import software.amazon.app.platform.presenter.molecule.MoleculePresenter
import software.amazon.app.platform.renderer.ComposeRenderer
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding

/**
 * A presenter that hosts other presenters and returns their models. For that reason this presenter
 * doesn't have its own [BaseModel] type and returns [BaseModel].
 */
interface NavigationPresenter : MoleculePresenter<Unit, BaseModel>

@Inject
@ContributesBinding(AppScope::class)
class NavigationPresenterImpl(
  private val mainPresenter: () -> FullScreenPresenter,
) : NavigationPresenter {

  @Composable
  override fun present(input: Unit): BaseModel {
    // If no user is logged in, then show the logged in screen.
    val presenter = mainPresenter()
    return presenter.present(Unit)
  }
}

interface FullScreenPresenter : MoleculePresenter<Unit, FullScreenPresenter.State> {
  data class State(
    val isLoading: Boolean = false,
  ) : BaseModel
}

@Inject
@ContributesBinding(AppScope::class)
class FullScreenPresenterImpl : FullScreenPresenter {
  @Composable
  override fun present(input: Unit): FullScreenPresenter.State {
    return FullScreenPresenter.State(
      isLoading = true,
    )
  }
}

@ContributesRenderer
class FullScreenRenderer : ComposeRenderer<FullScreenPresenter.State>() {
  @Composable
  override fun Compose(model: FullScreenPresenter.State) {
    BasicText("Hello world! - ${model.isLoading}")
  }
}
