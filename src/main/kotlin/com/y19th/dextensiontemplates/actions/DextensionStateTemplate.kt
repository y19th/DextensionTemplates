package com.y19th.dextensiontemplates.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiDirectory
import com.y19th.dextensiontemplates.file.File
import com.y19th.dextensiontemplates.file.Folder
import com.y19th.dextensiontemplates.file.createFile
import com.y19th.dextensiontemplates.file.createFolder
import com.y19th.dextensiontemplates.option.DependencyInjection
import com.y19th.dextensiontemplates.option.ScreenOption

class DextensionStateTemplate : DextensionAction() {

    override fun onAction(event: AnActionEvent, input: String, directory: PsiDirectory, option: DependencyInjection) {
        directory.apply {
            createFolder(Folder.Logic) { logic ->
                logic.createFile(File.State(input))
            }
            createFolder(Folder.Ui) { ui ->
                ui.createFile(File.Component(input, ScreenOption.State))
                ui.createFile(File.Content(input, ScreenOption.State))
                ui.createFile(File.Screen(input, option))
            }

            if (option == DependencyInjection.Koin)
                createFolder(Folder.Di) {
                    it.createFile(File.DependencyModule(input))
                }
        }
    }
}