@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo =====================================================================
echo    BIEN DICH VA CHAY DU AN: UNDERSTANDING JDBC CORE (BAN CHAT JDBC)
echo =====================================================================

cd /d "%~dp0"

if not exist "bin" mkdir "bin"

echo [1/2] Dang bien dich cac file Java voi thu vien JDBC...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt

if %errorlevel% neq 0 (
    echo [LOI] Bien dich that bai! Vui long kiem tra JDK 17+.
    pause
    exit /b %errorlevel%
)

echo [2/2] Bien dich thanh cong! Dang khoi chay chuong trinh...
echo.

if "%1"=="" (
    java -Dfile.encoding=UTF-8 -cp "bin;lib/*" Main
) else (
    java -Dfile.encoding=UTF-8 -cp "bin;lib/*" Main %1
)

pause
