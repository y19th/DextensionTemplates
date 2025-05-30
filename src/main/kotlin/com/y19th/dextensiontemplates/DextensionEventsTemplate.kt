package com.y19th.dextensiontemplates

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import kotlin.text.isNotBlank
import kotlin.text.isNotEmpty
import kotlin.text.lowercase

class DextensionEventsTemplate : AnAction() {

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
                        logic.createFile("${input}Events.kt") { events ->
                            events.writeWithPackage(
                                """
                                    import com.y19th.dextension.core.BaseEvents
                                    
                                    internal sealed interface ${input}Events : BaseEvents {
                                        
                                        data object OnNavigateBack: ${input}Events
                                    }
                                """.trimIndent()
                            )
                        }
                    }
                    element.createDirectory("ui") { ui ->
                        ui.createFile("${input}Component.kt") { component ->
                            component.writeWithPackage(
                                """
                                    import com.arkivanov.decompose.ComponentContext
                                    import ${element.getSubdirectory("logic")?.packageName()}.${input}Events
                                    import com.y19th.dextension.core.EventComponent
                                    
                                    internal class ${input}Component(
                                        componentContext: ComponentContext
                                    ): EventComponent<${input}Events>(
                                        componentContext = componentContext,
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

                            )
                        }
                        ui.createFile("${input}Content.kt") { component ->
                            component.writeWithPackage(
                                """
                                    import androidx.compose.runtime.Composable
                                    import com.y19th.dextension.compose.rememberHandleEvents
                                    
                                    @Composable
                                    internal fun ${input}Content(
                                        component: ${input}Component
                                    ) { 
                                        val handleEvents = component.rememberHandleEvents()
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