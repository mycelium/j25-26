#!/bin/bash
# compile.sh — compile all sources into out/
set -e

# Detect OS and set classpath separator
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]] || [[ -n "$WINDIR" ]]; then
    CP_SEP=";"
else
    CP_SEP=":"
fi

# Find javac
JAVAC=$(which javac 2>/dev/null)
if [ -z "$JAVAC" ]; then
    # Windows fallback with quoted path
    JAVAC="/c/Program Files/Java/jdk-25.0.2/bin/javac"
    if [ ! -f "$JAVAC" ]; then
        echo "Error: javac not found"
        exit 1
    fi
fi

echo "Compiling Lab 3 with $JAVAC..."
rm -rf out && mkdir -p out

CP="out"
[ -f "lib/gson.jar" ] && CP="out${CP_SEP}lib/gson.jar" && echo "  Gson found"

SOURCES=$(find src -name "*.java" | tr '\n' ' ')
"$JAVAC" --release 21 -cp "$CP" -d out $SOURCES

echo "  OK — $(find out -name '*.class' | wc -l) class files"