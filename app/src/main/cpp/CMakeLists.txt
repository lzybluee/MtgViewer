# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("native-lib")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(lua)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

add_library( # Sets the name of the library.
        lua-lib

        # Sets the library as a static library.
        STATIC

        # Provides a relative path to your source file(s).
        lua/lapi.c
        lua/lauxlib.c
        lua/lbaselib.c
        lua/lcode.c
        lua/lcorolib.c
        lua/lctype.c
        lua/ldblib.c
        lua/ldebug.c
        lua/ldo.c
        lua/ldump.c
        lua/lfunc.c
        lua/lgc.c
        lua/linit.c
        lua/liolib.c
        lua/llex.c
        lua/lmathlib.c
        lua/lmem.c
        lua/loadlib.c
        lua/lobject.c
        lua/lopcodes.c
        lua/loslib.c
        lua/lparser.c
        lua/lstate.c
        lua/lstring.c
        lua/lstrlib.c
        lua/ltable.c
        lua/ltablib.c
        lua/ltm.c
        lua/lua.c
        lua/luac.c
        lua/lundump.c
        lua/lutf8lib.c
        lua/lvm.c
        lua/lzio.c )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib
        lua-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )
