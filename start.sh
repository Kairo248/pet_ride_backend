#!/bin/bash
set -e

# Try to find the JAR file
JAR_FILE="target/backend-0.0.1-SNAPSHOT.jar"

# If exact name doesn't exist, find any JAR in target/
if [ ! -f "$JAR_FILE" ]; then
    echo "⚠️  Expected JAR not found. Searching for JAR files..."
    FOUND_JAR=$(find target -name "*.jar" -type f | grep -v "original" | head -1)
    
    if [ -n "$FOUND_JAR" ]; then
        echo "✅ Found JAR: $FOUND_JAR"
        JAR_FILE="$FOUND_JAR"
    else
        echo "❌ No JAR file found. Building..."
        chmod +x build.sh
        ./build.sh
        JAR_FILE="target/backend-0.0.1-SNAPSHOT.jar"
    fi
fi

# Final check
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR file still not found: $JAR_FILE"
    echo "📋 Contents of target directory:"
    ls -la target/ || echo "Target directory doesn't exist"
    exit 1
fi

echo "🚀 Starting Spring Boot application with: $JAR_FILE"
java -jar "$JAR_FILE"

