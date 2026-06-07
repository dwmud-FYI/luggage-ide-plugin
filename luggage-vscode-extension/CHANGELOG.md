# Changelog

## 1.0.2

- Repository moved to the dwmud-FYI organization. Updated `repository`, `homepage`, and `bugs` links. No functional changes.

## 1.0.1

- Renamed displayName from "Luggage Plugin Development" to "Luggage Development" — JetBrains Marketplace rejects names containing the word "plugin", and the VS Code listing now matches for brand consistency. Functionally identical to 1.0.0.

## 1.0.0 — Initial release

- JSON Schema validation + autocomplete for Luggage `manifest.json`.
- `client.*` autocomplete in `index.js` via bundled `luggage.d.ts` and per-workspace `jsconfig.json`.
- Snippets: `lsend`, `lon`, `lcapture`, `ltrigger`, `ltimer`, `lstorage`.
- `Luggage: New Plugin` command — scaffolds the standard 4-file structure.
- Activates only inside workspaces that contain a Luggage `manifest.json`.
