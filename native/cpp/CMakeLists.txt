cmake_minimum_required(VERSION 3.15)
project(SuperResolution)

option(SR_FSR "启用以支持FSR" ON)
option(SR_DLSS "启用以支持DLSS" ON)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Debug CACHE STRING "构建类型" FORCE)
endif()

if(NOT (CMAKE_BUILD_TYPE STREQUAL "Debug" OR CMAKE_BUILD_TYPE STREQUAL "Release"))
    message(FATAL_ERROR "只支持 Debug 或 Release 构建类型)")
endif()

message(STATUS "构建类型: ${CMAKE_BUILD_TYPE}")
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(SR_BUILD_TYPE "debug")
else()
    set(SR_BUILD_TYPE "release")
endif()

if(NOT DEFINED SRLIB_VERSION)
    string(TIMESTAMP COMPILE_TIME "%Y%m%d_%H%M%S")
    set(SRLIB_VERSION ${COMPILE_TIME})
endif()

set(LIB_PLATFORM "linux64")
set(ON_LINUX ON)

foreach(OUTPUTCONFIG DEBUG RELEASE RELWITHDEBINFO MINSIZEREL)
    string(TOUPPER ${OUTPUTCONFIG} OUTPUTCONFIG_UPPER)
    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_${OUTPUTCONFIG_UPPER} "${PROJECT_SOURCE_DIR}/output/bin")
    set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_${OUTPUTCONFIG_UPPER} "${PROJECT_SOURCE_DIR}/output/lib")
    set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY_${OUTPUTCONFIG_UPPER} "${PROJECT_SOURCE_DIR}/output/lib")
endforeach()

add_compile_options(-Wall)
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    add_compile_options(-O3 -g1)
elseif(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_compile_options(-O3 -g0 -ffunction-sections -fdata-sections -flto)
    add_link_options(-fuse-ld=lld -flto -Wl,--gc-sections -s)
endif()

include_directories(
    ${PROJECT_SOURCE_DIR}/third_party
)

add_subdirectory("SRNativeMain")
if(SR_FSR)
    add_subdirectory("SRNativeFSR")
endif()
if(SR_DLSS)
    add_subdirectory("SRNativeDLSS")
endif()
