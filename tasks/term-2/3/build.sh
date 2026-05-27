#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Lab 3 — Build script
# ─────────────────────────────────────────────────────────────────────────────
set -e

JAVA_VERSION=21
SRC=src
OUT=bin
LIB=lib

echo "── Checking Java version ──────────────────────────────────────"
java -version 2>&1 | head -1

# Vérifier que les jars sont présents
if [ ! -s "$LIB/sqlite-jdbc.jar" ]; then
  echo "ERROR: $LIB/sqlite-jdbc.jar is missing or empty."
  echo "Download: https://github.com/xerial/sqlite-jdbc/releases/download/3.45.3.0/sqlite-jdbc-3.45.3.0.jar"
  exit 1
fi

if [ ! -s "$LIB/gson.jar" ]; then
  echo "ERROR: $LIB/gson.jar is missing or empty."
  echo "Download: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
  exit 1
fi

CP="$LIB/sqlite-jdbc.jar:$LIB/gson.jar"

echo "── Compiling ──────────────────────────────────────────────────"
mkdir -p $OUT
find $SRC -name "*.java" > sources.txt
javac --release $JAVA_VERSION -cp "$CP" -d $OUT @sources.txt
rm sources.txt

echo "── Build OK ───────────────────────────────────────────────────"
echo ""
echo "Run with:"
echo "  java -cp \"$OUT:$CP\" Main [port] [totalRequests] [concurrentClients] [threadCount]"
echo ""
echo "Example (defaults):"
echo "  java -cp \"$OUT:$CP\" Main 8080 500 20 8"
