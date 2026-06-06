package com.luggage.plugindev

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

class LuggageManifestSchemaProviderFactory : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> =
        listOf(LuggageManifestSchemaProvider(project))
}

class LuggageManifestSchemaProvider(private val project: Project) : JsonSchemaFileProvider {

    override fun isAvailable(file: VirtualFile): Boolean =
        LuggageManifestDetector.looksLikeManifest(file)

    override fun getName(): String = "Luggage Plugin Manifest"

    override fun getSchemaFile(): VirtualFile? {
        val url = javaClass.getResource("/schemas/manifest.schema.json") ?: return null
        return VfsUtil.findFileByURL(url)
    }

    override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema
}
