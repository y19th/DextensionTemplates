package com.y19th.dextensiontemplates.option

enum class DependencyInjection(val uiString: String) {
    Koin("Koin"), None("Without di")
}

fun String.toDependencyInjectionOption(): DependencyInjection = when (this) {
    DependencyInjection.Koin.uiString -> DependencyInjection.Koin
    else -> DependencyInjection.None
}