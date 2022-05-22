package com.github.linuxchina.jetbrains.plugins.vitest.ui

import com.github.linuxchina.jetbrains.plugins.vitest.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel


class VitestToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val vitestToolWindowPanel = VitestToolWindowPanel(project)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(vitestToolWindowPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}

class VitestToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true) {
    private val usagePanel = UsagePanel("Vitest plugin usage:\n")
    private val vitestTreeModel = DefaultMutableTreeNode("Vitest")
    private var vitestTree: Tree? = null
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

    fun refreshVitestTree(project: Project, vitestRestResult: VitestTestResult) {
        val projectDir = project.guessProjectDir()?.canonicalPath ?: ""
        ApplicationManager.getApplication().invokeLater {
            vitestTreeModel.removeAllChildren()
            vitestRestResult.testResults?.forEach { testResult ->
                val testFileNode = DefaultMutableTreeNode(testFileName(projectDir, testResult.name!!))
                testResult.assertionResults?.forEach { assertionResult ->
                    testFileNode.add(DefaultMutableTreeNode(assertionResult))
                }
                vitestTreeModel.add(testFileNode)
            }
            (vitestTree!!.model as DefaultTreeModel).reload()
            expandVitestTree()
        }
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
        vitestTree = Tree(DefaultTreeModel(vitestTreeModel))
        vitestTree!!.cellRenderer = VitestTreeCellRender()
        expandVitestTree()
        vitestTree!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val node = vitestTree!!.lastSelectedPathComponent as DefaultMutableTreeNode
                    if (node.isLeaf) {
                        val assertionResult = node.userObject as AssertionResult
                        val testName = assertionResult.title
                        val filePath = (node.parent as DefaultMutableTreeNode).userObject.toString()
                        project.guessProjectDir()?.let {
                            it.findFileByRelativePath(filePath)?.let { testedFile ->
                                val psiFile = PsiManager.getInstance(project).findFile(testedFile)
                                val lineNum = psiFile!!.text.lines().indexOfFirst { line ->
                                    line.contains("'${testName}'") || line.contains("\"${testName}\"")
                                }
                                val fileEditorManager = FileEditorManager.getInstance(project)
                                fileEditorManager.openTextEditor(OpenFileDescriptor(project, testedFile, lineNum, 0), true)
                            }
                        }
                    }
                }
            }
        })
        return JBScrollPane(vitestTree)
    }

    private fun expandVitestTree() {
        for (i in 0 until vitestTree!!.rowCount) {
            vitestTree!!.expandRow(i)
        }
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

