# AGENTS.md — Super Resolution

Minecraft mod (Fabric/Forge/NeoForge) that embeds upscaling algorithms (FSR, DLSS, XeSS, SGSR). Java + Kotlin/Gradle build, C++ native libraries, Manifold preprocessor for cross-version code.

## Build basics

- Required order: initialize native deps → build native libs → build Java.
- Native deps must be initialized once before building:
  ```bash
  pip install pyyaml simplejson
  cd native/cpp && python init.py
  ```
- Build native libs for current host:
  ```bash
  ./gradlew :native:buildNative
  ```
  Output lands in `common/src/main/resources/lib/` (ignored by git).
- Build a single version:
  ```bash
  ./gradlew -Pminecraft_version_config=1.20.1 :forge:build
  ./gradlew -Pminecraft_version_config=26.2 :fabric:build
  ```
- Build one configured version and collect JARs:
  ```bash
  ./gradlew buildOneVersion -Psr.version=1.20.1
  ```
- Build every configured version:
  ```bash
  ./gradlew buildAll                # native + all versions
  ./gradlew buildAllVersions        # Java only; assumes natives exist
  python script/buildAll.py         # Python alternative; auto-detects JDK
  ```
- Release artifacts are copied to `build_jars/` (ignored by git).

## Multi-version configuration

- Version config lives in `configs/*.json`.
- `gradle.properties` sets `minecraft_version_config=26.2` (default).
- Each config declares `common.platforms` (e.g. `["fabric"]`, `["forge"]`) and Java/MC versions; only those subprojects are included.
- Some configs set `skip_build: true`; `buildAllVersions` skips them.
- The `unobfuscated: true` flag in a config swaps Fabric Loom to `net.fabricmc.fabric-loom-remap` automatically.

## Native build

- Uses CMake + Ninja. On Windows use MinGW GCC; on Linux use GCC/Clang.
- Requires Vulkan SDK (CI uses 1.4.321.1).
- Uses git submodules for DLSS, FidelityFX SDK, XeSS, glslang, freetype. Clone with `git submodule update --init --recursive`.
- Native build scripts: `native/cpp/build_linux.sh`, `native/cpp/build_windows.ps1`, `native/cpp/build_windows_docker.sh`.

## Preprocessor / generated files

- `build.properties` is generated during Gradle configuration by `buildSrc/src/main/kotlin/multiversion.gradle.kts`. It defines `MC_VER`, `MC_...`, `IS_DEV`, `IS_VULKAN`, `ENABLE_AUTO_DOWNLOAD`, `USE_DEBUG_LIB` for the Manifold preprocessor.
- Do not edit `build.properties` manually; re-run Gradle to regenerate.
- `gradle.properties` defaults: `is_dev=true`, `is_vulkan=false`, `enable_auto_download=true`, `use_debug_lib=true`.
- `use_debug_lib=true` includes debug native libs and excludes release libs from resources.

## Dev / run client

- Run directory is `runs/<loader>/`.
- Run tasks exist only for the loaders declared in the active config:
  ```bash
  ./gradlew :fabric:runClient
  ./gradlew :forge:runClient
  ./gradlew :neoforge:runClient
  ```

## Toolchain

- CI uses JDK 21 (Temurin). Individual configs may require higher (`26.2` uses Java 25).
- No unit/integration tests exist; verification is building + running the client.
- No lint or formatter tasks are configured.

## Git / ignored files

- `.gitignore` ignores `build_jars/`, `build.properties`, `common/src/main/resources/lib/`, native build dirs, and `/AGENTS.md` itself.
- If asked to commit `AGENTS.md`, note that it is currently ignored and would need a `.gitignore` change to track.

## Release workflow

- `./gradlew uploadToModrinth` reads the latest `changelogs/<semver>.md` and uploads `build_jars/*.jar` interactively.
- `Build_and_Upload.yml` builds natives on Linux + Windows, then builds all versions and creates a GitHub release using the latest changelog.
