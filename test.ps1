Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host " Personal Finance Tracker - Automated Verification Suite" -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

& java -cp "bin;lib/mysql-connector-j-8.4.0.jar;resources" app.ApplicationTestSuite
