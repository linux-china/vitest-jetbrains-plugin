package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandProvider
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSDestructuringElement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.util.parentOfType

open class VitestBaseRunLineMarkerProvider : RunLineMarkerProvider() {
    companion object {
        val vitestTestMethodNames = listOf("test", "it", "describe")
        val vitestIcon = IconLoader.getIcon("/vitest-16.png", VitestRunnerMarkerProvider::class.java)
        val runIcon = IconLoader.getIcon("/runConfigurations/testState/run.svg", AllIcons::class.java)
    }

    fun isVitestTestMethod(jsCallExpression: JSCallExpression): Boolean {
        val firstChild = jsCallExpression.firstChild
        if (firstChild != null && firstChild is JSReferenceExpression && vitestTestMethodNames.contains(firstChild.text)) {
            val resolvedElement = firstChild.resolve()
            if (resolvedElement != null) {
                val filePath = resolvedElement.containingFile.virtualFile.toString()
                if (filePath.contains("/vitest/")) {
                    return true
                } else {
                    val declareBlock = resolvedElement.parentOfType<JSDestructuringElement>()
                    if (declareBlock != null && declareBlock.text.contains("import.meta.vitest")) {
                        return true
                    }
                }

            }
        }
        return false
    }


    fun runSingleVitest(jsCallExpression: JSCallExpression, watch: Boolean) {
        val arguments = jsCallExpression.arguments
        val project = jsCallExpression.project
        val projectDir = project.guessProjectDir()!!
        val relativePath = VfsUtil.getRelativePath(jsCallExpression.containingFile.virtualFile, projectDir)
        val testName = arguments[0].text.trim('\'').trim('"')
        val vitestCommand = if (watch) {
            "node_modules/.bin/vitest -t '${testName}' $relativePath"
        } else {
            "node_modules/.bin/vitest run -t '${testName}' $relativePath"
        }
        RunAnythingCommandProvider.runCommand(
            project.guessProjectDir()!!,
            vitestCommand,
            DefaultRunExecutor.getRunExecutorInstance(),
            SimpleDataContext.getProjectContext(project)
        )
    }


}