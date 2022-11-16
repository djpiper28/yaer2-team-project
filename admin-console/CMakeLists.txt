cmake_minimum_required(VERSION 3.5)

project(admin-console VERSION 0.1 LANGUAGES CXX)

#=========
# Version info
#=========
set(REPO_URL "https://github.com/RHUL-CS-Projects/TeamProject2022_05")

message(STATUS "Building ${CMAKE_PROJECT_NAME}")
message(STATUS "Git repo: ${REPO_URL}")
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "Version: ${CMAKE_PROJECT_VERSION}")
message(STATUS "Build OS: ${CMAKE_SYSTEM_NAME}")

# Get the branch name
execute_process(
  COMMAND git rev-parse --abbrev-ref HEAD
  WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
  OUTPUT_VARIABLE GIT_BRANCH
  OUTPUT_STRIP_TRAILING_WHITESPACE
)

# Get the latest abbreviated commit hash of the working branch
execute_process(
  COMMAND git log -1 --format=%h
  WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
  OUTPUT_VARIABLE GIT_COMMIT_HASH
  OUTPUT_STRIP_TRAILING_WHITESPACE
)

add_definitions(-DGIT_COMMIT_HASH="${GIT_COMMIT_HASH}")
add_definitions(-DGIT_BRANCH="${GIT_BRANCH}")

# compiler flags
set(COMPILER_FLAGS "-Wall -Wextra -Wpedantic")
add_definitions("-DVERSION=\"${CMAKE_PROJECT_VERSION} (${GIT_COMMIT_HASH}@${GIT_BRANCH})\"")
add_definitions("-DOS=\"${CMAKE_SYSTEM_NAME}\"")
add_definitions("-DREPO_URL=\"${REPO_URL}\"")

message(STATUS "Commit info: (${GIT_COMMIT_HASH}@${GIT_BRANCH})")

#=========
# Build script
#=========
set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOUIC ON)
set(CMAKE_AUTOMOC ON)
set(CMAKE_AUTORCC ON)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

find_package(QT NAMES Qt6 Qt5 COMPONENTS Widgets Network LinguistTools REQUIRED)
find_package(Qt${QT_VERSION_MAJOR} COMPONENTS Widgets Network LinguistTools REQUIRED)


### Installs Discord Game SDK binaries
set(DISCORD_GAME_SDK_PATH "${CMAKE_SOURCE_DIR}")
if (${CMAKE_SYSTEM_NAME} STREQUAL "Windows")
    set(CMAKE_DISCORD_GAME_SDK_LIB_PATH "${DISCORD_GAME_SDK_PATH}/lib/x86_64/discord_game_sdk.dll.lib")
elseif (${CMAKE_SYSTEM_NAME} STREQUAL "Linux")
    set(CMAKE_DISCORD_GAME_SDK_LIB_PATH "${DISCORD_GAME_SDK_PATH}/lib/x86_64/discord_game_sdk.so")
elseif (${CMAKE_SYSTEM_NAME} STREQUAL "Macos")
    set(CMAKE_DISCORD_GAME_SDK_LIB_PATH "${DISCORD_GAME_SDK_PATH}/lib/x86_64/discord_game_sdk.dylib")
endif ()
install(FILES ${CMAKE_DISCORD_GAME_SDK_LIB_PATH} DESTINATION lib/)
message(STATUS "Discord game sdk: ${CMAKE_DISCORD_GAME_SDK_LIB_PATH}")

#==========
# App stuff
#==========
set(TS_FILES admin-console_en_GB.ts)
set(PROJECT_SOURCES
        tests/test.h tests/test.cpp
        tests/testing.h tests/testing.cpp

        utils.h utils.cpp
        model/configuration.h model/configuration.cpp model/config_sql.cpp
        main.cpp

        ui/addinvitemdialogue.h ui/addinvitemdialogue.cpp ui/addinvitemdialogue.ui
        ui/editinventorymenu.h ui/editinventorymenu.cpp ui/editinventorymenu.ui
        ui/mainwindow.h ui/mainwindow.cpp ui/mainwindow.ui
        ui/additemdialogue.h ui/additemdialogue.cpp ui/additemdialogue.ui

        model/menuitem.h model/menuitem.cpp
        ui/menuitemtable.h ui/menuitemtable.cpp

        model/inventoryitem.h model/inventoryitem.cpp
        ui/invitemtable.h ui/invitemtable.cpp

        model/inventorymenuitem.h model/inventorymenuitem.cpp
        ui/invmenuitemtable.h ui/invmenuitemtable.cpp

        model/menutype.h model/menutype.cpp
        ui/menutypetable.h ui/menutypetable.cpp

        ui/addmenutypedialogue.h ui/addmenutypedialogue.cpp ui/addmenutypedialogue.ui

        model/user.h model/user.cpp
        ui/usertable.h ui/usertable.cpp

        ui/adduserdialogue.h ui/adduserdialogue.cpp ui/adduserdialogue.ui
        ui/changepassworddialogue.h ui/changepassworddialogue.cpp ui/changepassworddialogue.ui
        ui/checkpassworddialogue.h ui/checkpassworddialogue.cpp ui/checkpassworddialogue.ui

				security.h security.cpp
        ui/filteredlist.h
        discord_game_sdk.h
        ansi_colour.h
        ${TS_FILES}
)

if(${QT_VERSION_MAJOR} GREATER_EQUAL 6)
    qt_add_executable(admin-console
        MANUAL_FINALIZATION
        ${PROJECT_SOURCES}
    )
# Define target properties for Android with Qt 6 as:
#    set_property(TARGET admin-console APPEND PROPERTY QT_ANDROID_PACKAGE_SOURCE_DIR
#                 ${CMAKE_CURRENT_SOURCE_DIR}/android)
# For more information, see https://doc.qt.io/qt-6/qt-add-executable.html#target-creation
    qt_create_translation(QM_FILES ${CMAKE_SOURCE_DIR} ${TS_FILES})
else()
    if(ANDROID)
        add_library(admin-console SHARED
            ${PROJECT_SOURCES}
        )
# Define properties for Android with Qt 5 after find_package() calls as:
    set(ANDROID_PACKAGE_SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/android")
    else()
        add_executable(admin-console
            ${PROJECT_SOURCES}
        )
    endif()

    qt5_create_translation(QM_FILES ${CMAKE_SOURCE_DIR} ${TS_FILES})
endif()

set(PQXX pqxx pq)
set(QT Qt${QT_VERSION_MAJOR}::Widgets Qt${QT_VERSION_MAJOR}::Network)
target_link_libraries(admin-console PRIVATE ${QT} ${PQXX} crypto)

# Link discord
add_library(discord STATIC IMPORTED)
set_target_properties(discord PROPERTIES IMPORTED_LOCATION ${CMAKE_DISCORD_GAME_SDK_LIB_PATH})
target_link_libraries(admin-console PUBLIC discord)

# Copy the icon
file(COPY ${CMAKE_CURRENT_SOURCE_DIR}/icon.png
     DESTINATION ${CMAKE_CURRENT_BINARY_DIR})
file(COPY ${CMAKE_DISCORD_GAME_SDK_LIB_PATH}
     DESTINATION ${CMAKE_CURRENT_BINARY_DIR})
# Finish qt app creation
set_target_properties(admin-console PROPERTIES
    MACOSX_BUNDLE_GUI_IDENTIFIER www.rhul-hack.com
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

if(QT_VERSION_MAJOR EQUAL 6)
    qt_finalize_executable(admin-console)
endif()
