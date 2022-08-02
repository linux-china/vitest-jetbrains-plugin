# vitest-jetbrains-plugin

<!-- Plugin description -->
A simple WebStorm plugin to run Vitest tests.

* Green Run icon means to run once only
* Vitest Run icon to debug test or run test with watch mode
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

Please enable `globals: true` for `test` in Vitest configuration file.

```typescript
// vite.config.ts
import {defineConfig} from 'vitest/config'

export default defineConfig({
    test: {
        globals: true,
    },
})
```
          
**Exclude jest**: jest is removed from the devDependencies, anyway, jest still resolved and installed by co dependency during `npm install`. 
You can use following solution to exclude jest: 

```
"scripts": {
    "postinstall": "rm -rf node_modules/jest*; rm -rf node_modules/@jest"
}
```

**Attention**: Please reload(close/open) project if you enable `globals: true` first time.

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