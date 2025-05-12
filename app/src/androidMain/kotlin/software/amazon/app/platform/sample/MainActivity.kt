package software.amazon.app.platform.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import software.amazon.app.platform.scope.RootScopeProvider

/**
 * The only `Activity` of our sample app. This class is just an entry point to start rendering
 * templates.
 */
class MainActivity : ComponentActivity() {

  private val rootScopeProvider
    get() = application as RootScopeProvider

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }
}
