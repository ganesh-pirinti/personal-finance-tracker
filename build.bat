@echo off
echo ===================================================================
echo  Personal Finance Tracker - Compilation Build Script
echo ===================================================================

if not exist bin mkdir bin

echo Compiling Java source files...
javac -cp "lib/mysql-connector-j-8.4.0.jar;src" -d bin -encoding UTF-8 src/util/*.java src/model/*.java src/database/*.java src/dao/*.java src/service/*.java src/ui/components/*.java src/ui/*.java src/ui/admin/*.java src/app/*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed without errors. Binaries placed in bin/
) else (
    echo [ERROR] Compilation failed. Please inspect error logs above.
)
