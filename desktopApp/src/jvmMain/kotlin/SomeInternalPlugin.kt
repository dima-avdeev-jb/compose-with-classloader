import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposePanel
import org.example.MyAppLogic
import java.awt.Component

object SOME_INTERNAL_PLUGIN : PluginWithName.InternalPlugin {
    override fun createSwingComponent(): Component = ComposePanel().apply {
        setContent {
            SomeInternalPlugin()
        }
    }

    override fun updateSwingComponent(appLogic: MyAppLogic, component: Component) {

    }

    override val name: String = "Internal1"
}

@Composable
private fun SomeInternalPlugin() {
    Column {
        var couter by remember { mutableStateOf(0) }
        if (couter > 10) {
            throw Exception("Simulate plugin Counter exception")
        }
        Button({ couter++ }) {
            Text("Click $couter")
        }
        Button({
            throw Error("Simulate plugin exception")
        }) {
            Text("Plugin, throw exception")
        }
    }
}
