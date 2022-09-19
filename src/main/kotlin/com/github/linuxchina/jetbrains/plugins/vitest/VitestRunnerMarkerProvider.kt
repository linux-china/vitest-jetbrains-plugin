package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import javax.swing.Icon


class VitestRunnerMarkerProvider : VitestBaseRunLineMarkerProvider() {


    override fun getName(): String {
        return "Run Vitest test"
    }

    override fun getIcon(): Icon {
        return runIcon
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement is JSCallExpression) {
            if (isVitestTestMethod(psiElement) && psiElement.arguments.isNotEmpty()) {
                val testMethod = psiElement.firstChild.text
                val testName = getTestDisplayName( psiElement.arguments[0].text)
                var tooltip = "Run $testName"
                var markIcon = runIcon
                val testedVirtualFile = psiElement.containingFile.virtualFile
                val assertionResult = findTestResult(psiElement.project, testedVirtualFile, testName)
                if (assertionResult?.isSuccess() == false) {
                    markIcon = redRunIcon
                    tooltip = assertionResult.getFailureMessage()
                } else if (testMethod.startsWith("describe")) {
                    markIcon = runRunIcon
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
                        "Run $testName by Vitest"
                    }
                )
            }
        }
        return null
    }


}