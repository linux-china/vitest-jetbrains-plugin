package com.github.linuxchina.jetbrains.plugins.vitest

import com.github.linuxchina.jetbrains.plugins.vitest.VitestBaseRunLineMarkerProvider.Companion.getWorkingDir
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.jetbrains.nodejs.run.NodeJsRunConfiguration
import com.jetbrains.nodejs.run.NodeJsRunConfigurationType

class VitestTestRunConfigurationProducer : LazyRunConfigurationProducer<NodeJsRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory {
        return runConfigurationType<NodeJsRunConfigurationType>().configurationFactories[0]
    }

    override fun isConfigurationFromContext(configuration: NodeJsRunConfiguration, context: ConfigurationContext): Boolean {
        val location = context.location ?: return false
        val file = location.virtualFile ?: return false
        if (!file.isInLocalFileSystem) return false
        val jsCallExpression = location.psiElement
        if (jsCallExpression !is JSCallExpression) {
            return false
        }
        val applicationParameters = configuration.applicationParameters ?: return false
        val runnerName = configuration.name
        val testName = VitestBaseRunLineMarkerProvider.escapeVitestTestName(jsCallExpression)
        val filePath = applicationParameters.substring(applicationParameters.lastIndexOf(' ') + 1)
        val uniqueName = getTestRunConfigurationName(testName, location.virtualFile!!)
        return file.path.endsWith(filePath) && runnerName == uniqueName
    }

    override fun setupConfigurationFromContext(configuration: NodeJsRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
        val jsCallExpression = sourceElement.get()
        if (jsCallExpression !is JSCallExpression) {
            return false
        }
        val psiFile = jsCallExpression.containingFile
        val testedVirtualFile = psiFile.virtualFile ?: return false
        val project = psiFile.project
        val projectDir = project.guessProjectDir()!!
        val testName = VitestBaseRunLineMarkerProvider.escapeVitestTestName(jsCallExpression)
        val relativePath = VfsUtil.getRelativePath(testedVirtualFile, projectDir)!!
        configuration.name = getTestRunConfigurationName(testName, testedVirtualFile)
        configuration.workingDirectory = getWorkingDir(project, testedVirtualFile).path
        val vitestMjsPath = if (SystemInfo.isWindows) {
            "${projectDir.path}\\node_modules\\vitest\\vitest.mjs"
        } else {
            "${projectDir.path}/node_modules/vitest/vitest.mjs"
        }
        configuration.inputPath = vitestMjsPath
        var extraOptions = ""
        if (project.getService(VitestService::class.java).coverageAvailable) {
            extraOptions = "--coverage"
        }
        configuration.applicationParameters = "run --threads false $extraOptions -t \"${testName}\" $relativePath"
        return true
    }

    private fun getTestRunConfigurationName(testName: String, virtualFile: VirtualFile): String {
        return testName + "@" + virtualFile.name + " by Vitest"
    }

}