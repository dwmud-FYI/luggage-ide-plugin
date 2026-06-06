package com.luggage.plugindev

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText

/*
 * Shape-based Luggage manifest detection. We don't want to claim every manifest.json on the planet
 * (web extensions, npm packages with that filename, etc.), so we only kick in when the JSON has
 * Luggage-specific fingerprints: id + name + version, plus at least one of panel/minClientVersion
 * or main === "index.js".
 */
object LuggageManifestDetector {

    private val log = Logger.getInstance(LuggageManifestDetector::class.java)

    fun looksLikeManifest(file: VirtualFile): Boolean {
        if (file.name != "manifest.json") return false

        val text = try {
            file.readText()
        } catch (e: Exception) {
            log.debug("could not read ${file.path}", e)
            return false
        }

        // Cheap textual check before parsing — keeps us off the JSON parser for the
        // 99% case of files that obviously aren't Luggage manifests.
        if (!text.contains("\"id\"") || !text.contains("\"name\"") || !text.contains("\"version\"")) {
            return false
        }

        return text.contains("\"panel\"") ||
                text.contains("\"minClientVersion\"") ||
                Regex("\"main\"\\s*:\\s*\"index\\.js\"").containsMatchIn(text)
    }

    fun moduleContentRootHasManifest(roots: Array<VirtualFile>): VirtualFile? {
        for (root in roots) {
            val candidate = root.findChild("manifest.json")
            if (candidate != null && looksLikeManifest(candidate)) return candidate
        }
        return null
    }
}
