package org.example.plugin

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposePanel
import org.example.MyAppLogic
import org.example.MyPluginInterface
import java.awt.Component

class MyJarPlugin : MyPluginInterface {
    override fun createSwingComponent(): Component =
        ComposePanel().also {
            it.setContent {
                Column {
                    Text("MyJarPlugin")

                    var couter by remember { mutableStateOf(0) }
                    if (couter > 10) {
                        throw Exception("Simulate MyJarPlugin Counter exception")
                    }
                    Button({ couter++ }) {
                        Text("Click $couter")
                    }
                    Button({
                        throw Error("Simulate MyJarPlugin exception")
                    }) {
                        Text("MyJarPlugin, throw exception")
                    }
                }
            }
        }

    override fun updateSwingComponent(appLogic: MyAppLogic, component: Component) {
        println("some update with appLogic, someAppState: ${appLogic.someAppState()}")
    }
}
