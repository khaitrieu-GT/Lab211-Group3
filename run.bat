@echo off
REM Bien dich va chay chuong trinh (Windows). Chay tu thu muc goc du an.
cd /d "%~dp0"
if exist build\classes rmdir /s /q build\classes
mkdir build\classes
dir /s /b src\*.java > build\sources.txt
javac -encoding UTF-8 -d build\classes @build\sources.txt || exit /b 1
java -cp build\classes Main
