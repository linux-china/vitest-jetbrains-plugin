package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.runConfigurationType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
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
        val jsCallExpression = context.location!!.psiElement
        if (jsCallExpression !is JSCallExpression) {
            return false
        }
        val applicationParameters = configuration.applicationParameters!!
        val runnerName = configuration.name
        val testName = VitestBaseRunLineMarkerProvider.getVitestTestName(jsCallExpression)
        val fileName = location.virtualFile!!.name
        val filePath = applicationParameters.substring(applicationParameters.lastIndexOf(' ') + 1)
        val uniqueName = "${testName}@${fileName}"
        return file.path.endsWith(filePath) && runnerName.contains(uniqueName)
    }

    override fun setupConfigurationFromContext(configuration: NodeJsRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
        val jsCallExpression = sourceElement.get()
        if (jsCallExpression !is JSCallExpression) {
            return false
        }
        val psiFile = jsCallExpression.containingFile
        val virtualFile = psiFile.virtualFile ?: return false
        val project = psiFile.project
        val projectDir = project.guessProjectDir()!!
        val testName = VitestBaseRunLineMarkerProvider.getVitestTestName(jsCallExpression)
        val relativePath = VfsUtil.getRelativePath(virtualFile, projectDir)!!
        configuration.name = testName + "@" + virtualFile.name + " by Vitest"
        configuration.workingDirectory = projectDir.path
        val vitestMjsPath = if (SystemInfo.isWindows) {
            ".\\node_modules\\vitest\\vitest.mjs"
        } else {
            "./node_modules/vitest/vitest.mjs"
        }
        configuration.inputPath = vitestMjsPath
        configuration.applicationParameters = "run -t '${testName}' $relativePath"
        return true
    }


}