@echo off
echo ===================================================================
echo  Personal Finance Tracker - Desktop Application Launcher
echo ===================================================================

if not exist bin (
    echo Binaries not found. Triggering build first...
    call build.bat
)

set JVM_OPTS=
if not "%~1"=="" (
    echo Administrator Bootstrap Token parameter detected.
    set JVM_OPTS=-DADMIN_BOOTSTRAP_TOKEN=%~1
) else if not "%ADMIN_BOOTSTRAP_TOKEN%"=="" (
    echo Administrator Bootstrap Token environment variable detected.
    set JVM_OPTS=-DADMIN_BOOTSTRAP_TOKEN=%ADMIN_BOOTSTRAP_TOKEN%
)

echo Launching application...
java %JVM_OPTS% -cp "bin;lib/mysql-connector-j-8.4.0.jar;resources" app.Main
