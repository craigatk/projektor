# syntax=docker/dockerfile:1
# Builds the Kotlin/Ktor backend and the React UI, then packages the UI into the server's
# static resources and produces the runnable fat jar (mirrors `.github/workflows/release-server.yml`).
# Built and pushed to GitHub Container Registry by `.github/workflows/deploy-digitalocean.yml`;
# DO App Platform runs the pushed image rather than building from source.
FROM eclipse-temurin:25-jdk AS build

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

WORKDIR /app
COPY . .

# Optional: bakes a Honeycomb-configured OpenTelemetry javaagent jar into the build (the
# `com.atkinsondev.opentelemetry-build` plugin - see root build.gradle's `openTelemetryBuild`
# block). Passed as a BuildKit secret so the key never lands in an image layer or the build cache:
# `docker build --secret id=honeycomb_api_key,env=HONEYCOMB_API_KEY .`. If omitted, the plugin
# disables itself and the app just runs without the agent (handled by docker-entrypoint.sh).
# The optional cache_access_key/cache_secret_key secrets enable read access to the Gradle remote
# build cache (settings.gradle) the same way.
RUN --mount=type=secret,id=honeycomb_api_key,env=HONEYCOMB_API_KEY \
    --mount=type=secret,id=cache_access_key,env=CACHE_ACCESS_KEY \
    --mount=type=secret,id=cache_secret_key,env=CACHE_SECRET_KEY \
    chmod +x gradlew \
    && cd ui && yarn install --frozen-lockfile && cd .. \
    && ./gradlew :server:server-app:assembleFull --no-daemon \
    && mkdir -p server/server-app/opentelemetry

# ---- Runtime image: just the JRE, the fat jar, and (if built) the OpenTelemetry javaagent ----
FROM eclipse-temurin:25-jre-jammy

WORKDIR /opt/app
COPY --from=build /app/server/server-app/build/libs/server-app-1.0-all.jar app.jar
COPY --from=build /app/server/server-app/opentelemetry/ ./opentelemetry/
COPY docker-entrypoint.sh .
RUN chmod +x docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["./docker-entrypoint.sh"]
