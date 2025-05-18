package nolambda.stream.nuart.template

import androidx.compose.runtime.Composable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.app.platform.presenter.molecule.MoleculePresenter
import software.amazon.app.platform.presenter.molecule.returningCompositionLocalProvider
import software.amazon.app.platform.presenter.template.toTemplate

/**
 * A presenter that wraps any other presenter and turns the emitted models from the other presenter
 * into [NuartAppTemplate]s.
 *
 * Inject [Factory] to create a new instance of [NuartTemplatePresenter].
 */
@Inject
class NuartTemplatePresenter(@Assisted private val rootPresenter: MoleculePresenter<Unit, *>) :
  MoleculePresenter<Unit, NuartAppTemplate> {
  @Composable
  override fun present(input: Unit): NuartAppTemplate {
    @Suppress("RemoveEmptyParenthesesFromLambdaCall")
    return returningCompositionLocalProvider(
      // Add local composition providers if needed.
    ) {
      rootPresenter.present(Unit).toTemplate<NuartAppTemplate> {
        NuartAppTemplate.FullScreenTemplate(it)
      }
    }
  }

  /**
   * A factory to instantiate a new [NuartTemplatePresenter] instance. This implementation hides
   * that assisted injection with kotlin-inject is used. It's easier to inject this [Factory] than
   * the lambda provided by kotlin-inject.
   */
  @Inject
  class Factory(private val factory: (MoleculePresenter<Unit, *>) -> NuartTemplatePresenter) {
    /**
     * Create a new [NuartTemplatePresenter]. The given [presenter] will be wrapped and its
     * models are transformed into a [NuartAppTemplate] with [NuartAppTemplate.FullScreenTemplate]
     * as default. The given [presenter] can override the template by either returning
     * [NuartAppTemplate] directly or making its [BaseModel] type implement [ModelDelegate].
     */
    fun create(presenter: MoleculePresenter<Unit, *>): NuartTemplatePresenter {
      return factory(presenter)
    }
  }
}
