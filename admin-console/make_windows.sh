#!/bin/bash

mkdir build
cd build
echo "Init Make Files"
rm -r *
cmake .. -DCMAKE_BUILD_TYPE=DEBUG -DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc -D CMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++ -DCMAKE_SYSTEM_NAME=Windows

echo "Building..."
cmake --build . -j

