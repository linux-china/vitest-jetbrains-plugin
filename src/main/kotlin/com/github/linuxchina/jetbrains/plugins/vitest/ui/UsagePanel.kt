package com.github.linuxchina.jetbrains.plugins.vitest.ui

import javax.swing.JTextArea

class UsagePanel constructor(prefix: String) : JTextArea() {
    private val vitestPluginUsage = """
Setup: 
Add following command into `"webstorm-integration": "vitest --watch --reporter=verbose --reporter=json --outputFile=.vitest-result.json",` package.json scripts

Features:
- Green Run icon means to run once only
- Vitest Run icon means to run with watch mode
- Failure detection to run test method
- Vitest json reporter integration with Vitest run icons by `.vitest-result.json` file

"""

    init {
        isOpaque = false
        text = prefix + vitestPluginUsage
        isEditable = false
    }
}
