Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host " Personal Finance Tracker - Desktop Application Launcher" -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    Write-Host "Binaries not found. Running build.ps1 first..." -ForegroundColor Yellow
    & .\build.ps1
}

Write-Host "Starting Java Swing & AWT Desktop Application..." -ForegroundColor Green
& java -cp "bin;lib/mysql-connector-j-8.4.0.jar;resources" app.Main
