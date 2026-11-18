Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host " Personal Finance Tracker - PowerShell Build Script" -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sourceFiles = (Get-ChildItem -Path "src" -Recurse -Filter "*.java").FullName
Write-Host "Compiling $($sourceFiles.Count) Java source files..." -ForegroundColor Yellow

& javac -cp "lib/mysql-connector-j-8.4.0.jar;src" -d bin -encoding UTF-8 $sourceFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[SUCCESS] All source files compiled successfully into bin/" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
}
