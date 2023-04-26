import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.MyAppLogic
import org.example.MyPluginInterface
import java.io.File
import java.net.URL
import java.net.URLClassLoader

class PluginException(
    val thread: Thread,
    val throwable: Throwable,
)

sealed interface PluginWithName {
    val name: String

    interface InternalPlugin : MyPluginInterface, PluginWithName

    class ExternalJarPlugin(
        override val name: String,
        val pathToJar: String,
        val className: String
    ) : PluginWithName
}

val plugins: List<PluginWithName> = listOf(
    SOME_INTERNAL_PLUGIN,
    PluginWithName.ExternalJarPlugin(
        "External1",
        "../plugin-jar/build/libs/plugin-jar.jar",
        className = "org.example.plugin.MyJarPlugin",
    )
)

fun main() {
    application {
        val pluginExceptionState: MutableState<PluginException?> = remember { mutableStateOf(null) }
        val currentPluginState: MutableState<PluginWithName?> = remember { mutableStateOf(null) }

        Window(onCloseRequest = ::exitApplication) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Main application")
                    Row {
                        plugins.forEach {
                            Button(
                                onClick = { currentPluginState.value = it },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                enabled = currentPluginState.value != it
                            ) {
                                Text("load plugin ${it.name}")
                            }
                        }
                    }
                    val currentPlugin = currentPluginState.value
                    if (currentPlugin != null) {
                        Box(Modifier.fillMaxSize()) {
                            val pluginException = pluginExceptionState.value
                            if (pluginException == null) {
                                Column(
                                    Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Plugin")
                                    Box(Modifier.border(width = 2.dp, color = Color.Black).padding(20.dp)) {
                                        val pluginInterface: MyPluginInterface =
                                            when (currentPlugin) {
                                                is PluginWithName.InternalPlugin -> currentPlugin
                                                is PluginWithName.ExternalJarPlugin -> Loader.loadJarPlugin(
                                                    currentPlugin
                                                )
                                            }
                                        SwingPanel(
                                            factory = {
                                                Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                                                    println("DefaultUncaughtExceptionHandler")
                                                    throwable.printStackTrace()
                                                    pluginExceptionState.value = PluginException(thread, throwable)
                                                }
                                                pluginInterface.createSwingComponent()
                                            },
                                            modifier = Modifier.size(400.dp, 400.dp),
                                            update = {
                                                pluginInterface.updateSwingComponent(
                                                    appLogic = object : MyAppLogic {
                                                        override fun someAppState(): String = "some App state"
                                                    },
                                                    component = it
                                                )
                                            }
                                        )
                                    }
                                }
                            } else {
                                Dialog(onCloseRequest = {}) {
                                    Column {
                                        Text("Exception occurs inside plugin")
                                        Button({
                                            pluginExceptionState.value = null
                                            currentPluginState.value = null
                                        }) {
                                            Text("Close plugin")
                                        }
                                        Button({
                                            pluginExceptionState.value = null
                                        }) {
                                            Text("Restart plugin")
                                        }
                                        Text(pluginException.throwable.message.orEmpty())
                                        TextField(pluginException.throwable.stackTraceToString(), {})
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

object Loader {
    fun loadJarPlugin(plugin: PluginWithName.ExternalJarPlugin): MyPluginInterface {
        println("File(\".\").absoluteFile: ${File(".").absoluteFile}")
        val jarPath = File(".").resolve(plugin.pathToJar)
        val urlClassLoader: URLClassLoader = URLClassLoader(
            arrayOf<URL>(jarPath.toURI().toURL()),
            this.javaClass.classLoader
        )
        val classToLoad: Class<*> = Class.forName(plugin.className, true, urlClassLoader)
        return classToLoad.getDeclaredConstructor().newInstance() as MyPluginInterface
    }
}
