@echo off
echo ===================================================================
echo  Personal Finance Tracker - Test Suite Execution
echo ===================================================================

java -cp "bin;lib/mysql-connector-j-8.4.0.jar;resources" app.ApplicationTestSuite
