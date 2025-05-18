package nolambda.stream.nuart.home

import software.amazon.app.platform.presenter.BaseModel
import software.amazon.app.platform.presenter.molecule.MoleculePresenter

interface HomePresenter : MoleculePresenter<Unit, HomePresenter.State> {
  data class State(
    val isLoading: Boolean = false,
  ) : BaseModel
}
