package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.execution.Location
import com.intellij.execution.PsiLocation
import com.intellij.execution.RunManagerEx
import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.psi.PsiElement

class VitestRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        if (element is JSCallExpression) {
            val firstChild = element.firstChild
            if (firstChild != null && firstChild is JSReferenceExpression && VitestBaseRunLineMarkerProvider.vitestTestMethodNames.contains(firstChild.text.split(".")[0])) {
                val watchAction = object : AnAction("Watch", "Run Vitest with watch mode", watchIcon) {
                    override fun actionPerformed(e: AnActionEvent) {
                        VitestBaseRunLineMarkerProvider.runSingleVitest(element, true)
                    }
                }
                val debugAction = object : AnAction("Debug", "Debug Vitest test", debuggerIcon) {
                    override fun actionPerformed(e: AnActionEvent) {
                        val dataContext = SimpleDataContext.getSimpleContext(Location.DATA_KEY, PsiLocation(element), e.dataContext)
                        val context = ConfigurationContext.getFromContext(dataContext, e.place)
                        val producer = VitestTestRunConfigurationProducer()
                        val configurationSettings = producer.findOrCreateConfigurationFromContext(context)?.configurationSettings ?: return
                        (context.runManager as RunManagerEx).setTemporaryConfiguration(configurationSettings)
                        // start debug
                        ExecutionEnvironmentBuilder.create(e.project!!, DefaultDebugExecutor.getDebugExecutorInstance(), configurationSettings.configuration).buildAndExecute()
                        //ExecutionUtil.runConfiguration(configuration, Executor.EXECUTOR_EXTENSION_NAME.extensionList.first())
                    }
                }
                val actions: Array<AnAction> = arrayOf(watchAction, debugAction)
                return Info(vitestIcon, actions) {
                    "Vitest"
                }
            }
        }
        return null
    }

}