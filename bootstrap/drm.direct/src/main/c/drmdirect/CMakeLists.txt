project (drmdirect C)

cmake_minimum_required (VERSION 2.8)
set(CMAKE_BUILD_TYPE Release)
set(CMAKE_C_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -Wall")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Wall")

# The version number.
set (VERSION_MAJOR \"1\")
set (VERSION_MINOR \"0\")
set (VERSION_PATCH \"0\")
set (VERSION_EXT \"SNAPSHOT\")

# configure a header file to pass some of the CMake settings
# to the source code
configure_file (
    "${PROJECT_SOURCE_DIR}/Config.h.in"
    "${PROJECT_BINARY_DIR}/Config.h"
    )
# add the binary tree to the search path for include files
# so that we will find Config.h
include_directories("${PROJECT_BINARY_DIR}")

find_package(JNI REQUIRED)

# generate drmdirect
include_directories(${JNI_INCLUDE_DIRS})
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2})
add_executable(drmdirect src/drmdirect.c)
target_link_libraries(drmdirect ${JAVA_JVM_LIBRARY})
