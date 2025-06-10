package com.y19th.dextensiontemplates.option

enum class ScreenOption {
    State, Event, Effect, Default
}

fun ScreenOption.onStateOption(block: () -> String) = if (this != ScreenOption.Event) block() else ""
fun ScreenOption.onEventOption(block: () -> String) = if (this != ScreenOption.State) block() else ""