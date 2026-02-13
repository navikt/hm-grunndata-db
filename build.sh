#!/usr/bin/env bash
# workaround for testcontainer docker engine tull
echo api.version=1.44 >> ~/.docker-java.properties

./gradlew clean build --no-daemon