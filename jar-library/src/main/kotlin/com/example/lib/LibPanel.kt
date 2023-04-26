package com.example.lib

interface UILib {

}

class LibImplementation {
    fun hello():String {
        throw Error("Simulate exception")
        return "Hello LibImplementation 2"
    }
}
