#!/bin/sh

export OTEL_TRACES_EXPORTER=otlp
export OTEL_METRICS_EXPORTER=none
export OTEL_EXPORTER_OTLP_ENDPOINT=https://api.honeycomb.io
export OTEL_EXPORTER_OTLP_HEADERS=x-honeycomb-team=$APIKEY,x-honeycomb-dataset=projektorlocal
export OTEL_RESOURCE_ATTRIBUTES=service.name=projektor-server
export OTEL_INSTRUMENTATION_COMMON_DB_STATEMENT_SANITIZER_ENABLED=false

cd ..

../../gradlew stage
java -javaagent:opentelemetry/opentelemetry-javaagent.jar -jar build/libs/server-app-1.0-all.jar
