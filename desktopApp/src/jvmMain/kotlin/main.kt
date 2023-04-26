import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader

class PluginException(
    val thread: Thread,
    val throwable: Throwable,
)

sealed interface MyPlugin {
    val name: String

    class InternalPlugin(
        override val name: String,
        val content: @Composable () -> Unit
    ) : MyPlugin

    class ExternalJarPlugin(
        override val name: String,
        val pathToJar: String,
        className: String,
        functionName: String
    ) : MyPlugin
}

val plugins: List<MyPlugin> = listOf(
    MyPlugin.InternalPlugin("Internal1") {
        SomeInternalPlugin()
    },
    MyPlugin.ExternalJarPlugin(
        "External1",
        "/Users/dim/Desktop/github/dima-avdeev-jb/compose-with-classloader/jar-library/build/libs/jar-library.jar",
        className = "com.example.lib.LibImplementation",
        functionName = "hello",
    )
)

fun main() {
    application {
        val pluginExceptionState: MutableState<PluginException?> = remember { mutableStateOf(null) }
        val currentPluginState: MutableState<MyPlugin?> = remember { mutableStateOf(null) }

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
                                        SwingPanel(
                                            factory = {
                                                Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                                                    println("DefaultUncaughtExceptionHandler")
                                                    throwable.printStackTrace()
                                                    pluginExceptionState.value = PluginException(thread, throwable)
                                                }
                                                when (currentPlugin) {
                                                    is MyPlugin.InternalPlugin -> {
                                                        ComposePanel().also {
                                                            it.setContent {
                                                                SomeInternalPlugin()
                                                            }
                                                        }
                                                    }

                                                    is MyPlugin.ExternalJarPlugin -> {
                                                        TODO()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(400.dp, 400.dp),
                                            update = { println("update") }
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

@Composable
fun SomeInternalPlugin() {
    Column {
        var couter by remember { mutableStateOf(0) }
        if (couter > 10) {
            throw Exception("Simulate library Counter exception")
        }
        Button({ couter++ }) {
            Text("Click $couter")
        }
        Button({
            throw Error("Simulate library exception")
        }) {
            Text("Library, throw exception")
        }
    }
}

object Loader {
    fun loadLibJar() {
        val jarPath =
            File("/Users/dim/Desktop/github/dima-avdeev-jb/compose-with-classloader/jar-library/build/libs/jar-library.jar")
        val urlClassLoader: URLClassLoader = URLClassLoader(
            arrayOf<URL>(jarPath.toURI().toURL()),
            this.javaClass.classLoader
        )
        Thread.setDefaultUncaughtExceptionHandler(object : Thread.UncaughtExceptionHandler {
            override fun uncaughtException(t: Thread?, e: Throwable?) {
                e?.printStackTrace()
                println("uncaughtException, e?.message: ${e?.message}")
            }
        })
        val classToLoad: Class<*> = Class.forName("com.example.lib.LibImplementation", true, urlClassLoader)
        val method: Method = classToLoad.getDeclaredMethod("hello")
        val instance = classToLoad.newInstance()
        val result: Any = method.invoke(instance)
        println(result)
    }
}
