package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.icons.AllIcons
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
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.execution.ParametersListUtil
import javax.swing.Icon

open class VitestBaseRunLineMarkerProvider : RunLineMarkerProvider() {
    companion object {
        val vitestTestMethodNames = listOf("test", "it", "describe")
        val vitestIcon = IconLoader.getIcon("/vitest-16.png", VitestRunnerMarkerProvider::class.java)
        val runIcon = IconLoader.getIcon("/runConfigurations/testState/run.svg", AllIcons::class.java)
        val runRunIcon = IconLoader.getIcon("/runConfigurations/testState/run_run.svg", AllIcons::class.java)
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
        return VitestBaseRunLineMarkerProvider.vitestIcon
    }
}