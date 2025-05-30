package com.y19th.dextensiontemplates

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty

class DextensionStateTemplate : AnAction() {

    private val actionTitle = "Dextension Feature Template"

    override fun actionPerformed(p0: AnActionEvent) {

        val input = Messages.showInputDialog(
            p0.project,
            "Enter a header of your feature (Main in MainFeature)",
            actionTitle,
            Messages.getQuestionIcon()
        )

        if (input != null && (input.isNotEmpty() || input.isNotBlank())) {
            val psiElement = p0.getData(CommonDataKeys.PSI_ELEMENT)

            if (psiElement != null && psiElement is PsiDirectory) {
                psiElement.also { element ->
                    element.createDirectory("logic") { logic ->
                        logic.createFile("${input}State.kt") { state ->
                            state.writeWithPackage(
                                """
                                import com.y19th.dextension.core.BaseState

                                internal data class ${input}State(
                                	val isLoading: Boolean = false
                                ): BaseState
                            """.trimIndent()
                            )
                        }
                    }
                    element.createDirectory("ui") { ui ->
                        ui.createFile("${input}Component.kt") { component ->
                            component.writeWithPackage(
                                """
                                    import com.arkivanov.decompose.ComponentContext
                                    import ${element.getSubdirectory("logic")?.packageName()}.${input}State
                                    import com.y19th.dextension.core.StateComponent
                                    
                                    internal class ${input}Component(
                                        componentContext: ComponentContext
                                    ): StateComponent<${input}State, ${input}Events>(
                                        componentContext = componentContext,
                                        initialState = ${input}State()
                                    ){
                                        
                                    }
                                """.trimIndent()

                            )
                        }
                        ui.createFile("${input}Content.kt") { component ->
                            component.writeWithPackage(
                                """
                                    import androidx.compose.runtime.Composable
                                    import com.y19th.dextension.compose.collectAsImmediateState
                                    
                                    @Composable
                                    internal fun ${input}Content(
                                        component: ${input}Component
                                    ) { 
                                        val state = component.state.collectAsImmediateState()
                                    }
                                """.trimIndent()

                            )
                        }
                        ui.createFile("${input}Screen.kt") { component ->
                            component.writeWithPackage(
                                """
                                    import androidx.compose.runtime.Composable
                                    import com.arkivanov.decompose.ComponentContext
                                    import com.y19th.dextension.koin.getComponent
                                    import com.y19th.dextension.koin.KoinScreen
                                    import ${ui.packageName()}.${input}Component
                                    
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
                            )
                        }
                    }
                    element.createDirectory("di") { di ->
                        di.createFile("${input}Module.kt") { file ->
                            file.writeWithPackage(
                                """
                                    import org.koin.core.module.dsl.factoryOf
                                    import org.koin.dsl.bind
                                    import org.koin.dsl.module
                                    import ${element.getSubdirectory("ui")?.packageName()}.${input}Component
                                    import ${element.getSubdirectory("ui")?.packageName()}.${input}Screen
                                    import ${element.getSubdirectory("ui")?.packageName()}.${input}ScreenImpl
                                    
                                    val ${input.lowercase()}Module = module {
                                        factoryOf(::${input}Component)
                                        factoryOf(::${input}ScreenImpl).bind<${input}Screen>()
                                    }
                                """.trimIndent()

                            )
                        }
                    }

                }
            } else {
                showErrorDialog(
                    "You should create this template into directory"
                )
            }
        } else {
            showErrorDialog(
                "You should enter a valid header, it can not be empty or blank"
            )
        }
    }

    private fun showErrorDialog(message: String) {
        Messages.showErrorDialog(
            message,
            actionTitle
        )
    }
}