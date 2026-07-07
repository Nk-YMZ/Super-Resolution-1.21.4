cmake_minimum_required(VERSION 3.15)
project(SuperResolutionNativeMain)

set(THIRD_PARTY_DIR "${PROJECT_SOURCE_DIR}/third_party")
set(FT_DIR "${THIRD_PARTY_DIR}/freetype")
set(GLSLANG_DIR "${THIRD_PARTY_DIR}/glslang")

include_directories(
    ${PROJECT_SOURCE_DIR}/include
    ${PROJECT_SOURCE_DIR}/src
    ${GLSLANG_DIR}
)

add_definitions(-DON_WIN64 -D_DISABLE_CONSTEXPR_MUTEX_CONSTRUCTOR)
set(CMAKE_MSVC_RUNTIME_LIBRARY "MultiThreaded$<$<CONFIG:Debug>:Debug>")

#add freetype
add_subdirectory("${FT_DIR}")
#add glslang
set(GLSLANG_ENABLE_INSTALL_DEFAULT OFF)
set(BUILD_SHARED_LIBS OFF)
set(LIB_TYPE STATIC)
set(ENABLE_GLSLANG_BINARIES OFF)
set(GLSLANG_TESTS_DEFAULT OFF)
set(ENABLE_HLSL OFF)
set(BUILD_EXTERNAL ON)
set(ENABLE_SPIRV_TOOLS_INSTALL OFF)
set(SPIRV_TOOLS_BUILD_STATIC ON)
set(SPIRV_SKIP_EXECUTABLES ON)
add_subdirectory(${GLSLANG_DIR})

#add source files
aux_source_directory(${PROJECT_SOURCE_DIR}/src ALL_SRC)
aux_source_directory(${PROJECT_SOURCE_DIR}/src/nvg NVG_SRC)
add_library(SR_MAIN_LIB SHARED ${ALL_SRC} ${NVG_SRC})
target_compile_definitions(SR_MAIN_LIB PRIVATE SRLIB_VERSION="${SRLIB_VERSION}")
set_target_properties(SR_MAIN_LIB PROPERTIES OUTPUT_NAME "libSuperResolution+${LIB_PLATFORM}+${SR_BUILD_TYPE}")
target_link_libraries(SR_MAIN_LIB
    glslang
    glslang-default-resource-limits
    SPIRV
    freetype
)
