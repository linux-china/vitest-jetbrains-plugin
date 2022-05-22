package com.github.linuxchina.jetbrains.plugins.vitest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiManager


class VitestService(private val project: Project) {
    var globalServiceEnabled = false
    private val objectMapper = ObjectMapper()
    var vitestRestResult: VitestTestResult? = null

    init {
        checkViteConfig()
        reloadVitestJsonResultFile()
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
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
            projectDir.findChild("vite.config.ts")?.let {
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
            projectDir.findChild(".vitest-result.json")?.let {
                val refreshRequired = vitestRestResult != null
                vitestRestResult = objectMapper.readValue<VitestTestResult>(it.toNioPath().toFile())
                if (refreshRequired) {
                    ApplicationManager.getApplication().runReadAction {
                        DaemonCodeAnalyzer.getInstance(project).restart()
                    }
                }
            }
        }
    }

    fun findTestResult(filePath: String, testName: String): AssertionResult? {
        return vitestRestResult?.findTestResult(filePath, testName)
    }
}