# luggage-ide-plugin

Editor extensions for [Luggage](https://luggage.dev) MUD client plugin authors. One monorepo,
two installable extensions, one shared source of truth for schema + typings.

## Layout

```
shared/                      — manifest.schema.json + luggage.d.ts (source of truth)
luggage-idea-plugin/         — JetBrains plugin (IDEA Ultimate, WebStorm, PhpStorm, PyCharm Pro, …)
luggage-vscode-extension/    — VS Code / VSCodium / Cursor extension
.github/workflows/           — CI + tag-driven release pipelines
```

Both extension builds copy the `shared/` assets into their own bundle at build time —
no symlinks, no submodules. Edit once in `shared/`, both packages pick it up next build.

## What you get on install

- `manifest.json` validation + autocomplete (JSON Schema)
- `client.*` autocomplete inside `index.js` (luggage.d.ts as a global JS library)
- **New → Luggage Plugin** (or `Luggage: New Plugin` in VS Code) — 4-file scaffold
- Live templates / snippets: `lsend`, `lon`, `lcapture`, `ltrigger`, `ltimer`, `lstorage`

Zero config. Drop into any folder with a Luggage `manifest.json` and you get autocomplete.

## Develop

### JetBrains plugin

```bash
cd luggage-idea-plugin
./gradlew runIde         # launches a sandbox IDE with the plugin installed
./gradlew buildPlugin    # produces build/distributions/luggage-idea-plugin-X.Y.Z.zip
./gradlew verifyPlugin   # runs the JetBrains plugin verifier
```

### VS Code extension

```bash
cd luggage-vscode-extension
npm install
npm run sync             # copy ../shared/ assets into schemas/ + types/
npm run build            # tsc
npm run package          # produces luggage-plugin-dev-X.Y.Z.vsix
```

Press `F5` in VS Code with this folder open to launch an Extension Development Host.

## Publish

Push a tag like `v1.0.0`; GitHub Actions builds both extensions and publishes them
with the same semantic version to:

- JetBrains Marketplace (token: `JETBRAINS_MARKETPLACE_TOKEN`)
- VS Code Marketplace (token: `VSCE_PAT`)
- Open VSX (token: `OVSX_PAT`)

Manual local publish flows are documented in each subproject's README.

## Not in this repo

Sample Luggage plugins (`xp-tracker`, `shield-timer`, `dwmud.FYI`, etc.) live in the
parent workspace — they're reference material, not part of this distributable.
