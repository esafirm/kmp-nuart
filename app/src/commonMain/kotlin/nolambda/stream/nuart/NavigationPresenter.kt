package nolambda.stream.nuart

import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Inject
import nolambda.stream.nuart.home.HomePresenter
import software.amazon.app.platform.presenter.BaseModel
import software.amazon.app.platform.presenter.molecule.MoleculePresenter
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
  private val mainPresenter: () -> HomePresenter,
) : NavigationPresenter {

  @Composable
  override fun present(input: Unit): BaseModel {
    // If no user is logged in, then show the logged in screen.
    val presenter = mainPresenter()
    return presenter.present(Unit)
  }
}
