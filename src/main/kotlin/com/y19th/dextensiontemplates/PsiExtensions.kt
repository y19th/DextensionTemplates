package com.y19th.dextensiontemplates

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.writeText
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile


internal fun PsiFile.writeWithPackage(content: String) {
    runWriteAction {
        virtualFile.writeText(
            "package ${virtualFile.packageName(project)}\n\n$content"
        )
    }
}

internal fun VirtualFile.packageName(project: Project): String {
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

internal fun PsiDirectory.getSubdirectory(name: String): PsiDirectory? {
    return subdirectories.find {
        it.virtualFile.name == name
    }
}

internal fun PsiDirectory.packageName(): String? {
    return virtualFile.packageName(project)
}

internal fun PsiDirectory.createDirectory(name: String, block: (PsiDirectory) -> Unit): PsiDirectory? {
    return runWriteAction {
        createSubdirectory(name)
        findSubdirectory(name)?.apply { block(this) }
    }
}

internal fun PsiDirectory.createFile(name: String, block: (PsiFile) -> Unit) {
    block(createFile(name))
}