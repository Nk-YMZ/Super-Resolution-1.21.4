# 构建

## 通用
* 确保已安装 CMake 3.15 或更高版本。
* 确保已安装 Vulkan SDK。
* 确保已安装 C++ 编译器。
* 确保已经克隆了子模块。
* 确保已经执行了 init.py 脚本以下载必要的依赖项。

## 对于 Windows
1. 确保已安装 Visual Studio 2019 或更高版本。
2. 在 `native/cpp` 目录运行 `build_windows.ps1`。
3. 构建产物位于 `output/` 目录下。

## 在 Linux 上交叉编译 Windows DLL
1. 确保已安装 Docker。
2. 在 `native/cpp` 目录运行 `build_windows_docker.sh`。
3. 构建产物位于 `output/` 目录下。
