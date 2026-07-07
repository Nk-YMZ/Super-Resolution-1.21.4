# AGENTS.md — Super Resolution

Minecraft 模组（Fabric），内置 NVIDIA DLSS 超分辨率算法以提升游戏性能。Java + Kotlin/Gradle 构建，C++ 原生库，Manifold 预处理器实现跨版本代码。

## 工作约束（默认假设，无需每次重复）

- **目标平台**：仅 Windows x86_64。不考虑 Linux / macOS / Android 等其它平台，不为它们增加额外工作。
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
- 为当前主机构建原生库（Windows 主机）：
  ```powershell
  .\gradlew :native:buildNative
  ```
  产出到 `common/src/main/resources/lib/`（被 git 忽略）。
- 在 Linux 上交叉编译 Windows 原生库：
  ```bash
  ./gradlew :native:buildNativeCppWindows
  ```
  通过 Docker + msvc-wine 调用 `native/cpp/build_windows_docker.sh` → `build_windows_on_linux.sh`。
- 构建当前版本：
  ```bash
  ./gradlew -Pminecraft_version_config=1.21.4 :fabric:build
  ```
- 发布产物拷贝到 `build_jars/`（被 git 忽略）。

## 原生构建

- 使用 CMake + MSVC（Windows 本机）或 clang-cl + lld-link（Linux 交叉编译）。
- 需要 Vulkan SDK（Windows 本机）或仓库自带的 Vulkan 头 + Docker 生成的 `vulkan-1.lib` stub（Linux 交叉编译）。
- 通过 git 子模块获取 DLSS、glslang、freetype。克隆时用 `git submodule update --init --recursive`。
- 原生构建脚本：`native/cpp/build_windows.ps1`、`native/cpp/build_windows_docker.sh`、`native/cpp/build_windows_on_linux.sh`。
- **DLSS 子模块固定到 v310.7.0**（commit `a291cc7d2cc642a51566f3dfd5376f635cd1b284`）：v310.6.0 在 Linux 上有 NGX snippet 签名验证 bug，310.7.0 已修复。Windows 端同样采用此版本以获得一致行为。
- **NVIDIA NGX DLSS 运行时** `nvngx_dlss.dll`（来自 `native/cpp/SRNativeDLSS/third_party/DLSS/lib/Windows_x86_64/rel/`）由 `copyNativeLibAll` 任务拷贝进 `common/src/main/resources/lib/`，并经 `NativeLibManager.LIB_NVIDIA_NGX_DLSS` 在启动时解压到 `NATIVE_LIBRARIES_DIR`，与 `DLSS.java` 设置的 `NGX_FEATURE_DLL_PATH` 同目录，使 jar 开箱即用。
- `NativeLibManager.NativeLib` 的 `nameIsPath=true` 模式下 `baseName` 视为完整文件名直接使用，不再自动追加 `.dll` 后缀（用于 NGX 运行时这种现成的文件名）。
- 不打包 DLSSD / DLSSG 相关运行时 —— 本 mod 不使用，避免膨胀 jar。
- 仅编译 DLSS 模块：CMake 中 `SR_DLSS=ON`，不构建 FSR / XeSS 相关原生库。

## OpenGL-Vulkan 互操作

- DLSS 在 Vulkan 端运行，渲染在 OpenGL 端，因此需要跨 API 共享纹理和信号量。
- 本分支仅实现 **Windows 路径**：Vulkan 使用 `VK_KHR_external_memory_win32` / `VK_KHR_external_semaphore_win32` 导出 NT 句柄，OpenGL 使用 `GL_EXT_memory_object_win32` / `GL_EXT_semaphore_win32` 导入。
- 对应代码集中在 `common/src/main/java/io/homo/superresolution/core/graphics/vulkan/VulkanInterop.java` 和 `RenderSystems.java`，已移除 Linux 的 file-descriptor 路径。
- `AlgorithmDescriptions` 中 DLSS 仅声明需求 `GL_EXT_memory_object` / `GL_EXT_semaphore`（与上游一致），Windows 驱动通常同时提供对应的 win32 扩展。

## 当前状态与注意事项

- 已完成 `dev` 分支的 Windows-only 移植，并推送到 `origin/dev`（`c5de4b74`）。`main` 仍为 Linux-only，未改动。
- Java 层已通过 `./gradlew :common:compileJava` 编译验证。
- **尚未运行原生库编译**（Windows 本机 MSVC 或 Linux Docker 交叉编译），因此 `common/src/main/resources/lib/` 里还没有实际的 DLL 文件。
- **尚未在 Windows 客户端运行测试**。DLSS 功能正确性需要实际构建并运行游戏验证。
- 换设备继续开发时，需要重新执行：
  ```bash
  git submodule update --init --recursive
  pip install pyyaml simplejson
  cd native/cpp && python init.py
  ```
  然后按“构建基础”或“Linux 交叉编译 Windows DLL”章节构建。

## Linux 交叉编译 Windows DLL（镜像已预拉）

本小节用于换设备或 CI 环境已存在 `ghcr.io/shiroiame-kusu/msvc-wine-debian12:0.0.1` 镜像时快速开始。

### 1. 确认镜像存在

```bash
docker images | grep msvc-wine
```

应看到 `ghcr.io/shiroiame-kusu/msvc-wine-debian12` 条目。如果没有，先执行：

```bash
docker pull ghcr.io/shiroiame-kusu/msvc-wine-debian12:0.0.1
```

或自行构建本地镜像：

```bash
cd native/cpp
docker build -t sr-cross-win -f docker/Dockerfile .
```

### 2. 初始化原生依赖（仅一次）

```bash
git submodule update --init --recursive
pip install pyyaml simplejson
cd native/cpp && python init.py
```

### 3. 交叉编译 DLL

**方式 A：通过 Gradle 任务（推荐）**

```bash
./gradlew :native:buildNativeCppWindows
```

**方式 B：手动运行 Docker 脚本**

```bash
cd native/cpp
bash build_windows_docker.sh
```

**方式 C：直接执行 docker run**

```bash
cd native/cpp
docker run --rm -v "$PWD":/src ghcr.io/shiroiame-kusu/msvc-wine-debian12:0.0.1 ./build_windows_on_linux.sh
```

若使用本地镜像：

```bash
docker run --rm -v "$PWD":/src sr-cross-win ./build_windows_on_linux.sh
```

### 4. 仅编译 Release 或 Debug

```bash
cd native/cpp
docker run --rm -v "$PWD":/src ghcr.io/shiroiame-kusu/msvc-wine-debian12:0.0.1 ./build_windows_on_linux.sh Release
```

不传参数时默认同时编译 Debug 和 Release。

### 5. 拷贝原生库到资源目录

```bash
./gradlew :native:copyNativeLibAll
```

产物（`libSuperResolution+win64+*.dll`、`libSuperResolutionDLSS+win64+*.dll`、`nvngx_dlss.dll`）会被拷贝到 `common/src/main/resources/lib/`。

### 6. 构建 Java

```bash
./gradlew -Pminecraft_version_config=1.21.4 :fabric:build
```

最终产物在 `fabric/build/libs/`。

## 预处理器 / 生成文件

- `build.properties` 在 Gradle 配置阶段由 `buildSrc/src/main/kotlin/multiversion.gradle.kts` 生成。它为 Manifold 预处理器定义 `MC_VER`、`MC_...`、`IS_DEV`、`IS_VULKAN`、`ENABLE_AUTO_DOWNLOAD`、`USE_DEBUG_LIB`。
- 不要手动编辑 `build.properties`；重新运行 Gradle 即可重新生成。
- `gradle.properties` 默认值：`is_dev=false`、`is_vulkan=false`、`enable_auto_download=true`、`use_debug_lib=true`。
- `use_debug_lib=true` 时包含 debug 原生库并从资源中排除 release 库。

## 开发 / 运行客户端

- 运行目录为 `runs/fabric/`。
- 运行任务仅对 Fabric 存在：
  ```bash
  ./gradlew :fabric:runClient
  ```

## 工具链

- 使用 JDK 25（Minecraft 1.21.4 配置）。
- 无单元/集成测试；验证方式是构建 + 运行客户端。
- 未配置 lint 或格式化任务。

## Git / 忽略文件

- `.gitignore` 忽略 `build_jars/`、`build.properties`、`common/src/main/resources/lib/`、原生构建目录以及 `/AGENTS.md` 本身。
- 如果被要求提交 `AGENTS.md`，注意它当前被忽略，需要修改 `.gitignore` 才能纳入版本控制。
