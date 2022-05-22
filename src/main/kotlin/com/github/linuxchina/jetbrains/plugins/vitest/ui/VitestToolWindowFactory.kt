package com.github.linuxchina.jetbrains.plugins.vitest.ui

import com.github.linuxchina.jetbrains.plugins.vitest.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer


class VitestToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val jBangToolWindowPanel = JBangToolWindowPanel(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(jBangToolWindowPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class JBangToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true) {
    private val usagePanel = UsagePanel("Vitest plugin usage:\n")
    private val jbangToolWindow = vitestTree()
    var mode: String = "help"

    init {
        setContent(usagePanel)
        project.guessProjectDir()?.let { projectDir ->
            projectDir.findChild(".vitest-result.json")?.let {
                switchToScriptInfoPanel()
            }
        }
    }

    fun refreshScriptInfo(project: Project) {

    }

    fun switchToHelp() {
        setContent(usagePanel)
        this.mode = "help"
    }

    private fun switchToScriptInfoPanel() {
        setContent(jbangToolWindow)
        this.mode = "vitest"
    }

    private fun vitestTree(): JComponent {
        val vitestRestResult = project.getService(VitestService::class.java).vitestRestResult
        val vitestTreeModel = DefaultMutableTreeNode("Vitest")
        val projectDir = project.guessProjectDir()?.canonicalPath ?: ""
        if (vitestRestResult != null) {
            vitestRestResult.testResults?.forEach { testResult ->
                val testFileNode = DefaultMutableTreeNode(testFileName(projectDir, testResult.name!!))
                testResult.assertionResults?.forEach { assertionResult ->
                    testFileNode.add(DefaultMutableTreeNode(assertionResult))
                }
                vitestTreeModel.add(testFileNode)
            }
        }
        val tree = Tree(vitestTreeModel)
        tree.cellRenderer = VitestTreeCellRender()
        return JBScrollPane(tree)
    }

    private fun testFileName(projectDir: String, testFilePath: String): String {
        return if (testFilePath.startsWith(projectDir)) {
            testFilePath.substring(projectDir.length + 1)
        } else {
            testFilePath
        }
    }
}

class VitestTreeCellRender : DefaultTreeCellRenderer() {

    override fun getTreeCellRendererComponent(tree: JTree, value: Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        val node = value as DefaultMutableTreeNode
        if (leaf) {
            val assertionResult = node.userObject as AssertionResult
            if (assertionResult.isSuccess()) {
                setLeafIcon(runIcon)
            } else {
                setLeafIcon(redRunIcon)
            }
        } else if (node.isRoot) {
            setOpenIcon(vitestIcon)
            setClosedIcon(vitestIcon)
        } else {
            val fileName = node.userObject.toString()
            if (fileName.contains(".ts")) {
                setOpenIcon(tsTestIcon)
                setClosedIcon(tsTestIcon)
            } else {
                setOpenIcon(jsTestIcon)
                setClosedIcon(jsTestIcon)
            }
        }
        return super.getTreeCellRendererComponent(
            tree, value, sel, expanded, leaf, row, hasFocus
        )
    }
}

