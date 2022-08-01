package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement

class VitestRunLineMarkerContributor : RunLineMarkerContributor() {

    override fun getInfo(element: PsiElement): Info? {
        if (element is JSCallExpression) {
            val firstChild = element.firstChild
            if (firstChild != null && firstChild is JSReferenceExpression && VitestBaseRunLineMarkerProvider.vitestTestMethodNames.contains(firstChild.text)) {
                val watchAction = object : AnAction("Watch", "Run Vitest With Watch Mode", watchIcon) {
                    override fun actionPerformed(e: AnActionEvent) {
                        VitestBaseRunLineMarkerProvider.runSingleVitest(element, true)
                    }
                }
                val debugAction = object : AnAction("Debugger", "Debug Vitest test", debuggerIcon) {
                    override fun actionPerformed(e: AnActionEvent) {
                        println("action1")
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