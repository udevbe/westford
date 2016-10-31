project (westford-launch C)

cmake_minimum_required (VERSION 2.8)
set(CMAKE_BUILD_TYPE Release)

set(CMAKE_C_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -Wall")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Wall")

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")

find_package(JNI REQUIRED)
find_package(PAM REQUIRED)

# generate drmindirect
include_directories(${JNI_INCLUDE_DIRS})
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2})
include_directories(${PAM_INCLUDE_DIR})
add_executable(westford-launch src/westford-launch.c src/westford-launch.h)
target_link_libraries(westford-launch ${JAVA_JVM_LIBRARY} ${PAM_LIBRARIES})
