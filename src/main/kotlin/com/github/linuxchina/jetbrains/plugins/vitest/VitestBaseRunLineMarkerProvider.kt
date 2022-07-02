package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.OutputListener
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.runAnything.commands.RunAnythingCommandCustomizer
import com.intellij.ide.actions.runAnything.execution.RunAnythingRunProfile
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSDestructuringElement
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.parentOfType
import com.intellij.util.execution.ParametersListUtil
import java.io.File
import javax.swing.Icon

open class VitestBaseRunLineMarkerProvider : RunLineMarkerProvider() {

    companion object {
        val vitestTestMethodNames = listOf("test", "it", "describe")
        const val nodeBinDir = "node_modules/.bin/"
        val testResults = mutableMapOf<String, AssertionResult>()
    }

    fun isVitestTestMethod(jsCallExpression: JSCallExpression): Boolean {
        val firstChild = jsCallExpression.firstChild
        if (firstChild != null && firstChild is JSReferenceExpression && vitestTestMethodNames.contains(firstChild.text)) {
            val project = jsCallExpression.project
            if (project.getService(VitestService::class.java).globalServiceEnabled) {
                return true
            }
            val resolvedElement = firstChild.resolve()
            if (resolvedElement != null) {
                val filePath = resolvedElement.containingFile.virtualFile.toString()
                if (filePath.contains("/vitest/") || filePath.contains("\\vitest\\")) {
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
        val testedVirtualFile = jsCallExpression.containingFile.virtualFile
        var workDir = project.guessProjectDir()!!
        // sub project support #5
        val packageJson = PackageJsonUtil.findUpPackageJson(testedVirtualFile)
        if (packageJson != null) {
            val packageJsonDir = packageJson.parent
            if (packageJsonDir != workDir) {
                val vitestBin = packageJsonDir.findFileByRelativePath("${nodeBinDir}vitest")
                if (vitestBin != null) {
                    workDir = packageJsonDir
                }
            }
        }
        val relativePath = VfsUtil.getRelativePath(testedVirtualFile, workDir)!!
        val testName = arguments[0].text.trim {
            it == '\'' || it == '"'
        }
        val isWSL = workDir.path.contains("wsl$")
        val (binDir, command) = if (SystemInfo.isWindows && !isWSL) {
            "${workDir.path.replace('/', '\\')}\\${nodeBinDir.replace('/', '\\')}" to "vitest.CMD"
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
            workDir,
            testedVirtualFile,
            relativePath,
            testName,
            watch,
            vitestCommand,
            DefaultRunExecutor.getRunExecutorInstance(),
            SimpleDataContext.getProjectContext(project)
        )
    }

    open fun runCommand(
        project: Project,
        workDirectory: VirtualFile,
        testedVirtualFile: VirtualFile,
        relativePath: String,
        testName: String,
        watch: Boolean,
        commandString: String,
        executor: Executor,
        dataContext: DataContext
    ) {
        var commandDataContext = dataContext
        commandDataContext = RunAnythingCommandCustomizer.customizeContext(commandDataContext)
        val initialCommandLine = GeneralCommandLine(ParametersListUtil.parse(commandString, false, true))
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            .withWorkDirectory(workDirectory.path)
        val commandLine = RunAnythingCommandCustomizer.customizeCommandLine(commandDataContext, workDirectory, initialCommandLine)
        // use configured nodejs interpreter
        val nodeJsInterpreter = NodeJsInterpreterManager.getInstance(project).interpreter
        if (nodeJsInterpreter != null) {
            val environment = commandLine.environment
            val nodePath = nodeJsInterpreter.referenceName
            val nodeBinDir = nodePath.substring(0, nodePath.lastIndexOfAny(charArrayOf('/', '\\')))
            environment["PATH"] = nodeBinDir + File.pathSeparator + environment["PATH"]
        }
        val testUniqueName = getTestUniqueName(testedVirtualFile, testName)
        try {
            val generalCommandLine = if (Registry.`is`("run.anything.use.pty", false)) PtyCommandLine(commandLine) else commandLine
            val runAnythingRunProfile = RunViteProfile(generalCommandLine, commandString)
            if (watch) { // watch mode
                val environment = ExecutionEnvironmentBuilder.create(project, executor, runAnythingRunProfile)
                    .dataContext(commandDataContext)
                    .build()
                environment.runner.execute(environment) {
                    it.processHandler!!.addProcessListener(object : ProcessAdapter() {
                        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                            if (event.text.startsWith("Test Files ")) {
                                val succeeded = !event.text.contains("failed")
                                processTestResult(succeeded, testUniqueName, testName, project, testedVirtualFile)
                            }
                        }
                    })
                }
            } else {
                val environment = ExecutionEnvironmentBuilder.create(project, executor, runAnythingRunProfile)
                    .dataContext(commandDataContext)
                    .build()
                environment.runner.execute(environment) {
                    it.processHandler!!.addProcessListener(object : OutputListener(StringBuilder(), StringBuilder()) {
                        override fun processTerminated(event: ProcessEvent) {
                            super.processTerminated(event)
                            val succeeded = event.exitCode == 0
                            processTestResult(succeeded, testUniqueName, testName, project, testedVirtualFile)
                        }
                    })
                }
            }
        } catch (e: ExecutionException) {
            Messages.showInfoMessage(project, e.message, IdeBundle.message("run.anything.console.error.title"))
        }
    }

    private fun processTestResult(succeeded: Boolean, testUniqueName: String, testName: String, project: Project, testedVirtualFile: VirtualFile) {
        val testStatus = if (succeeded) "passed" else "failed"
        val assertionResult = AssertionResult().apply {
            startTime = System.currentTimeMillis()
            fullName = testUniqueName
            title = testName
            status = testStatus
        }
        val previousAssertionResult = findTestResult(project, testedVirtualFile, testName)
        var refreshRequired = false
        if (previousAssertionResult == null) {
            if (!succeeded) {
                refreshRequired = true;
            }
        } else {
            if (previousAssertionResult.isSuccess() != succeeded) {
                refreshRequired = true
            }
        }
        testResults[testUniqueName] = assertionResult
        if (refreshRequired) { //refresh required
            ApplicationManager.getApplication().runReadAction {
                PsiManager.getInstance(project).findFile(testedVirtualFile)?.let { psiFile ->
                    DaemonCodeAnalyzer.getInstance(project).restart(psiFile)
                }
            }
        }
    }

    fun findTestResult(project: Project, testedVirtualFile: VirtualFile, testName: String): AssertionResult? {
        val testUniqueName = getTestUniqueName(testedVirtualFile, testName)
        val testedFilePath = testedVirtualFile.path
        var assertionResult = testResults[testUniqueName]
        if (assertionResult == null) {
            assertionResult = project.getService(VitestService::class.java).findTestResult(testedFilePath, testName)
        } else {
            val assertionResult2 = project.getService(VitestService::class.java).findTestResult(testedFilePath, testName)
            if (assertionResult2?.startTime != null) {
                if (assertionResult2.startTime!! > assertionResult.startTime!!) {
                    assertionResult = assertionResult2
                }
            }
        }
        return assertionResult;
    }

    private fun getTestUniqueName(testedVirtualFile: VirtualFile, testName: String): String {
        return "${testedVirtualFile.path}?${testName}"
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
