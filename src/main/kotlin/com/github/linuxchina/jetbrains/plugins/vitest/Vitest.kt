package com.github.linuxchina.jetbrains.plugins.vitest

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader

val viteIcon = IconLoader.getIcon("/vite-16.png", VitestRunnerMarkerProvider::class.java)
val vitestIcon = IconLoader.getIcon("/vitest-16.png", VitestRunnerMarkerProvider::class.java)
val runIcon = IconLoader.getIcon("/runConfigurations/testState/run.svg", AllIcons::class.java)
val redRunIcon = IconLoader.getIcon("/runConfigurations/testState/red2.svg", AllIcons::class.java)
val runRunIcon = IconLoader.getIcon("/runConfigurations/testState/run_run.svg", AllIcons::class.java)
val watchIcon = AllIcons.Debugger.Watch
val debuggerIcon = AllIcons.Toolwindows.ToolWindowDebugger
val jsTestIcon = icons.JavaScriptLanguageIcons.FileTypes.JsTestFile
val tsTestIcon = icons.JavaScriptLanguageIcons.FileTypes.TypeScriptTest
val testError = AllIcons.RunConfigurations.TestError
val testPassed = AllIcons.RunConfigurations.TestPassed
