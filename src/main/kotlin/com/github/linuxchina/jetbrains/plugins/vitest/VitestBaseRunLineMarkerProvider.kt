package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.runAnything.RunAnythingCache
import com.intellij.ide.actions.runAnything.commands.RunAnythingCommandCustomizer
import com.intellij.ide.actions.runAnything.execution.RunAnythingRunProfile
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSDestructuringElement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.execution.ParametersListUtil
import javax.swing.Icon

open class VitestBaseRunLineMarkerProvider : RunLineMarkerProvider() {
    companion object {
        val vitestTestMethodNames = listOf("test", "it", "describe")
        const val nodeBinDir = "node_modules/.bin/"
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
        val testName = arguments[0].text.trim {
            it == '\'' || it == '"'
        }
        val (binDir, command) = if (SystemInfo.isWindows) {
            "${projectDir.path.replace('/', '\\')}\\${nodeBinDir.replace('/', '\\')}" to "vitest.CMD"
        } else {
            nodeBinDir to "vitest"
        }
        val vitestCommand = if (watch) {
            "${binDir}${command} -t '${testName}' $relativePath"
        } else {
            "${binDir}${command} run -t '${testName}' $relativePath"
        }
        runCommand(
            project,
            projectDir,
            vitestCommand,
            DefaultRunExecutor.getRunExecutorInstance(),
            SimpleDataContext.getProjectContext(project)
        )
    }

    open fun runCommand(project: Project, workDirectory: VirtualFile, commandString: String, executor: Executor, dataContext: DataContext) {
        var commandDataContext = dataContext
        val commands: MutableCollection<String> = RunAnythingCache.getInstance(project).state.commands
        commands.remove(commandString)
        commands.add(commandString)
        commandDataContext = RunAnythingCommandCustomizer.customizeContext(commandDataContext)
        val initialCommandLine = GeneralCommandLine(ParametersListUtil.parse(commandString, false, true))
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workDirectory.path)
        val commandLine = RunAnythingCommandCustomizer.customizeCommandLine(commandDataContext, workDirectory, initialCommandLine)
        try {
            val generalCommandLine = if (Registry.`is`("run.anything.use.pty", false)) PtyCommandLine(commandLine) else commandLine
            val runAnythingRunProfile = RunViteProfile(generalCommandLine, commandString)
            ExecutionEnvironmentBuilder.create(project, executor, runAnythingRunProfile)
                .dataContext(commandDataContext)
                .buildAndExecute()
        } catch (e: ExecutionException) {
            Messages.showInfoMessage(project, e.message, IdeBundle.message("run.anything.console.error.title"))
        }
    }

}

class RunViteProfile(commandLine: GeneralCommandLine, originalCommand: String) : RunAnythingRunProfile(commandLine, originalCommand) {
    override fun getIcon(): Icon {
        return vitestIcon
    }

    override fun getName(): String {
        return if (originalCommand.contains("node_modules") && originalCommand.contains(".bin")) {
            originalCommand.substring(originalCommand.indexOf(".bin") + 5)
        } else {
            originalCommand
        }
    }
}
