#!/usr/bin/env node
// Copy ../shared/schema and ../shared/types into the extension bundle so the source
// of truth stays in one place. Runs before build + before vsce package via prepublish.

const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const shared = path.resolve(root, '..', 'shared');

const pairs = [
    [path.join(shared, 'schema', 'manifest.schema.json'), path.join(root, 'schemas', 'manifest.schema.json')],
    [path.join(shared, 'types',  'luggage.d.ts'),         path.join(root, 'types',   'luggage.d.ts')],
];

for (const [src, dst] of pairs) {
    if (!fs.existsSync(src)) {
        console.error(`sync-shared: missing source file ${src}`);
        process.exit(1);
    }
    fs.mkdirSync(path.dirname(dst), { recursive: true });
    fs.copyFileSync(src, dst);
    console.log(`sync-shared: ${path.relative(root, src)} → ${path.relative(root, dst)}`);
}
