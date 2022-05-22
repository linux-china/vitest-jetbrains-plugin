package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import javax.swing.Icon


class VitestRunnerMarkerProvider : VitestBaseRunLineMarkerProvider() {


    override fun getName(): String {
        return "Run vitest test"
    }

    override fun getIcon(): Icon {
        return runIcon
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement is JSCallExpression) {
            if (isVitestTestMethod(psiElement) && psiElement.arguments.isNotEmpty()) {
                val testMethod = psiElement.firstChild.text
                val testName = psiElement.arguments[0].text.trim {
                    it == '\'' || it == '"'
                }
                var tooltip = "Run $testName"
                var markIcon = runIcon
                if (testMethod.startsWith("describe")) {
                    markIcon = runRunIcon
                } else {
                    val testedVirtualFile = psiElement.containingFile.virtualFile
                    val assertionResult = findTestResult(psiElement.project, testedVirtualFile, testName)
                    if (assertionResult?.isSuccess() == false) {
                        markIcon = redRunIcon
                        tooltip = assertionResult.getFailureMessage()
                    }
                }
                return LineMarkerInfo(
                    psiElement,
                    psiElement.textRange,
                    markIcon,
                    { _: PsiElement? ->
                        tooltip
                    },
                    { e, elt ->
                        runSingleVitest(psiElement, false)
                    },
                    GutterIconRenderer.Alignment.CENTER,
                    {
                        "Run $testName"
                    }
                )
            }
        }
        return null
    }


}