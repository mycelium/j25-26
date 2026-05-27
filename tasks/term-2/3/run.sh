#!/bin/bash
# run.sh — exécute le load test

set -e

# Trouver java dynamiquement
JAVA=$(which java 2>/dev/null)
if [ -z "$JAVA" ]; then
    # Chercher Java dans les emplacements communs Windows
    if [ -f "/c/Program Files/Java/jdk-25.0.2/bin/java" ]; then
        JAVA="/c/Program Files/Java/jdk-25.0.2/bin/java"
    elif [ -f "/c/Program Files/Java/jdk-21/bin/java" ]; then
        JAVA="/c/Program Files/Java/jdk-21/bin/java"
    else
        echo "Error: Java not found"
        exit 1
    fi
fi

echo "Running with $JAVA..."

# Paramètres par défaut
THREADS=${THREADS:-20}
REQUESTS=${REQUESTS:-1000}
HOST=${HOST:-127.0.0.1}
PORT=${PORT:-8080}

# Construire le classpath
CP="out"
if [ -f "lib/gson.jar" ]; then
    CP="out;lib/gson.jar"  # Utiliser ; sur Windows, : sur Linux/Mac
    echo "Gson found"
fi

# Exécuter
"$JAVA" -cp "$CP" \
    -Dthreads=$THREADS \
    -Drequests=$REQUESTS \
    -Dhost=$HOST \
    -Dport=$PORT \
    loadtest.LoadTestRunner