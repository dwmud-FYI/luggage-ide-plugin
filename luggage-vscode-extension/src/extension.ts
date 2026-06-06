import * as vscode from 'vscode';
import { findLuggageManifests } from './luggageDetect';
import { ensureJsConfig } from './jsconfigWriter';
import { registerNewPluginCommand } from './commands/newPlugin';

const DECISION_KEY = 'luggage.jsconfigDecision';

interface PerFolderDecision {
    [folder: string]: 'yes' | 'no';
}

export async function activate(context: vscode.ExtensionContext) {
    registerNewPluginCommand(context);

    const dtsAbs = context.asAbsolutePath('types/luggage.d.ts');
    await offerJsConfigForOpenWorkspace(context, dtsAbs);

    // re-check when the workspace changes (folder added/removed)
    context.subscriptions.push(
        vscode.workspace.onDidChangeWorkspaceFolders(async () => {
            await offerJsConfigForOpenWorkspace(context, dtsAbs);
        })
    );
}

async function offerJsConfigForOpenWorkspace(
    context: vscode.ExtensionContext,
    dtsAbs: string,
): Promise<void> {
    const manifests = await findLuggageManifests();
    if (manifests.length === 0) return;

    const decisions = context.globalState.get<PerFolderDecision>(DECISION_KEY, {});
    let mutated = false;

    for (const manifest of manifests) {
        const folderKey = vscode.Uri.joinPath(manifest, '..').fsPath;
        const decision = decisions[folderKey];

        if (decision === 'no') continue;
        if (decision === 'yes') {
            await ensureJsConfig(manifest, dtsAbs);
            continue;
        }

        const pick = await vscode.window.showInformationMessage(
            `Luggage plugin detected in ${shortLabel(folderKey)}. ` +
            `Add a jsconfig.json so client.* autocomplete works?`,
            { modal: false },
            'Yes', 'Not now', "Don't ask again",
        );

        if (pick === 'Yes') {
            await ensureJsConfig(manifest, dtsAbs);
            decisions[folderKey] = 'yes';
            mutated = true;
        } else if (pick === "Don't ask again") {
            decisions[folderKey] = 'no';
            mutated = true;
        }
    }

    if (mutated) {
        await context.globalState.update(DECISION_KEY, decisions);
    }
}

function shortLabel(p: string): string {
    const parts = p.split(/[\\/]/);
    return parts.slice(-2).join('/') || p;
}

export function deactivate() {}
