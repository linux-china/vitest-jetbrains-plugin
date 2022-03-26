package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class VitestFileIconProvider : FileIconProvider {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        return when(file.name) {
            "vite.config.ts", "vite.config.mjs", "vite.config.js", "vite.config.cjs" -> viteIcon
            "vitest.config.ts", "vitest.config.mjs", "vitest.config.js", "vitest.config.cjs" -> vitestIcon
            else -> null
        }
    }
}
