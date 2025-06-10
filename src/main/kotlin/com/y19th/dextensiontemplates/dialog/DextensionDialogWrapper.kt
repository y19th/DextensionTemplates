package com.y19th.dextensiontemplates.dialog

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.y19th.dextensiontemplates.option.DependencyInjection
import com.y19th.dextensiontemplates.option.toDependencyInjectionOption
import javax.swing.JComponent

class DextensionDialogWrapper(
    action: AnActionEvent
) : DialogWrapper(action.project) {

    private var fieldProperty: Cell<JBTextField>? = null
    private val radioList = mutableListOf<Cell<JBRadioButton>>()
    val input: String get() = requireNotNull(fieldProperty).component.text
    val option: DependencyInjection
        get() = radioList
            .find { it.component.isSelected }
            ?.component
            ?.text
            ?.toDependencyInjectionOption()
            ?: DependencyInjection.None

    init {
        title = "Dextension Feature Template"
        super.init()
    }

    override fun createCenterPanel(): JComponent? {
        return panel {
            row("Enter a header of your feature (Main in MainFeature)") {
                fieldProperty = textField()
                    .focused()
            }

            buttonsGroup {
                row("Dependency Injection") {
                    radioButton(DependencyInjection.Koin.uiString)
                        .selected(true)
                        .also { radioList.add(it) }
                    radioButton(DependencyInjection.None.uiString).also {
                        radioList.add(it)
                    }
                }
            }
        }
    }
}