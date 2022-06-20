FROM openjdk:11

ARG version
ARG buildDir

ENV VERSION=$version
ENV BUILD_DIR=$buildDir

RUN mkdir -p /app/db

COPY $BUILD_DIR/chain-pass-server-$VERSION.jar /app

WORKDIR /app

ENTRYPOINT java -jar chain-pass-server-$VERSION.jar