# luggage-idea-plugin

JetBrains-platform plugin (IntelliJ IDEA, WebStorm, PhpStorm, PyCharm Pro, …) for Luggage
MUD plugin authors. Zero-config validation, autocomplete, scaffolding.

## Develop

First time only — generate the Gradle wrapper jar (we don't commit it):

```bash
gradle wrapper --gradle-version 8.9
```

Then:

```bash
./gradlew runIde         # sandbox IDE with this plugin installed
./gradlew buildPlugin    # build/distributions/luggage-idea-plugin-X.Y.Z.zip
./gradlew verifyPlugin   # JetBrains plugin verifier (run before each release)
```

## Publish

Set your Marketplace token once (in `~/.gradle/gradle.properties`):

```
publishToken=eyJhbGciOiJ...
```

Or pass it on the command line:

```bash
./gradlew publishPlugin -PpublishToken=eyJhbGciOiJ...
```

CI publishes automatically on tag push using the `JETBRAINS_MARKETPLACE_TOKEN` repo secret.

## Layout

```
src/main/kotlin/com/luggage/plugindev/
    LuggageManifestDetector.kt          — shape-based "is this a Luggage manifest" check
    LuggageManifestSchemaProvider.kt    — binds JSON Schema to detected manifest.json files
    LuggageJsLibraryProvider.kt         — exposes luggage.d.ts as a predefined JS library
    LuggageNewPluginAction.kt           — File → New → Luggage Plugin (4-file scaffold)
    LuggageFileTemplateGroupFactory.kt  — File → New → Luggage Files (individual templates)

src/main/resources/
    META-INF/plugin.xml                 — extension registrations, marketplace metadata
    schemas/manifest.schema.json        — copied from ../shared/schema by Gradle
    types/luggage.d.ts                  — copied from ../shared/types by Gradle
    fileTemplates/j2ee/Luggage *.ft     — scaffold files
    liveTemplates/Luggage.xml           — lsend, lon, lcapture, ltrigger, ltimer, lstorage
```

The Gradle `syncSharedAssets` task copies `../shared/` into `src/main/resources/` before each
build. Don't edit the copies under `src/main/resources/schemas` or `src/main/resources/types`
directly — they will be overwritten. Edit the originals in `../shared/`.
