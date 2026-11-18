@echo off
echo ===================================================================
echo  Personal Finance Tracker - Desktop Application Launcher
echo ===================================================================

if not exist bin (
    echo Binaries not found. Triggering build first...
    call build.bat
)

echo Launching application...
java -cp "bin;lib/mysql-connector-j-8.4.0.jar;resources" app.Main
