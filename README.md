<div align="center">
<h1>Super Resolution — DLSS for Minecraft 1.21.4</h1>
<p>Windows x86_64 · Fabric + Iris · 开箱即用</p>
</div>

----

本分支（`dev`）基于 [`main`](https://github.com/Nk-YMZ/Super-Resolution-1.21.4/tree/main) 移植到 **Windows x86_64**，仅保留 DLSS 算法路径。除下列差异外，其余与 `main` 一致，请参考 `main` 分支的 README。

## 与 `main` 的差异

- **目标平台**：Windows x86_64（`main` 为 Linux x86_64）。已移除 Linux 的 file-descriptor 互操作路径，仅保留 Windows 的 NT 句柄路径。
- **OpenGL-Vulkan 互操作**：Vulkan 用 `VK_KHR_external_memory_win32` / `VK_KHR_external_semaphore_win32` 导出 NT 句柄，OpenGL 用 `GL_EXT_memory_object_win32` / `GL_EXT_semaphore_win32` 导入。
- **内置运行时**：打包 `nvngx_dlss.dll`（而非 `libnvidia-ngx-dlss.so.310.7.0`），首次启动解压到 `config/super_resolution/libraries/`。
- **原生库构建**：在 Linux 上通过 Docker + msvc-wine 交叉编译 Windows DLL（`./gradlew :native:buildNativeCppWindows`），详见 `AGENTS.md`。

## 下载

从 [Releases](https://github.com/Nk-YMZ/Super-Resolution-1.21.4/releases) 下载 Windows 版 jar，放入 `mods/` 目录即可。

## 构建

### Windows 本机构建

需要 Windows 主机、JDK 25、Vulkan SDK、MSVC、CMake。

```powershell
git clone --recurse-submodules https://github.com/Nk-YMZ/Super-Resolution-1.21.4.git
cd Super-Resolution-1.21.4
pip install pyyaml simplejson
cd native/cpp && python init.py && cd ..\..
.\gradlew :native:buildNative
.\gradlew -Pminecraft_version_config=1.21.4 :fabric:build
```

### Linux 交叉编译 Windows DLL

在 Linux 上通过 Docker + msvc-wine 交叉编译（镜像 `ghcr.io/shiroiame-kusu/msvc-wine-debian12:0.0.1`）：

```bash
git clone --recurse-submodules https://github.com/Nk-YMZ/Super-Resolution-1.21.4.git
cd Super-Resolution-1.21.4
pip install pyyaml simplejson
cd native/cpp && python init.py && cd ../..
./gradlew :native:buildNativeCppWindows
./gradlew :native:copyNativeLibAll
./gradlew -Pminecraft_version_config=1.21.4 :fabric:build
```

产物在 `fabric/build/libs/`。

