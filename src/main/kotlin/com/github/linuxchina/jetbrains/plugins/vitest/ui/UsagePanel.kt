package com.github.linuxchina.jetbrains.plugins.vitest.ui

import javax.swing.JTextArea

class UsagePanel constructor(prefix: String) : JTextArea() {
    private val vitestPluginUsage = """
Setup: 

- Add following command into package.json scripts

```
"scripts": {
 "webstorm-integration": "vitest --watch --reporter=dot --reporter=json --outputFile=.vitest-result.json",
}
```

Features:
- Green Run icon means to run once only
- Vitest Run icon means to run with watch mode
- Test failure detection to gutter
- Test failure detection to gutter
- Vitest json reporter integration with Vitest run icons by `.vitest-result.json` file

"""

    init {
        isOpaque = false
        text = prefix + vitestPluginUsage
        isEditable = false
    }
}
