#!/usr/bin/env bash
# Build and run City Taxi 3D.
# Usage: ./run.sh
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v mvn >/dev/null 2>&1; then
    echo "ERROR: Maven (mvn) is not on your PATH."
    echo "  Install from https://maven.apache.org/download.cgi"
    echo "  macOS : brew install maven"
    echo "  Ubuntu: sudo apt install maven"
    exit 1
fi

if ! command -v java >/dev/null 2>&1; then
    echo "ERROR: java is not on your PATH. Install JDK 17 or newer."
    exit 1
fi

echo ">>> Building the game..."
mvn -q package

echo ">>> Launching the game..."
# --enable-native-access=ALL-UNNAMED silences LWJGL's restricted-method warnings on JDK 17+
# --enable-preview is NOT needed - just the native access flag
java --enable-native-access=ALL-UNNAMED -jar target/city-taxi-3d.jar
