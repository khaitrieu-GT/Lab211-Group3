#!/bin/sh
# Bien dich va chay chuong trinh (Linux/macOS/Git Bash). Chay tu thu muc goc du an.
cd "$(dirname "$0")" || exit 1
rm -rf build/classes
mkdir -p build/classes
javac -encoding UTF-8 -d build/classes $(find src -name "*.java") || exit 1
# Demo giu ghe 1 phut: JAVA_OPTS=-Dhold.minutes=1 ./run.sh
java $JAVA_OPTS -cp build/classes Main
