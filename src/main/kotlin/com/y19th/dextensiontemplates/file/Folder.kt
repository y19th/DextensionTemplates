package com.y19th.dextensiontemplates.file

import com.intellij.psi.PsiDirectory
import com.y19th.dextensiontemplates.createDirectory

fun PsiDirectory.createFolder(folder: Folder, block: (PsiDirectory) -> Unit) {
    createDirectory(folder.title, block)
}

enum class Folder(val title: String) {
    Ui("ui"), Logic("logic"), Di("di");
}