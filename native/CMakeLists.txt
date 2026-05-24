cmake_minimum_required(VERSION 3.10)
project(FastHardware VERSION 0.1.0)

# Check if building on Windows
if(NOT WIN32)
    message(FATAL_ERROR "FastHardware is a Windows-only module.")
endif()

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED True)

# Set JNI headers
find_package(JNI REQUIRED)

# Include directories
include_directories(${JNI_INCLUDE_DIRS} include)

# Source files
set(SOURCES
    src/fasthardware.cpp
    src/pdh_cpu.cpp
    src/wmi_temperature.cpp
    src/memory_info.cpp
)

# Build the DLL
add_library(fasthardware SHARED ${SOURCES})

# Link against required Windows libraries (PDH for CPU, WbemUuid/Ole32/OleAut32 for WMI)
target_link_libraries(fasthardware pdh wbemuuid ole32 oleaut32)

# Set output directory to Java resources folder
set_target_properties(fasthardware PROPERTIES 
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/../src/main/resources/native"
    RUNTIME_OUTPUT_DIRECTORY_RELEASE "${CMAKE_SOURCE_DIR}/../src/main/resources/native"
)
