package com.y19th.dextensiontemplates

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile


class DextensionFullTemplate : AnAction() {

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
                    element.createDirectory("di") { di ->
                        di.createFile("${input}Module.kt") { file ->
                            file.writeWithPackage(
                                """
                                    import org.koin.dsl.bind
                                    import org.koin.dsl.module
                                    
                                    val ${input}Module = module {
                                    
                                    }
                                """.trimIndent()

                            )
                        }
                    }
                    element.createDirectory("logic") { logic ->
                        logic.createFile("${input}Events.kt") { events ->
                            events.writeWithPackage(
                                """
                                    internal sealed interface ${input}Events : BaseEvents {
                                        
                                        data object OnNavigateBack: ${input}Events
                                    }
                                """.trimIndent()
                            )
                        }

                        logic.createFile("${input}State.kt") { state ->
                            state.writeWithPackage(
                                "internal data class ${input}State(\n\tval isLoading: Boolean = false\n): BaseState"
                            )
                        }
                    }
                    element.createDirectory("ui") { ui ->
                        ui.createFile("${input}Component.kt") { component ->
                            component.writeWithPackage(
                                """
                                    import com.arkivanov.decompose.ComponentContext
                                    import ${element.getSubdirectory("logic")?.packageName()}.${input}State
                                    import ${element.getSubdirectory("logic")?.packageName()}.${input}Events
                                    import com.y19th.dextension.core.ScreenComponent
                                    
                                    internal class ${input}Component(
                                        componentContext: ComponentContext
                                    ): ScreenComponent<${input}State, ${input}Events>{
                                        
                                        override fun handleEvent(event: ${input}Events) {
                                            when(event) {
                                                
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
                                    import com.y19th.dextension.compose.collectAsImmediateState
                                    import com.y19th.dextension.compose.rememberHandleEvents
                                    
                                    @Composable
                                    internal fun ${input}Content(
                                        component: ${input}Component
                                    ) {
                                        
                                        val state = component.state.collectAsImmediateState()
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
                                    import com.y19th.dextension.compose.Screen
                                    import ${ui.packageName()}.${input}Component
                                    
                                    interface ${input}Screen : Screen

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

    private fun PsiFile.writeWithPackage(content: String) {
        runWriteAction {
            virtualFile.writeText(
                "package ${virtualFile.packageName(project)}\n\n$content"
            )
        }
    }

    private fun VirtualFile.packageName(project: Project): String {
        val sourceRoot = ProjectRootManager.getInstance(project)
            .fileIndex
            .getSourceRootForFile(this)
            ?: return ""

        return VfsUtilCore
            .getRelativePath(this, sourceRoot, '.')
            ?.let { if (isDirectory) it else it.replace(name, "") }
            ?.dropLastWhile { it == '.' }
            ?: ""
    }

    private fun PsiDirectory.getSubdirectory(name: String): PsiDirectory? {
        return subdirectories.find {
            it.virtualFile.name == name
        }
    }

    private fun PsiDirectory.packageName(): String? {
        return virtualFile.packageName(project)
    }

    private fun PsiDirectory.createDirectory(name: String, block: (PsiDirectory) -> Unit): PsiDirectory? {
        return runWriteAction {
            createSubdirectory(name)
            findSubdirectory(name)?.apply { block(this) }
        }
    }

    private fun PsiDirectory.createFile(name: String, block: (PsiFile) -> Unit) {
        block(createFile(name))
    }

    private fun showErrorDialog(message: String) {
        Messages.showErrorDialog(
            message,
            actionTitle
        )
    }
}