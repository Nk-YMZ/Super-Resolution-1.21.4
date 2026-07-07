#!/bin/bash
set -euo pipefail

# ============================================================================
# Build Windows DLLs on Linux (在 sr-cross-win Docker 容器内运行)
#
# Usage:
#   docker run --rm -v "$PWD":/src sr-cross-win ./build_windows_on_linux.sh
#   docker run --rm -v "$PWD":/src sr-cross-win ./build_windows_on_linux.sh Debug
#   docker run --rm -v "$PWD":/src sr-cross-win ./build_windows_on_linux.sh Release
#
# 环境变量:
#   SR_DLSS — 模块开关 (ON/OFF)
# ============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

SR_DLSS="${SR_DLSS:-ON}"
BUILD_TYPES="${1:-Debug Release}"

echo "=== SuperResolution Windows cross-build ==="
echo "  SR_DLSS=${SR_DLSS}"
echo "  Build types: ${BUILD_TYPES}"
echo ""

for BUILD_TYPE in ${BUILD_TYPES}; do
    echo "=== Building ${BUILD_TYPE} ==="

    rm -rf buildWindowsOnLinux
    cmake -G Ninja -S . -B buildWindowsOnLinux \
        -DCMAKE_BUILD_TYPE="${BUILD_TYPE}" \
        -DCMAKE_SYSTEM_NAME=Windows \
        -DCMAKE_C_COMPILER=clang-cl \
        -DCMAKE_CXX_COMPILER=clang-cl \
        -DCMAKE_LINKER=lld-link \
        -DSR_DLSS="${SR_DLSS}" \
        -DVulkan_INCLUDE_DIR="${SCRIPT_DIR}/third_party" \
        -DVulkan_LIBRARY="/tmp/vulkan-stub/vulkan-1.lib"

    cmake --build buildWindowsOnLinux --config "${BUILD_TYPE}" -- -j"$(nproc)"
    echo "[${BUILD_TYPE}] Done."
    echo ""
done

echo "=== All builds complete ==="
echo "Output DLLs:"
find "${SCRIPT_DIR}/output" -name '*.dll' 2>/dev/null | sort || echo "(none found)"
