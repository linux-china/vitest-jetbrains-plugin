package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiManager

class VitestService(private val project: Project) {
    var globalServiceEnabled = false

    init {
        checkViteConfig()
    }

    private fun checkViteConfig() {
        project.guessProjectDir()?.let { projectDir ->
            projectDir.findChild("vite.config.ts")?.let {
                val vitestConfigFile = PsiManager.getInstance(project).findFile(it)!!
                val content = vitestConfigFile.text
                if (content.contains("test:") && content.contains("globals: true")) {
                    globalServiceEnabled = true
                }
            }
        }
    }
}