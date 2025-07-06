package com.kazakago.swr.example

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SWR Compose Example",
    ) {
        App()
    }
}
