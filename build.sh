#!/usr/bin/env bash
# Rebuilds token-auto-refresher.jar from src/. No Maven/Gradle needed — just
# JDK 17+ (javac + jar). Downloads montoya-api and gson from Maven Central
# into lib/ on first run.
set -euo pipefail
cd "$(dirname "$0")"

MONTOYA_VERSION="2026.7"
GSON_VERSION="2.11.0"
MONTOYA_JAR="lib/montoya-api-${MONTOYA_VERSION}.jar"
GSON_JAR="lib/gson-${GSON_VERSION}.jar"

mkdir -p lib
if [ ! -f "$MONTOYA_JAR" ]; then
  curl -sf -o "$MONTOYA_JAR" "https://repo1.maven.org/maven2/net/portswigger/burp/extensions/montoya-api/${MONTOYA_VERSION}/montoya-api-${MONTOYA_VERSION}.jar"
fi
if [ ! -f "$GSON_JAR" ]; then
  curl -sf -o "$GSON_JAR" "https://repo1.maven.org/maven2/com/google/code/gson/gson/${GSON_VERSION}/gson-${GSON_VERSION}.jar"
fi

rm -rf build/classes build/fatjar dist
mkdir -p build/classes build/fatjar dist

find src -name '*.java' > /tmp/tr-sources.txt
javac -encoding UTF-8 -cp "$MONTOYA_JAR:$GSON_JAR" -d build/classes @/tmp/tr-sources.txt

cp -r build/classes/* build/fatjar/
(cd build/fatjar && jar xf "../../$GSON_JAR" com && rm -rf META-INF)

jar cfe dist/token-auto-refresher.jar tokenrefresher.TokenRefresherExtension -C build/fatjar .
echo "Built: dist/token-auto-refresher.jar"
