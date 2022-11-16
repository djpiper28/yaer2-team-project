#!/bin/bash

mkdir build
cd build
echo "Init Make Files"
rm - r *
cmake .. -DCMAKE_BUILD_TYPE=DEBUG

echo "Building..."
cmake --build . -j

