package com.github.linuxchina.jetbrains.plugins.vitest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.linuxchina.jetbrains.plugins.vitest.ui.VitestToolWindowPanel
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiManager


class VitestService(private val project: Project) {
    var globalServiceEnabled = false
    private val objectMapper = ObjectMapper()
    var vitestRestResult: VitestTestResult? = null

    companion object {
        val vitestConfigFileNames = listOf("vite.config.ts", "vite.config.js", "vite.config.mjs", "vitest.config.ts", "vitest.config.js", "vitest.config.mjs")
    }

    init {
        checkViteConfig()
        reloadVitestJsonResultFile()
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                events.forEach { vFileEvent ->
                    val changedFile = vFileEvent.file
                    if (changedFile != null) {
                        if (changedFile.name == ".vitest-result.json") {
                            if (ProjectFileIndex.getInstance(project).isInContent(changedFile)) {
                                reloadVitestJsonResultFile()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun checkViteConfig() {
        project.guessProjectDir()?.let { projectDir ->
            var viteConfig: VirtualFile? = null
            for (configFileName in vitestConfigFileNames) {
                viteConfig = projectDir.findChild(configFileName)
                if (viteConfig != null) {
                    break
                }
            }
            viteConfig?.let {
                val vitestConfigFile = PsiManager.getInstance(project).findFile(it)!!
                val content = vitestConfigFile.text
                if (content.contains("test:") && content.contains("globals: true")) {
                    globalServiceEnabled = true
                }
            }
        }
    }

    private fun reloadVitestJsonResultFile() {
        project.guessProjectDir()?.let { projectDir ->
            projectDir.findChild(".vitest-result.json")?.let { vitestResultJsonFile ->
                val refreshRequired = vitestRestResult != null
                vitestRestResult = objectMapper.readValue<VitestTestResult>(vitestResultJsonFile.toNioPath().toFile())
                if (refreshRequired) {
                    getVitestToolWindowPanel(project)?.refreshVitestTree(project, vitestRestResult!!)
                    ApplicationManager.getApplication().runReadAction {
                        val daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project)
                        val psiManager = PsiManager.getInstance(project)
                        // restart opened files
                        FileEditorManager.getInstance(project).openFiles.filter {
                            it.name.contains(".ts") || it.name.contains(".js")
                        }.forEach {
                            psiManager.findFile(it)?.let { psiFile ->
                                daemonCodeAnalyzer.restart(psiFile)
                            }
                        }
                    }
                }
            }
        }
    }

    fun findTestResult(filePath: String, testName: String): AssertionResult? {
        return vitestRestResult?.findTestResult(filePath, testName)
    }

    private fun getVitestToolWindowPanel(project: Project): VitestToolWindowPanel? {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Vitest")
        if (toolWindow != null) {
            val content = toolWindow.contentManager.contents.first { it.component is VitestToolWindowPanel }
            return content.component as VitestToolWindowPanel
        }
        return null
    }
}