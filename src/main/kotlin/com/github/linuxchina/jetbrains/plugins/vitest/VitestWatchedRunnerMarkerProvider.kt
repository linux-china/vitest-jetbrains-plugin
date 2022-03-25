package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import javax.swing.Icon


class VitestWatchedRunnerMarkerProvider : VitestBaseRunLineMarkerProvider() {

    override fun getName(): String {
        return "Run Vitest test with watch"
    }

    override fun getIcon(): Icon {
        return vitestIcon
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement is JSCallExpression) {
            if (isVitestTestMethod(psiElement)) {
                val testName = psiElement.arguments[0].text.trim('\'').trim('"')
                return LineMarkerInfo(
                    psiElement,
                    psiElement.textRange,
                    icon,
                    { _: PsiElement? ->
                        "Run $testName"
                    },
                    { e, elt ->
                        runSingleVitest(psiElement, true)
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