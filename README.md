<div align="center">
<h1>Super Resolution — DLSS for Minecraft 1.21.4</h1>
<p>Linux x86_64 · Fabric + Iris · 开箱即用</p>
</div>

----

在 Minecraft 中内置 NVIDIA DLSS 超分辨率算法，以提升游戏性能与画质。

本仓库是 [187J3X1-114514/superresolution](https://github.com/187J3X1-114514/superresolution) 的特化分支，针对 **Linux x86_64 + Minecraft 1.21.4 + Fabric + Iris** 这一特定场景做了定制与优化，仅保留 DLSS 算法路径。

## 特化内容

- **内置 NVIDIA NGX DLSS 运行时**：将 `libnvidia-ngx-dlss.so.310.7.0` 打包进 jar，开箱即用，无需手动下载放置动态库
- **DLSS SDK 固定 v310.7.0**：规避 v310.6.0 在 Linux 上的 NGX snippet 签名验证 bug
- **默认启用 Vulkan 初始化**：DLSS 依赖 Vulkan 互操作路径，不再被跳过
- **默认暗色主题**
- 移除了跨平台/多版本分支，专注于单一目标以减少维护负担

## 环境要求

### 系统

* **Linux x86_64**（不支持 Windows / macOS，本仓库不为其它平台增加额外工作）

### Minecraft

* **版本**：1.21.4
* **加载器**：Fabric 0.16.14+
* **光影**：Iris 1.8.8+

### 显卡

* **NVIDIA RTX 系列**（DLSS 需要 NVIDIA 驱动 + NGX 支持）
* 支持 OpenGL 4.6 及以上
* 支持 OpenGL 扩展 `GL_EXT_memory_object` `GL_EXT_semaphore`
* 支持 Vulkan >= 1.2

### Java

* **JDK 25**（构建用；运行时使用游戏自带 Java 即可）

## 安装

从 [Releases](https://github.com/Nk-YMZ/Super-Resolution-1.21.4/releases) 下载 jar，放入 `mods/` 目录。

DLSS 运行时会在首次启动时自动解压到 `config/super_resolution/libraries/`，无需任何手动操作。

## 兼容性

* Sodium 正常工作
* Iris 正常工作
* Reese's Sodium Options 正常工作

## 构建

需要 JDK 25、Vulkan SDK、CMake + Ninja、Clang。

```bash
git clone --recurse-submodules https://github.com/Nk-YMZ/Super-Resolution-1.21.4.git
cd Super-Resolution-1.21.4
pip install pyyaml simplejson
cd native/cpp && python init.py && cd ../..
./gradlew :native:buildNative
./gradlew -Pminecraft_version_config=1.21.4 :fabric:build
```

产物在 `fabric/build/libs/`。

## 致谢

本分支站在巨人的肩膀上：

* **[187J3X1-114514](https://github.com/187J3X1-114514)** — 原项目 Super Resolution 的作者。他构建了完整的超分辨率框架、Iris 光影兼容层、OpenGL-Vulkan 互操作管线与跨版本基础设施，让 DLSS 接入 Minecraft 这件事从零到一成为可能。本分支所做的全部工作都建立在他打下的地基之上
* **原项目贡献者们** — 每一位提交代码、报告 issue、测试反馈的贡献者，共同把这个项目打磨到可用的程度
* **[NVIDIA Corporation](https://github.com/NVIDIA/DLSS)** — 提供 DLSS SDK 与 NGX 运行时，并开源了 Linux 版本的 snippet 二进制

最后，感谢 AI 参与了本分支的开发：

* **[OpenCode](https://opencode.ai)** — 本仓库使用的 AI 编程工具，提供了完整的项目上下文管理与多轮交互能力。通过 OpenCode 切换不同底层模型协作完成开发：
  - **[Z.ai GLM-5.2](https://z.ai)** — 主力模型，承担了主要工作量：原生库打包链路设计、`NativeLibManager` 接口改造、`build.gradle.kts` 构建脚本修改、DLSS 子模块从 v310.6.0 升级至 v310.7.0 的决策与签名 bug 分析、构建验证、Release 发布等全流程
  - **[Moonshot Kimi K2.7 Code](https://www.moonshot.cn)** — 在项目前期参与了源码解读与可行性验证工作

没有这些 AI 工具的协助，从子模块签名 bug 排查到原生库打包这一系列工程问题，对个人开发者而言将会困难得多。AI 不是取代创造，而是让一个人也能做到原本需要一个团队才能做的事。

## 许可证

* 模组本身：GPL-3.0
* 本机库：MIT
* 本软件包含 NVIDIA Corporation 提供的源代码
