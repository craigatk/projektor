# Builds the Kotlin/Ktor backend and the React UI, then packages the UI into the server's
# static resources and produces the runnable fat jar (mirrors `.github/workflows/release-server.yml`).
FROM eclipse-temurin:21-jdk AS build

# Node.js + a matching Yarn Classic: `ui/build.gradle`'s YarnTask only runs `yarn build`, it does
# not install dependencies itself, so `yarn install` has to happen before the Gradle build (same
# as the "ui install" step in release-server.yml).
ENV NODE_MAJOR=24
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates gnupg \
    && curl -fsSL https://deb.nodesource.com/setup_${NODE_MAJOR}.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && npm install -g yarn@1.22.22 \
    && rm -rf /var/lib/apt/lists/*

# Optional: bakes a Honeycomb-configured OpenTelemetry javaagent jar into the build (same
# `com.atkinsondev.opentelemetry-build` plugin the Heroku Procfile relies on - see root
# build.gradle's `openTelemetryBuild` block). Pass with `docker build --build-arg
# HONEYCOMB_API_KEY=...`. If omitted, the plugin disables itself and the app just runs without
# the agent (handled by docker-entrypoint.sh below).
ARG HONEYCOMB_API_KEY
ENV HONEYCOMB_API_KEY=${HONEYCOMB_API_KEY}

WORKDIR /app
COPY . .

RUN chmod +x gradlew \
    && cd ui && yarn install --frozen-lockfile && cd .. \
    && ./gradlew :server:server-app:assembleFull --no-daemon \
    && mkdir -p server/server-app/opentelemetry

# ---- Runtime image: just the JRE, the fat jar, and (if built) the OpenTelemetry javaagent ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /opt/app
COPY --from=build /app/server/server-app/build/libs/server-app-1.0-all.jar app.jar
COPY --from=build /app/server/server-app/opentelemetry/ ./opentelemetry/
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["./docker-entrypoint.sh"]
