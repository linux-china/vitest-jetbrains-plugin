# vitest-jetbrains-plugin

<!-- Plugin description -->
A simple WebStorm plugin to run Vitest tests.

* Green Run icon means to run once only
* Vitest Run icon means to run with watch mode
* Test failure detection to remark run icon as red
* Vitest json reporter integration by `.vitest-result.json` file

```
  "scripts": {
    "test": "vitest --watch",
    "webstorm-integration": "vitest --watch --reporter=dot --reporter=json --outputFile=.vitest-result.json",
  },
```

* Vitest toolWindow to display test statistics from `.vitest-result.json` file

Please install [Awesome Console](https://plugins.jetbrains.com/plugin/7677-awesome-console) for code link/navigation from console.

<!-- Plugin description end -->

# Screenshot

![Vitest](screenshot.png)

# Attention

This plugin just a temp solution before official Vitest support from WebStorm.
I think JetBrains people will do this job, and they know Vitest is great framework.
For Vitest support in WebStorm, please vote here: https://youtrack.jetbrains.com/issue/WEB-54437

# Vitest global support

Please enable `globals: true` for test in `vite.config.ts`: not `vitest.config.ts`

```typescript
// vite.config.ts
import {defineConfig} from 'vitest/config'

export default defineConfig({
    test: {
        globals: true,
    },
})
```

**Attention**: Please reload(close/open) project if you enable `globals: true` first time.

# How to debug Vitest tests?

Now Vitest plugin doesn't support Vitest test debug, but you can debug Vitest tests by running npm scripts with debug mode in WebStorm.

```
  "scripts": {
    "debug-demo": "vitest -t 'demo' test/demo.test.ts",
    "webstorm-integration": "vitest --watch --reporter=dot --reporter=json --outputFile=.vitest-result.json",
  },
```

You can run `webstorm-integration` from WebStorm with debug mode, then you can debug all test methods.

Or you can use standard `Node.js` app run configuration to debug test file, please refer https://vitest.dev/guide/debugging.html#intellij-idea

# Installation

- Using IDE built-in plugin system: https://plugins.jetbrains.com/plugin/19220-vitest-runner

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "vitest"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/linux-china/vitest-jetbrains-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

# References

* Vitest VS Code plugin: https://marketplace.visualstudio.com/items?itemName=ZixuanChen.vitest-explorer
* Awesome Console plugin: https://plugins.jetbrains.com/plugin/7677-awesome-console
* Building a Plugin for WebStorm – Tutorial for JavaScript Developers: https://blog.jetbrains.com/webstorm/2021/09/building-a-plugin-for-webstorm-part-1/