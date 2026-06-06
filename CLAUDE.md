# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

IntelliJ IDEA Community-Edition plugin that wraps MyBatis Generator with a GUI, plus editor integrations for Mapper interface ⇄ Mapper XML navigation/validation. Targets IDEA `IC-2022.2` and builds against JDK 17. Plugin ID: `com.chuntung.plugin.mybatisbuilder`.

## Build & run

Use the Gradle wrapper (do not invoke a system Gradle). Build relies on the `org.jetbrains.intellij` Gradle plugin.

- `./gradlew buildPlugin` — produces the distributable zip under `build/distributions/`. This is what CI runs (`.github/workflows/gradle.yml`).
- `./gradlew runIde` — launches a sandbox IDE with the plugin installed; use for manual UI testing (the Swing dialogs and tool window cannot be exercised by unit tests).
- `./gradlew test` — runs the unit test suite.
- `./gradlew test --tests "com.chuntung.plugin.mybatis.builder.util.StatementTypeInferrerTest"` — run a single test class.
- `./gradlew verifyPlugin` — validates the plugin descriptor / structure.

`gradle.properties` controls the IDEA SDK version (`ideaVersion`), the `untilBuild` ceiling (`customUtilBuild`), and the release `version`. Bump these when targeting a newer platform.

### Source-set quirks (defined in `build.gradle`)

- `main` excludes `**/support` — files under `src/main/java/.../mybatis/support/` are scratch/dev-only and are **not** compiled into the plugin. The `.gitignore` confirms this directory is local-only.
- `test` excludes `**/example`, `MybatisBuilderServiceTest.java`, and `GeneratorToolWrapperTest.java`. These tests require live infrastructure (an IDEA test fixture / a real DB / file output) and are intentionally skipped in the default `test` task. Don't "fix" them by un-excluding without understanding the dependency — invoke them manually when validating end-to-end flows.

## Architecture

The plugin is roughly three layers: an **IntelliJ extension/action layer** registered in `plugin.xml`, a **services layer** that holds state and brokers JDBC/MyBatis-Generator calls, and an **MBG wrapper layer** that adapts user input into a MyBatis Generator `Configuration` and drives it.

### Services (registered in `plugin.xml` under `<extensions>`)

- `MybatisBuilderService` (project service, in package root) — the unified facade used by actions/dialogs. Owns JDBC metadata reads (`fetchDatabases` / `fetchTables` / `fetchColumns`) via `SimpleDataSourceFactory`, and proxies persistence through `MybatisBuilderSettingsManager`.
- `MybatisBuilderSettingsManager` (project service) — `PersistentStateComponent<MybatisBuilderSettings>` backed by `mybatisbuilder.xml` (state name `MybatisBuilder.project.settings`). Connection **passwords are stored separately in IntelliJ `PasswordSafe`**, keyed by `MybatisBuilderConnection_<id>` — never persist them into the settings XML.
- `MyBatisBuilderConfigManager` (application service) — application-wide config.

### Generator wrapper (`generator/`)

- `GeneratorToolWrapper` — converts a `GeneratorParamWrapper` into MBG's `Configuration` and runs `MyBatisGenerator.generate(...)`. It forces SLF4J logging and temporarily overrides the `javax.xml.parsers.DocumentBuilderFactory` system property to work around an MBG XML-parsing issue — preserve those guards if refactoring.
- `generator/plugins/` — custom MBG plugins (`LombokPlugin`, `MapperAnnotationPlugin`, `RenamePlugin`, `ExampleRowBoundsPlugin`, `DsqlRuntimePatchPlugin`, `selectwithlock/SelectWithLockPlugin`). Each plugin's configurable fields are annotated with `@PluginConfig` (`generator/annotation/PluginConfig.java`); the dialogs introspect those annotations reflectively to render UI and read defaults, so add new options via the annotation rather than hard-coding form fields.
- `generator/callback/JavaMergerShellCallback` — implements the "merge existing MyBatis files" feature by re-parsing existing Java/XML sources before MBG overwrites them.

### UI (`view/`)

Swing-based, built with IntelliJ GUI Designer. The `.form` files are the source of truth for layout; the paired `.java` files declare components but the IDE writes layout code at compile time. **Editing dialog layout outside the IntelliJ GUI Designer will silently break the build** — change `.form` via the IDE or hand-edit both files in sync.

- `MybatisBuilderToolWindowFactory` / `MybatisBuilderToolWindowPanel` — left-side tool window showing the connection/database/table tree.
- `MybatisBuilderSettingsDialog` — connection + defaults management.
- `MybatisBuilderParametersDialog` — the main "Build" dialog driven by `GeneratorParamWrapper`.
- `MapperToXmlLineMarkerProvider` / `XmlToMapperLineMarkerProvider` — gutter icons for Mapper-interface ⇄ Mapper-XML navigation.

### Mapper XML integration (`reference/`, `annotator/`, `util/MapperXmlIndex`)

- `util/MapperXmlIndex` is the **single source of truth** for namespace → XML-file resolution. It caches a `namespace → VirtualFile[]` map via `CachedValuesManager` keyed on `PsiModificationTracker.MODIFICATION_COUNT`. All cross-language features (line markers, references, annotator, intention) should go through it rather than re-scanning XML.
- `MapperNamespaceReferenceContributor` contributes PSI references from `<mapper namespace="...">` and `id`/`resultMap`/`parameterType`/`resultType` attributes into the corresponding Java mapper interface / class.
- `MapperTypeAnnotator` validates `parameterType` / `resultType` on statement tags against the matching mapper method signature.
- `action/idea/GenerateXmlStatementIntention` — IntentionAction that generates a stub `<select>`/`<insert>`/etc. for a mapper method, optionally creating a new XML mapper file if none exists. `StatementTypeInferrer` decides the tag name from the method name.

### Actions (`action/idea/`)

All `AnAction` / `IntentionAction` classes are wired in `plugin.xml`. Key entry points:
- `ManageAction` → opens `MybatisBuilderSettingsDialog`.
- `BuildAction` → reads the tool-window tree selection, loads last params via `MybatisBuilderService`, opens `MybatisBuilderParametersDialog`. Only tables from one connection + one database can be built at once (enforced in `populateSelectedTables`).
- `RunMyBatisGeneratorAction` → runs MBG against a user-supplied `generator-config.xml` from the project view popup.
- `CopyAsExecutableSQLAction` → parses `Preparing:` / `Parameters:` lines from MyBatis logs in the editor/console.
- `NewMyBatisGeneratorConfigAction` + `MyBatisBuilderFileTemplateProvider` (templates under `resources/fileTemplates/`) → "New → MyBatis Generator Config" file template.

## Conventions

- Action IDs in code (e.g. `BuildAction.ACTION_ID = "MyBatisBuilder.Build"`) must stay in sync with `plugin.xml`; the action looks itself up via `ActionManager`.
- Files carry a `Copyright (c) <year> Tony Ho / Chuntung Ho. Some rights reserved.` header — match this style for new files.
- When adding a new MBG plugin, register it on `GeneratorParamWrapper.selectedPlugins` (via the dialog) and annotate fields with `@PluginConfig` so the UI picks them up automatically.
- When adding a new extension point, register it under `<extensions defaultExtensionNs="com.intellij">` in `plugin.xml`; the build does not auto-discover services or providers.
