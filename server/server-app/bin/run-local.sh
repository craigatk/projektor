#!/bin/sh

cd ..

../../gradlew stage
java -jar build/libs/server-app-1.0-all.jar
