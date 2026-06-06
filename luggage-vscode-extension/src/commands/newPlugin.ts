import * as vscode from 'vscode';
import * as path from 'path';

export function registerNewPluginCommand(context: vscode.ExtensionContext) {
    const disposable = vscode.commands.registerCommand('luggage.newPlugin', async (folderArg?: vscode.Uri) => {
        await runNewPluginFlow(folderArg, context);
    });
    context.subscriptions.push(disposable);
}

async function runNewPluginFlow(folderArg: vscode.Uri | undefined, context: vscode.ExtensionContext) {
    const id = await vscode.window.showInputBox({
        prompt: 'Plugin id (lowercase, dashes ok)',
        placeHolder: 'my-plugin',
        validateInput: v => /^[a-z][a-z0-9-]*$/.test(v) ? null : 'lowercase letters / digits / dashes; must start with a letter',
    });
    if (!id) return;

    const name = await vscode.window.showInputBox({
        prompt: 'Plugin display name',
        value: titleCase(id),
    });
    if (!name) return;

    const author = await vscode.window.showInputBox({
        prompt: 'Author',
        value: process.env.USER || process.env.USERNAME || '',
    });
    if (author === undefined) return;   // cancelled — empty string is OK

    let parent: vscode.Uri | undefined = folderArg;
    if (!parent) {
        const picked = await vscode.window.showOpenDialog({
            canSelectFiles: false,
            canSelectFolders: true,
            canSelectMany: false,
            openLabel: 'Create plugin here',
        });
        if (!picked || picked.length === 0) return;
        parent = picked[0];
    }

    const target = vscode.Uri.joinPath(parent, id);
    try {
        await vscode.workspace.fs.createDirectory(target);
    } catch (e) {
        vscode.window.showErrorMessage(`Could not create ${target.fsPath}: ${e}`);
        return;
    }

    const files: Array<[string, string]> = [
        ['manifest.json', manifestTemplate(id, name, author)],
        ['panel.html',    panelHtmlTemplate(id, name)],
        ['panel.css',     panelCssTemplate(id)],
        ['index.js',      indexJsTemplate(id, name)],
    ];

    for (const [filename, body] of files) {
        const uri = vscode.Uri.joinPath(target, filename);
        await vscode.workspace.fs.writeFile(uri, Buffer.from(body, 'utf8'));
    }

    vscode.window.showInformationMessage(`Created Luggage plugin '${id}'.`);

    const manifestUri = vscode.Uri.joinPath(target, 'manifest.json');
    await vscode.window.showTextDocument(manifestUri);

    // Drop a jsconfig so client.* autocomplete works without a separate prompt.
    const dtsAbs = context.asAbsolutePath('types/luggage.d.ts');
    const relDts = path.relative(target.fsPath, dtsAbs).split(path.sep).join('/');
    const jsconfig = {
        compilerOptions: { target: 'ES2022', module: 'CommonJS', allowJs: true, checkJs: false },
        include: ['**/*.js', relDts],
    };
    await vscode.workspace.fs.writeFile(
        vscode.Uri.joinPath(target, 'jsconfig.json'),
        Buffer.from(JSON.stringify(jsconfig, null, 2) + '\n', 'utf8'),
    );
}

function titleCase(id: string): string {
    return id.split('-').map(s => s.charAt(0).toUpperCase() + s.slice(1)).join(' ');
}

function manifestTemplate(id: string, name: string, author: string): string {
    return JSON.stringify({
        id,
        name,
        version: '1.0.0',
        description: '',
        author,
        main: 'index.js',
        panel: { html: 'panel.html', css: 'panel.css', title: name },
        minClientVersion: '1.10.2',
    }, null, 2) + '\n';
}

function panelHtmlTemplate(id: string, _name: string): string {
    return `<div class="${id}-panel" data-plugin="${id}">
  <div class="${id}-field">
    <label class="${id}-label" for="${id}-input">Input</label>
    <input class="${id}-input" id="${id}-input" type="text" autocomplete="off" spellcheck="false" />
  </div>

  <button class="${id}-go" type="button">Go</button>

  <div class="${id}-status"></div>
</div>
`;
}

function panelCssTemplate(id: string): string {
    return `.${id}-panel {
    padding: 8px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.${id}-input {
    padding: 4px 6px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    color: inherit;
    border-radius: 3px;
}

.${id}-go {
    padding: 6px 10px;
    background: #2d7;
    color: #111;
    border: 0;
    border-radius: 3px;
    cursor: pointer;
    font-weight: 600;
}

.${id}-status { font-size: 12px; min-height: 1em; }
.${id}-error  { color: #f55; }
.${id}-ok     { color: #5d7; }
`;
}

function indexJsTemplate(id: string, name: string): string {
    return `// ${name}
// The client object is injected by Luggage; no import needed.

var panel = document.querySelector('.${id}-panel[data-plugin="${id}"]');
var input = panel.querySelector(".${id}-input");
var goBtn = panel.querySelector(".${id}-go");
var statusEl = panel.querySelector(".${id}-status");

function setStatus(msg, kind) {
    statusEl.textContent = msg;
    statusEl.className = "${id}-status" + (kind ? " ${id}-" + kind : "");
}

goBtn.addEventListener("click", function () {
    var value = input.value.trim();
    if (!value) return setStatus("Enter something first.", "error");
    client.send(value);
    setStatus("Sent.", "ok");
});

setStatus("Ready.");
`;
}
