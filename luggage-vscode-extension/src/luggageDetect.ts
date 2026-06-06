import * as vscode from 'vscode';

/*
 * Shape-based "is this a Luggage manifest" check. Mirrors the IDEA-side detector — we don't
 * want to claim every random manifest.json in a workspace.
 */
export async function looksLikeLuggageManifest(uri: vscode.Uri): Promise<boolean> {
    try {
        const bytes = await vscode.workspace.fs.readFile(uri);
        const text = Buffer.from(bytes).toString('utf8');
        const json = JSON.parse(text);

        if (typeof json !== 'object' || json === null) return false;
        if (typeof json.id !== 'string') return false;
        if (typeof json.name !== 'string') return false;
        if (typeof json.version !== 'string') return false;

        if (typeof json.panel === 'object' && json.panel !== null) return true;
        if (typeof json.minClientVersion === 'string') return true;
        if (json.main === 'index.js') return true;

        return false;
    } catch {
        return false;
    }
}

export async function findLuggageManifests(): Promise<vscode.Uri[]> {
    const found = await vscode.workspace.findFiles('**/manifest.json', '**/node_modules/**', 100);
    const out: vscode.Uri[] = [];
    for (const uri of found) {
        if (await looksLikeLuggageManifest(uri)) out.push(uri);
    }
    return out;
}
