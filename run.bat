@echo off
REM Bien dich va chay chuong trinh (Windows). Chay tu thu muc goc du an.
REM Demo giu ghe 1 phut:  set JAVA_OPTS=-Dhold.minutes=1  roi chay run.bat
cd /d "%~dp0"
if exist build\classes rmdir /s /q build\classes
mkdir build\classes
dir /s /b src\*.java > build\sources.txt
javac -encoding UTF-8 -d build\classes @build\sources.txt || exit /b 1
java %JAVA_OPTS% -cp build\classes Main
