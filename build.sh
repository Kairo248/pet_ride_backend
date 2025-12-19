#!/bin/bash
set -e

echo "🔨 Building Spring Boot application..."

# Build the project
mvn clean package -DskipTests

# Check if JAR file exists
if [ ! -f "target/backend-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ Error: JAR file not found after build!"
    echo "📋 Listing target directory:"
    ls -la target/ || echo "Target directory doesn't exist"
    exit 1
fi

echo "✅ Build successful! JAR file created:"
ls -lh target/backend-0.0.1-SNAPSHOT.jar

