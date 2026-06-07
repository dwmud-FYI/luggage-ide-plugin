package com.luggage.plugindev

import com.intellij.ide.IdeView
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.Properties
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/*
 * File → New → Luggage Plugin. Single dialog -> creates a folder + the 4 standard files
 * (manifest.json, panel.html, panel.css, index.js) by expanding the bundled Velocity templates.
 * Matches the UX of the VS Code `luggage.newPlugin` command.
 */
class LuggageNewPluginAction : AnAction() {

    private val log = Logger.getInstance(LuggageNewPluginAction::class.java)

    override fun update(e: AnActionEvent) {
        val view = e.getData(LangDataKeys.IDE_VIEW)
        e.presentation.isEnabledAndVisible = e.project != null && view != null && view.directories.isNotEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val view: IdeView = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val parentPsi: PsiDirectory = view.orChooseDirectory ?: return
        val parent: VirtualFile = parentPsi.virtualFile

        val dialog = LuggageNewPluginDialog(project)
        if (!dialog.showAndGet()) return

        val id = dialog.pluginId
        val name = dialog.pluginName
        val author = dialog.author

        if (parent.findChild(id) != null) {
            Messages.showErrorDialog(project, "A folder named '$id' already exists here.", "Luggage")
            return
        }

        WriteCommandAction.runWriteCommandAction(project, "Create Luggage Plugin", null, {
            try {
                createPluginFiles(project, parent, id, name, author)
            } catch (ex: Exception) {
                log.warn("scaffold failed", ex)
                Messages.showErrorDialog(project, "Could not create plugin files: ${ex.message}", "Luggage")
            }
        })
    }

    private fun createPluginFiles(project: Project, parent: VirtualFile, id: String, name: String, author: String) {
        val folder = parent.createChildDirectory(this, id)
        val mgr = FileTemplateManager.getInstance(project)

        val props = Properties().apply {
            setProperty("PLUGIN_ID", id)
            setProperty("PLUGIN_NAME", name)
            setProperty("AUTHOR", author)
        }

        val files = listOf(
            "Luggage manifest.json" to "manifest.json",
            "Luggage panel.html"    to "panel.html",
            "Luggage panel.css"     to "panel.css",
            "Luggage index.js"      to "index.js",
        )

        for ((templateName, filename) in files) {
            // getJ2eeTemplate is annotated @NotNull but the runtime contract is "throws if missing",
            // which the Kotlin null-check warning doesn't reflect. Wrap in a try and surface a clear
            // message if the template ever goes missing from the jar.
            val template = try {
                mgr.getJ2eeTemplate(templateName)
            } catch (ex: Throwable) {
                throw IllegalStateException("Template '$templateName' not registered", ex)
            }
            val text = template.getText(props)
            val file = folder.createChildData(this, filename)
            file.setBinaryContent(text.toByteArray(Charsets.UTF_8))
        }

        val manifest = folder.findChild("manifest.json")
        if (manifest != null) FileEditorManager.getInstance(project).openFile(manifest, true)
    }
}

private class LuggageNewPluginDialog(project: Project) : DialogWrapper(project) {

    private val idField = JBTextField(20)
    private val nameField = JBTextField(20)
    private val authorField = JBTextField(20).apply {
        text = System.getProperty("user.name").orEmpty()
    }

    private var nameTouched = false

    val pluginId: String get() = idField.text.trim()
    val pluginName: String get() = nameField.text.trim()
    val author: String get() = authorField.text.trim()

    init {
        title = "New Luggage Plugin"
        setOKButtonText("Create")
        init()

        idField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = syncName()
            override fun removeUpdate(e: DocumentEvent?) = syncName()
            override fun changedUpdate(e: DocumentEvent?) = syncName()
        })

        nameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) { if (nameField.hasFocus()) nameTouched = true }
            override fun removeUpdate(e: DocumentEvent?) { if (nameField.hasFocus()) nameTouched = true }
            override fun changedUpdate(e: DocumentEvent?) { if (nameField.hasFocus()) nameTouched = true }
        })
    }

    private fun syncName() {
        if (!nameTouched) {
            nameField.text = titleCase(idField.text)
        }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            insets = Insets(4, 4, 4, 4)
            anchor = GridBagConstraints.WEST
        }

        c.gridx = 0; c.gridy = 0
        panel.add(JBLabel("Plugin id:"), c)
        c.gridx = 1
        panel.add(idField, c)

        c.gridx = 0; c.gridy = 1
        panel.add(JBLabel("Display name:"), c)
        c.gridx = 1
        panel.add(nameField, c)

        c.gridx = 0; c.gridy = 2
        panel.add(JBLabel("Author:"), c)
        c.gridx = 1
        panel.add(authorField, c)

        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = idField

    override fun doValidate(): ValidationInfo? {
        val id = pluginId
        if (id.isEmpty()) return ValidationInfo("Plugin id is required.", idField)
        if (!ID_PATTERN.matches(id))
            return ValidationInfo("Lowercase letters, digits and dashes; must start with a letter.", idField)
        if (pluginName.isEmpty()) return ValidationInfo("Display name is required.", nameField)
        return null
    }

    private fun titleCase(s: String): String =
        s.split("-").joinToString(" ") { part ->
            if (part.isEmpty()) part else part.substring(0, 1).uppercase() + part.substring(1)
        }

    companion object {
        private val ID_PATTERN = Regex("^[a-z][a-z0-9-]*$")
    }
}
