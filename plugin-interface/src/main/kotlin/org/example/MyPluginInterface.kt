package org.example

import java.awt.Component

interface MyAppLogic {
    fun someAppState(): String
}

interface MyPluginInterface {
    fun createSwingComponent(): Component
    fun updateSwingComponent(appLogic: MyAppLogic, component: Component)
}
