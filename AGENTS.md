# AGENTS.md — Super Resolution

Minecraft 模组（Fabric/Forge/NeoForge），内置 NVIDIA DLSS 超分辨率算法以提升游戏性能。Java + Kotlin/Gradle 构建，C++ 原生库，Manifold 预处理器实现跨版本代码。

## 工作约束（默认假设，无需每次重复）

- **目标平台**：仅 Linux x86_64。不考虑 Windows / macOS / Android 等其它平台，不为它们增加额外工作。
- **目标版本**：仅 Minecraft 1.21.4 + Fabric + Iris。
- **构建 JDK**：统一使用本机默认的 **JDK 25**（直接调用，无需指定 toolchain 或 JAVA_HOME）。
- **图形 API**：默认 OpenGL 路径（`is_vulkan=false`）由驱动决定；DLSS 走 Vulkan 互操作路径。
- 修改配置 / 构建脚本时，禁止引入跨平台或多版本分支，保持单一目标。

## 构建基础

- 必需顺序：初始化原生依赖 → 构建原生库 → 构建 Java。
- 构建前需初始化原生依赖（仅一次）：
  ```bash
  pip install pyyaml simplejson
  cd native/cpp && python init.py
  ```
- 为当前主机构建原生库：
  ```bash
  ./gradlew :native:buildNative
  ```
  产出到 `common/src/main/resources/lib/`（被 git 忽略）。
- 构建单个版本：
  ```bash
  ./gradlew -Pminecraft_version_config=1.20.1 :forge:build
  ./gradlew -Pminecraft_version_config=26.2 :fabric:build
  ```
- 构建一个已配置版本并收集 JAR：
  ```bash
  ./gradlew buildOneVersion -Psr.version=1.20.1
  ```
- 构建全部已配置版本：
  ```bash
  ./gradlew buildAll                # 原生 + 全部版本
  ./gradlew buildAllVersions        # 仅 Java；假定原生库已存在
  python script/buildAll.py         # Python 替代方案；自动探测 JDK
  ```
- 发布产物拷贝到 `build_jars/`（被 git 忽略）。

## 多版本配置

- 版本配置位于 `configs/*.json`。
- `gradle.properties` 设置 `minecraft_version_config=26.2`（默认）。
- 每个配置声明 `common.platforms`（如 `["fabric"]`、`["forge"]`）和 Java/MC 版本；仅包含声明的子项目。
- 部分配置设置 `skip_build: true`；`buildAllVersions` 会跳过它们。
- 配置中的 `unobfuscated: true` 标志会自动将 Fabric Loom 切换为 `net.fabricmc.fabric-loom-remap`。

## 原生构建

- 使用 CMake + Ninja。Windows 上用 MinGW GCC；Linux 上用 GCC/Clang。
- 需要 Vulkan SDK（CI 使用 1.4.321.1）。
- 通过 git 子模块获取 DLSS、FidelityFX SDK、XeSS、glslang、freetype。克隆时用 `git submodule update --init --recursive`。
- 原生构建脚本：`native/cpp/build_linux.sh`、`native/cpp/build_windows.ps1`、`native/cpp/build_windows_docker.sh`。
- **DLSS 子模块固定到 v310.7.0**（commit `a291cc7d2cc642a51566f3dfd5376f635cd1b284`）：v310.6.0 在 Linux 上有 NGX snippet 签名验证 bug（`nvLoadSignedLibraryW() failed ... missing or corrupted`），310.7.0 已修复。父仓库 gitlink 已指向此 SHA。
- **NVIDIA NGX DLSS 运行时** `libnvidia-ngx-dlss.so.310.7.0`（来自 `native/cpp/SRNativeDLSS/third_party/DLSS/lib/Linux_x86_64/rel/`）由 `copyNativeLibAll` 任务拷贝进 `common/src/main/resources/lib/`，并经 `NativeLibManager.LIB_NVIDIA_NGX_DLSS` 在启动时解压到 `NATIVE_LIBRARIES_DIR`，与 `DLSS.java` 设置的 `NGX_FEATURE_DLL_PATH` 同目录，使 jar 开箱即用。
- `NativeLibManager.NativeLib` 的 `nameIsPath=true` 模式下 `baseName` 视为完整文件名直接使用，不再自动追加 `.so` 后缀（用于 NGX 运行时这种带版本号后缀的现成文件名）。
- 不打包 `libnvidia-ngx-dlssd.so.*`（带 in-app overlay 的调试版）和 `libnvidia-ngx-dlssg.so.*`（Frame Generation）—— 本 mod 不使用，避免膨胀 jar。

## 预处理器 / 生成文件

- `build.properties` 在 Gradle 配置阶段由 `buildSrc/src/main/kotlin/multiversion.gradle.kts` 生成。它为 Manifold 预处理器定义 `MC_VER`、`MC_...`、`IS_DEV`、`IS_VULKAN`、`ENABLE_AUTO_DOWNLOAD`、`USE_DEBUG_LIB`。
- 不要手动编辑 `build.properties`；重新运行 Gradle 即可重新生成。
- `gradle.properties` 默认值：`is_dev=true`、`is_vulkan=false`、`enable_auto_download=true`、`use_debug_lib=true`。
- `use_debug_lib=true` 时包含 debug 原生库并从资源中排除 release 库。

## 开发 / 运行客户端

- 运行目录为 `runs/<loader>/`。
- 运行任务仅对当前配置中声明的加载器存在：
  ```bash
  ./gradlew :fabric:runClient
  ./gradlew :forge:runClient
  ./gradlew :neoforge:runClient
  ```

## 工具链

- CI 使用 JDK 21（Temurin）。个别配置可能要求更高（`26.2` 使用 Java 25）。
- 无单元/集成测试；验证方式是构建 + 运行客户端。
- 未配置 lint 或格式化任务。

## Git / 忽略文件

- `.gitignore` 忽略 `build_jars/`、`build.properties`、`common/src/main/resources/lib/`、原生构建目录以及 `/AGENTS.md` 本身。
- 如果被要求提交 `AGENTS.md`，注意它当前被忽略，需要修改 `.gitignore` 才能纳入版本控制。

## 发布流程

- `./gradlew uploadToModrinth` 读取最新的 `changelogs/<semver>.md` 并交互式上传 `build_jars/*.jar`。
- `Build_and_Upload.yml` 在 Linux + Windows 上构建原生库，然后构建全部版本并使用最新 changelog 创建 GitHub release。
