package com.luggage.plugindev

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

class LuggageFileTemplateGroupFactory : FileTemplateGroupDescriptorFactory {

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        // Individual file templates — for adding one file at a time to an existing plugin.
        // The full 4-file scaffold lives in LuggageNewPluginAction (File → New → Luggage Plugin).
        val group = FileTemplateGroupDescriptor("Luggage Files", null)
        group.addTemplate(FileTemplateDescriptor("Luggage manifest.json"))
        group.addTemplate(FileTemplateDescriptor("Luggage panel.html"))
        group.addTemplate(FileTemplateDescriptor("Luggage panel.css"))
        group.addTemplate(FileTemplateDescriptor("Luggage index.js"))
        return group
    }
}
