/**
 * Type definitions for Luggage Plugin API
 */

interface LuggageLineEvent {
    /** The plain text of the line (some events may provide only the string) */
    text?: string;
    /** Array of text segments with formatting information */
    segments?: Array<{
        text: string;
        fg?: string;
        bg?: string;
        bold?: boolean;
        italic?: boolean;
        underline?: boolean;
    }>;
}

/**
 * Global window extensions for Luggage
 */
interface Window {
    /** Access to other loaded Luggage plugins */
    __luggagePlugins?: { [pluginId: string]: any };
    /** Tauri API if available */
    __TAURI__?: any;
}

interface LuggageGMCPEvent {
    /** The GMCP module name (e.g. 'room.info') */
    module: string;
    /** The data payload of the GMCP packet */
    data: any;
}

interface LuggageEventMap {
    'line': LuggageLineEvent;
    'gmcp': LuggageGMCPEvent;
}

interface TimerOptions {
    /** Interval in milliseconds */
    interval: number;
    /** Function to execute when the timer fires */
    action: () => void;
    /** Whether the timer should repeat. Defaults to false. */
    repeat?: boolean;
}

interface TriggerOptions {
    /** Pattern to match against game output (string or RegExp) */
    pattern: string | RegExp;
    /** Action to perform when the pattern matches. Receives the matched text. */
    action: (text: string) => void;
}

interface PanelOptions {
    /** HTML content or path to HTML file for the panel */
    html?: string;
    /** Where to dock the panel ('top', 'bottom', 'left', 'right') or omit to float */
    dock?: 'top' | 'bottom' | 'left' | 'right';
}

interface TitlebarButtonOptions {
    /** Stable ID for idempotency (auto-generated if omitted) */
    id?: string;
    /** Text or HTML for the button content, e.g. '⚙' */
    icon: string;
    /** Tooltip text */
    title: string;
    /** Extra CSS class (optional) */
    className?: string;
    /** Click handler, receives the MouseEvent */
    onClick: (ev: MouseEvent) => void;
}

interface LayoutSnapshotHooks {
    /** Returns a JSON-serialisable snapshot of your current UI state */
    get: () => any;
    /** Restores that state to the live UI */
    apply: (snap: any) => void;
}

interface CommandOptions {
    /** Unique ID for the command */
    id: string;
    /** Display name in the command palette */
    name: string;
    /** Function to execute when the command is triggered */
    action: () => void;
    /** Category for grouping in the command palette */
    category?: string;
    /** Default keyboard shortcut (e.g. 'Alt+I') */
    defaultShortcut?: string;
}

/**
 * The Luggage Client API object, provided automatically to plugin scripts.
 */
declare const client: {
    /** Send a command to the MUD */
    send: (command: string) => void;
    /** Send a command without echoing it to the game output */
    sendSilent: (command: string) => void;
    /** Display a message in the game output locally */
    echo: (message: string) => void;
    /** Subscribe to an event */
    on: <K extends keyof LuggageEventMap>(event: K, handler: (ev: LuggageEventMap[K]) => void) => void;
    /** Unsubscribe from an event */
    off: <K extends keyof LuggageEventMap>(event: K, handler: (ev: LuggageEventMap[K]) => void) => void;
    
    intercept: {
        /** Intercept incoming game output. Return data to pass it through, or null to suppress (gag). */
        incoming: (fn: (ev: any) => any) => void;
        /** Intercept outgoing commands. Return command string to pass it through, or null to suppress. */
        outgoing: (fn: (cmd: string) => string | null) => void;
    };
    
    storage: {
        /** Retrieve data by key. Returns a Promise. */
        get: (key: string) => Promise<any>;
        /** Store any JSON-serializable value. */
        set: (key: string, value: any) => Promise<void>;
        /** Remove a stored key. */
        delete: (key: string) => Promise<void>;
    };
    
    timers: {
        /** Create a timer. Returns a timer ID. */
        create: (opts: TimerOptions) => string;
        /** Stop and remove a timer by ID. */
        remove: (id: string) => void;
    };
    
    triggers: {
        /** Create a trigger. Returns a trigger ID. */
        create: (opts: TriggerOptions) => string;
        /** Remove a trigger by ID. */
        remove: (id: string) => void;
    };
    
    bus: {
        /** Publish data to a topic. All subscribers across all plugins receive it. */
        publish: (topic: string, data: any) => void;
        /** Subscribe to a topic. Handler receives (data, senderPluginId). */
        subscribe: (topic: string, fn: (data: any, senderPluginId: string) => void) => void;
        /** Unsubscribe from a topic. */
        unsubscribe: (topic: string, fn: (data: any, senderPluginId: string) => void) => void;
    };
    
    /** Current room info (automatically updated from GMCP) */
    room: {
        identifier?: string;
        short?: string;
        exits?: { [direction: string]: any };
        [key: string]: any;
    };
    /** Character vitals and info (automatically updated from GMCP) */
    char: {
        hp?: string | number;
        maxhp?: string | number;
        gp?: string | number;
        maxgp?: string | number;
        xp?: string | number;
        burden?: string | number;
        [key: string]: any;
    };
    /** Group/party data (automatically updated from GMCP) */
    group: any;
    
    panels: {
        /** Register a panel from code */
        register: (opts: PanelOptions) => void;
        /** Add a button to the panel's host titlebar. Available in client 1.10.2+. */
        addTitlebarButton: (opts: TitlebarButtonOptions) => { remove: () => void };
        /** Register hooks for layout snapshot/restore. Available in client 1.10.2+. */
        registerLayoutSnapshot: (hooks: LayoutSnapshotHooks) => void;
    };
    
    /** Call a Tauri backend command directly. Returns a Promise. */
    invoke: (command: string, args?: any) => Promise<any>;
    
    commands: {
        /** Add a command to the command palette. */
        register: (opts: CommandOptions) => void;
    };
    
    /** Register a function callable from RunScript trigger actions. */
    registerFunction: (name: string, fn: Function) => void;
};
