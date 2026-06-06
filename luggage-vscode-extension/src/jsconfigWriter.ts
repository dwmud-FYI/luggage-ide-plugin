import * as vscode from 'vscode';
import * as path from 'path';

/*
 * Writes (or merges) a jsconfig.json next to a Luggage manifest so VS Code's JS service picks
 * up luggage.d.ts and provides client.* autocomplete. Non-destructive — we read whatever is
 * there, add our entry, and write back.
 */
export async function ensureJsConfig(manifestUri: vscode.Uri, dtsAbsolutePath: string): Promise<void> {
    const folder = vscode.Uri.joinPath(manifestUri, '..');
    const jsconfigUri = vscode.Uri.joinPath(folder, 'jsconfig.json');

    let existing: any = {};
    try {
        const raw = await vscode.workspace.fs.readFile(jsconfigUri);
        existing = JSON.parse(Buffer.from(raw).toString('utf8'));
    } catch {
        // no file yet — fine, we'll create one
    }

    if (typeof existing !== 'object' || existing === null) existing = {};

    existing.compilerOptions = existing.compilerOptions || {};
    if (!existing.compilerOptions.target) existing.compilerOptions.target = 'ES2022';
    if (!existing.compilerOptions.module) existing.compilerOptions.module = 'CommonJS';
    if (existing.compilerOptions.checkJs === undefined) existing.compilerOptions.checkJs = false;
    if (existing.compilerOptions.allowJs === undefined) existing.compilerOptions.allowJs = true;

    const relDts = path.relative(folder.fsPath, dtsAbsolutePath).split(path.sep).join('/');

    const include = new Set<string>(Array.isArray(existing.include) ? existing.include : []);
    include.add('**/*.js');
    include.add(relDts);
    existing.include = Array.from(include);

    const json = JSON.stringify(existing, null, 2) + '\n';
    await vscode.workspace.fs.writeFile(jsconfigUri, Buffer.from(json, 'utf8'));
}
