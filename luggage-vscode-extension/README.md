# Luggage Plugin Development

Zero-config tooling for [Luggage](https://luggage.dev) MUD client plugin authors.

Drop into any folder with a Luggage `manifest.json` and you immediately get:

- **manifest.json** validation + autocomplete (JSON Schema)
- **client.\*** autocomplete inside `index.js` (bundled `luggage.d.ts`)
- **Luggage: New Plugin** command (palette + folder right-click) — scaffolds the standard 4-file structure
- Snippets: `lsend`, `lon`, `lcapture`, `ltrigger`, `ltimer`, `lstorage`

The extension only activates inside workspaces that actually contain a Luggage manifest — unrelated JS projects stay untouched.

## Install

From the VS Code Marketplace, VSCodium / Cursor / Windsurf via Open VSX, or sideload a `.vsix`:

```bash
code --install-extension luggage-plugin-dev-1.0.0.vsix
```

## Develop

```bash
npm install
npm run sync          # copy ../shared/{schema,types} into the bundle
npm run build         # tsc
npm run package       # produces luggage-plugin-dev-X.Y.Z.vsix
```

Press `F5` with this folder open in VS Code to launch an Extension Development Host.

## Publish

```bash
npm run sync
npm run build
vsce publish -p $VSCE_PAT          # VS Code Marketplace
ovsx publish -p $OVSX_PAT          # Open VSX (VSCodium / Cursor / Windsurf)
```

The shared schema + `.d.ts` live in `../shared/`; `scripts/sync-shared.js` copies them into
`schemas/` and `types/` before each build. Don't edit the copies — edit the originals.
