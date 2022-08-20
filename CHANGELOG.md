<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# vitest-jetbrains-plugin Changelog

## [Unreleased]

## [0.6.3]

### Fixed

- Add Node v14 support by `npx vitest`

## [0.6.2]

### Fixed

- Add `--coverage` option for debug if  `@vitest/coverage-c8` or vitest/coverage-istanbul` dependency declared in package.json
- Fix test runner with WSL

## [0.6.1]

### Fixed

- Add `--coverage` option for debug if `c8` dependency declared in package.json
- Adjust working directory: root directory for workspaces, and proximate `package.json` directory for tested file.

## [0.6.0]

### Added

- Adjust Vitest line mark icons: green icon to run test, and Vitest icon to debug or run by watch mode
- Add debug support to run Vitest tests

## [0.5.1]

### Fixed

- Add `.cmd` ext name for `npm` and `yarn` commands on Windows

## [0.5.0]

### Added

- File icon for vite.config.cts and vite.config.mts
- Use configured nodejs interpreter to run tests
- Use `npm exec --` or `npm exec --` to run vitest: avoid OS detection, color output
- Quotation mark escape for test name

## [0.4.1]

### Added

- Support vite.config.ts and vitest.config.ts
- Bug fix test file without test methods for Vitest toolWindow

## [0.4.0]

### Added

- Test failure auto detection
- Vitest toolWindow
- `.vitest-result.json` json reporter file support

## [0.2.1]

### Added

- Split vite and vitest configuration files
- Vite png icon (16x16) optimized

## [0.2.0]

### Added

- Run Vitest tests
