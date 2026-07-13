#!/bin/sh
set -e

# Mirrors the Heroku Procfile's JVM flags: run with the OpenTelemetry javaagent when it was baked
# into the image at build time (HONEYCOMB_API_KEY build arg supplied), otherwise run plain.
if [ -f "./opentelemetry/opentelemetry-javaagent.jar" ]; then
    exec java \
        -javaagent:./opentelemetry/opentelemetry-javaagent.jar \
        --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED \
        --add-opens=java.base/java.net=ALL-UNNAMED \
        -jar app.jar
else
    exec java -jar app.jar
fi
