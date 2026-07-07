rm -r -Force buildWindows
cmake -S . -B buildWindows -DCMAKE_BUILD_TYPE=Debug -DSR_DLSS=ON
cmake --build buildWindows --config Debug

rm -r -Force buildWindows
cmake -S . -B buildWindows -DCMAKE_BUILD_TYPE=Release -DSR_DLSS=ON
cmake --build buildWindows --config Release
