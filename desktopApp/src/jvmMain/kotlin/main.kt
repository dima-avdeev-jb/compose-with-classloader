import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import java.net.URL
import java.net.URLClassLoader

fun main() {
    Loader.loadLibJar()
    if(false)application {
        Window(onCloseRequest = ::exitApplication) {
            Box(Modifier.fillMaxSize()) {
                Text("App")
            }
        }
    }
}

object Loader {
    fun loadLibJar() {
        val jarPath = File("/Users/dim/Desktop/github/dima-avdeev-jb/compose-with-classloader/jar-library/build/libs/jar-library.jar")
        val child = URLClassLoader(
            arrayOf<URL>(jarPath.toURI().toURL()),
            this.javaClass.classLoader
        )
        val classToLoad = Class.forName("com.example.lib.LibImplementation", true, child)
        val method = classToLoad.getDeclaredMethod("hello")
        val instance = classToLoad.newInstance()
        val result: Any = method.invoke(instance)
        println(result)
    }
}
