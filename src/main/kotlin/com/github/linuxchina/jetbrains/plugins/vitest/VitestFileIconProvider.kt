package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class VitestFileIconProvider : FileIconProvider {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        return if (file.name == "vite.config.ts") {
            VitestBaseRunLineMarkerProvider.vitestIcon
        } else {
            null
        }
    }
}