cmake_minimum_required(VERSION 3.15)
project(SuperResolutionNativeMain)
message(STATUS "生成DLSS模块")

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_definitions(-DON_LINUX64 -DFFX_GCC)
find_package(Vulkan REQUIRED)
link_directories(
    ${PROJECT_SOURCE_DIR}/libraries/${LIB_PLATFORM}
    ${PROJECT_SOURCE_DIR}/../SRNativeMain/libraries/${LIB_PLATFORM}
    ${PROJECT_SOURCE_DIR}/third_party/DLSS/lib/Linux_x86_64
)

include_directories(
    ${PROJECT_SOURCE_DIR}/include
    ${PROJECT_SOURCE_DIR}/../SRNativeMain/include
    ${PROJECT_SOURCE_DIR}/third_party
    ${PROJECT_SOURCE_DIR}/src
    ${PROJECT_SOURCE_DIR}/third_party/DLSS/include
)
aux_source_directory(${PROJECT_SOURCE_DIR}/src ALL_SRC)
add_library(SR_DLSS_LIB SHARED ${ALL_SRC})
set_target_properties(SR_DLSS_LIB PROPERTIES OUTPUT_NAME "SuperResolutionDLSS+${LIB_PLATFORM}+${SR_BUILD_TYPE}")
target_link_libraries(SR_DLSS_LIB
    SR_MAIN_LIB
    nvsdk_ngx
    ${Vulkan_LIBRARIES}
    dl
    pthread
)
