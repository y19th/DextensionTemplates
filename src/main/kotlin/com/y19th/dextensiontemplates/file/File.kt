package com.y19th.dextensiontemplates.file

import com.intellij.psi.PsiDirectory
import com.y19th.dextensiontemplates.createFile
import com.y19th.dextensiontemplates.getSubdirectory
import com.y19th.dextensiontemplates.option.DependencyInjection
import com.y19th.dextensiontemplates.option.ScreenOption
import com.y19th.dextensiontemplates.option.onEventOption
import com.y19th.dextensiontemplates.option.onStateOption
import com.y19th.dextensiontemplates.packageName
import com.y19th.dextensiontemplates.writeWithPackage

fun PsiDirectory.createFile(file: File) {
    file.apply {
        createFile(title) { createdFile ->
            createdFile.writeWithPackage(parentDirectory?.content() ?: content())
        }
    }
}


sealed class File(val title: String) {

    data class DependencyModule(private val input: String) : File(
        title = "${input}Module.kt",
    ) {
        override fun PsiDirectory.content(): String {
            return """
                import org.koin.core.module.dsl.factoryOf
                import org.koin.dsl.bind
                import org.koin.dsl.module
                import ${getSubdirectory("ui")?.packageName()}.${input}Component
                import ${getSubdirectory("ui")?.packageName()}.${input}Screen
                import ${getSubdirectory("ui")?.packageName()}.${input}ScreenImpl
                
                val ${input.lowercase()}Module = module {
                    factoryOf(::${input}Component)
                    factoryOf(::${input}ScreenImpl).bind<${input}Screen>()
                }
            """.trimIndent()

        }
    }

    data class State(private val input: String) : File(
        title = "${input}State.kt"
    ) {
        override fun PsiDirectory.content(): String {
            return """
                import com.y19th.dextension.core.BaseState

                internal data class ${input}State(
                	val isLoading: Boolean = false
                ): BaseState
            """.trimIndent()
        }
    }

    data class Events(private val input: String) : File(
        title = "${input}Events.kt"
    ) {
        override fun PsiDirectory.content(): String {
            return """
                import com.y19th.dextension.core.BaseEvents
                                    
                internal sealed interface ${input}Events : BaseEvents {
                    
                    data object OnNavigateBack: ${input}Events
                }
            """.trimIndent()
        }
    }

    data class Component(
        private val input: String,
        private val option: ScreenOption
    ) : File(
        title = "${input}Component.kt"
    ) {
        override fun PsiDirectory.content(): String {
            return when (option) {
                ScreenOption.State ->
                    """
                        import com.arkivanov.decompose.ComponentContext
                        import ${getSubdirectory("logic")?.packageName()}.${input}State
                        import com.y19th.dextension.core.ScreenComponent
                        
                        internal class ${input}Component(
                            componentContext: ComponentContext
                        ): StateComponent<${input}State>(
                            componentContext = componentContext,
                            initialState = ${input}State()
                        ){
                            
                        }
                """.trimIndent()

                ScreenOption.Event ->
                    """
                        import com.arkivanov.decompose.ComponentContext
                        import ${getSubdirectory("logic")?.packageName()}.${input}Events
                        import com.y19th.dextension.core.ScreenComponent
                        
                        internal class ${input}Component(
                            componentContext: ComponentContext
                        ): EventComponent<${input}Events>(
                            componentContext = componentContext
                        ){
                            override fun handleEvent(event: ${input}Events) {
                                when(event) {
                                    ${input}Events.OnNavigateBack -> {
                                        TODO()
                                    }
                                }
                            }
                        }
                """.trimIndent()

                ScreenOption.Effect ->
                    """
                        import com.arkivanov.decompose.ComponentContext
                        ${option.onStateOption { "import ${getSubdirectory("logic")?.packageName()}.${input}State" }}
                        ${option.onEventOption { "import ${getSubdirectory("logic")?.packageName()}.${input}Events" }}
                        import com.y19th.dextension.core.ScreenComponent
                        
                        internal class ${input}Component(
                            componentContext: ComponentContext
                        ): ScreenComponent<${input}State, ${input}Events>(
                            componentContext = componentContext,
                            initialState = ${input}State()
                        ){
                            override fun handleEvent(event: ${input}Events) {
                                when(event) {
                                    ${input}Events.OnNavigateBack -> {
                                        TODO()
                                    }
                                }
                            }
                        }
                """.trimIndent()

                ScreenOption.Default ->
                    """
                        import com.arkivanov.decompose.ComponentContext
                        import ${getSubdirectory("logic")?.packageName()}.${input}State
                        import ${getSubdirectory("logic")?.packageName()}.${input}Events
                        import com.y19th.dextension.core.ScreenComponent
                        
                        internal class ${input}Component(
                            componentContext: ComponentContext
                        ): ScreenComponent<${input}State, ${input}Events>(
                            componentContext = componentContext,
                            initialState = ${input}State()
                        ){
                            override fun handleEvent(event: ${input}Events) {
                                when(event) {
                                    ${input}Events.OnNavigateBack -> {
                                        TODO()
                                    }
                                }
                            }
                        }
                """.trimIndent()
            }
        }
    }

    data class Content(
        private val input: String,
        private val option: ScreenOption
    ) : File(
        title = "${input}Content.kt"
    ) {
        override fun PsiDirectory.content(): String {
            return """
                import androidx.compose.runtime.Composable
                ${option.onStateOption { "import com.y19th.dextension.compose.collectAsImmediateState" }}
                ${option.onEventOption { "import com.y19th.dextension.compose.rememberHandleEvents" }}               
                
                @Composable
                internal fun ${input}Content(
                    component: ${input}Component
                ) { 
                    ${option.onStateOption { "val state = component.state.collectAsImmediateState()" }}
                    ${option.onEventOption { "val handleEvents = component.rememberHandleEvents()" }}
                }
            """.trimIndent()
        }
    }

    data class Screen(
        private val input: String,
        private val option: DependencyInjection
    ) : File(
        title = "${input}Screen.kt"
    ) {
        override fun PsiDirectory.content(): String {
            return when (option) {
                DependencyInjection.Koin -> {
                    """
                        import androidx.compose.runtime.Composable
                        import com.arkivanov.decompose.ComponentContext
                        import com.y19th.dextension.koin.getComponent
                        import com.y19th.dextension.koin.KoinScreen
                        import ${getSubdirectory("ui")?.packageName()}.${input}Component
                        
                        interface ${input}Screen : KoinScreen
        
                        internal class ${input}ScreenImpl(
                            override val componentContext: ComponentContext
                        ) : ${input}Screen {
        
                            private val component: ${input}Component = getComponent(componentContext)
        
                            @Composable
                            override fun Content() {
                                ${input}Content(component)
                            }
                        }
                    """.trimIndent()
                }

                DependencyInjection.None -> {
                    """
                        import androidx.compose.runtime.Composable
                        import com.arkivanov.decompose.ComponentContext
                        import com.y19th.dextension.compose.Screen
                        import ${getSubdirectory("ui")?.packageName()}.${input}Component
                        
                        interface ${input}Screen : Screen
                        
                        internal class ${input}ScreenImpl(
                            override val componentContext: ComponentContext
                        ) : ${input}Screen {
                        
                            private val component: ${input}Component = ${input}Component(componentContext)
                            
                            @Composable
                            override fun Content() {
                                ${input}Content(component)
                            }
                        }
                    """.trimIndent()
                }
            }
        }
    }

    abstract fun PsiDirectory.content(): String
}