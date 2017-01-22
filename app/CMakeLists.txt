# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(src/main/cpp/lua)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/native-lib.cpp )

add_library( # Sets the name of the library.
             lua-lib

             # Sets the library as a static library.
             STATIC

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             src/main/cpp/lua/lapi.c
             src/main/cpp/lua/lauxlib.c
             src/main/cpp/lua/lbaselib.c
             src/main/cpp/lua/lbitlib.c
             src/main/cpp/lua/lcode.c
             src/main/cpp/lua/lcorolib.c
             src/main/cpp/lua/lctype.c
             src/main/cpp/lua/ldblib.c
             src/main/cpp/lua/ldebug.c
             src/main/cpp/lua/ldo.c
             src/main/cpp/lua/ldump.c
             src/main/cpp/lua/lfunc.c
             src/main/cpp/lua/lgc.c
             src/main/cpp/lua/linit.c
             src/main/cpp/lua/liolib.c
             src/main/cpp/lua/llex.c
             src/main/cpp/lua/lmathlib.c
             src/main/cpp/lua/lmem.c
             src/main/cpp/lua/loadlib.c
             src/main/cpp/lua/lobject.c
             src/main/cpp/lua/lopcodes.c
             src/main/cpp/lua/loslib.c
             src/main/cpp/lua/lparser.c
             src/main/cpp/lua/lstate.c
             src/main/cpp/lua/lstring.c
             src/main/cpp/lua/lstrlib.c
             src/main/cpp/lua/ltable.c
             src/main/cpp/lua/ltablib.c
             src/main/cpp/lua/ltm.c
             src/main/cpp/lua/lua.c
             src/main/cpp/lua/luac.c
             src/main/cpp/lua/lundump.c
             src/main/cpp/lua/lutf8lib.c
             src/main/cpp/lua/lvm.c
             src/main/cpp/lua/lzio.c )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       lua-lib
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
