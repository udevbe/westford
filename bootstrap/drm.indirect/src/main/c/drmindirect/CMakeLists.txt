project (drmindirect C)

cmake_minimum_required (VERSION 2.8)
set(CMAKE_BUILD_TYPE Release)

set(CMAKE_C_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -Wall")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Wall")

find_package(JNI REQUIRED)

# generate drmindirect
include_directories(${JNI_INCLUDE_DIRS})
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2})
add_executable(drmindirect src/drmindirect.c)
target_link_libraries(drmindirect ${JAVA_JVM_LIBRARY})
