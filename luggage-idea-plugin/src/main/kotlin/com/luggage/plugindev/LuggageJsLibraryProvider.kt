package com.luggage.plugindev

import com.intellij.lang.javascript.library.JSPredefinedLibraryProvider
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.webcore.libraries.ScriptingLibraryModel
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/*
 * Bundles luggage.d.ts as a global JS library inside any project that contains a Luggage
 * manifest.json. The d.ts is shipped inside the plugin jar, but the JS plugin's library
 * machinery wants a real VirtualFile on disk — so we extract once into the plugin config dir
 * and hand back that file.
 */
class LuggageJsLibraryProvider : JSPredefinedLibraryProvider() {

    private val log = Logger.getInstance(LuggageJsLibraryProvider::class.java)

    override fun getPredefinedLibraries(project: Project): Array<ScriptingLibraryModel> {
        if (!projectHasLuggageManifest(project)) return emptyArray()

        val file = extractedDtsFile() ?: return emptyArray()

        val model = ScriptingLibraryModel.createPredefinedLibrary(
            "Luggage Plugin API",
            arrayOf(file),
            true
        )
        return arrayOf(model)
    }

    private fun projectHasLuggageManifest(project: Project): Boolean {
        val roots = ProjectRootManager.getInstance(project).contentRoots
        if (LuggageManifestDetector.moduleContentRootHasManifest(roots) != null) return true

        // Also walk one level down — many Luggage authors keep multiple sample plugins
        // as sibling folders under one workspace.
        for (root in roots) {
            val children = root.children ?: continue
            for (child in children) {
                if (!child.isDirectory) continue
                val manifest = child.findChild("manifest.json") ?: continue
                if (LuggageManifestDetector.looksLikeManifest(manifest)) return true
            }
        }
        return false
    }

    private fun extractedDtsFile(): VirtualFile? {
        val url = javaClass.getResource(RESOURCE_PATH) ?: run {
            log.warn("luggage.d.ts not bundled at $RESOURCE_PATH")
            return null
        }
        val bytes = url.openStream().use { it.readBytes() }
        val digest = sha1(bytes)

        val outDir = File(PathManager.getSystemPath(), "luggage-plugindev")
        if (!outDir.exists()) outDir.mkdirs()

        val target = File(outDir, "luggage-$digest.d.ts")
        if (!target.exists() || target.length() != bytes.size.toLong()) {
            FileOutputStream(target).use { it.write(bytes) }
        }

        val lfs = LocalFileSystem.getInstance()
        val vf = lfs.refreshAndFindFileByIoFile(target)
        if (vf == null) {
            log.warn("could not materialize ${target.path} as VirtualFile")
        }
        return vf
    }

    private fun sha1(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-1")
        val out = md.digest(bytes)
        return out.joinToString("") { "%02x".format(it) }.take(12)
    }

    companion object {
        private const val RESOURCE_PATH = "/types/luggage.d.ts"
    }
}
