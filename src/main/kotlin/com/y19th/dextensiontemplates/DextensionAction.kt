package com.y19th.dextensiontemplates

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.y19th.dextensiontemplates.dialog.DextensionDialogWrapper
import com.y19th.dextensiontemplates.option.DependencyInjection


abstract class DextensionAction(
    private val actionTitle: String = "Dextension Feature Template"
) : AnAction() {

    override fun actionPerformed(p0: AnActionEvent) {
        DextensionDialogWrapper(p0).apply {
            if (showAndGet()) {
                println("option selected: $option")
                if (input.isNotEmpty() || input.isNotBlank()) {
                    val psiElement = p0.getData(CommonDataKeys.PSI_ELEMENT)

                    if (psiElement != null && psiElement is PsiDirectory) {
                        onAction(event = p0, input = input, directory = psiElement, option = option)
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

            } else {
                showErrorDialog(
                    "Unknown error. Try again."
                )
            }
        }

    }

    private fun showErrorDialog(message: String) {
        Messages.showErrorDialog(
            message,
            actionTitle
        )
    }

    abstract fun onAction(
        event: AnActionEvent,
        input: String,
        directory: PsiDirectory,
        option: DependencyInjection
    )
}

internal fun PsiDirectory.createScreenFileWithDependencyOption(
    input: String,
    dependencyInjection: DependencyInjection
) {
    when (dependencyInjection) {
        DependencyInjection.Koin -> {
            createFile("${input}Screen.kt") { component ->
                component.writeWithPackage(
                    """
                                    import androidx.compose.runtime.Composable
                                    import com.arkivanov.decompose.ComponentContext
                                    import com.y19th.dextension.koin.getComponent
                                    import com.y19th.dextension.koin.KoinScreen
                                    import ${packageName()}.${input}Component
                                    
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

        DependencyInjection.None -> {
            createFile("${input}Screen.kt") { component ->
                component.writeWithPackage(
                    """
                                    import androidx.compose.runtime.Composable
                                    import com.arkivanov.decompose.ComponentContext
                                    import ${packageName()}.${input}Component
                                    
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
                )
            }

        }
    }

}
