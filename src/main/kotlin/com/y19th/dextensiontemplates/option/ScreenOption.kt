package com.y19th.dextensiontemplates.option

enum class ScreenOption {
    State, Event, Effect, Default
}

inline fun ScreenOption.onStateOption(block: () -> String) = if (this != ScreenOption.Event) block() else ""
inline fun ScreenOption.onEventOption(block: () -> String) = if (this != ScreenOption.State) block() else ""
inline fun ScreenOption.onEffectOption(block: () -> String) = if (this == ScreenOption.Effect) block() else ""