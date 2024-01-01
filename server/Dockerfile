FROM gradle:7.4.2-jdk11-alpine AS build

WORKDIR /home/gradle/src

COPY . .

RUN chown -R gradle:gradle ./ && gradle server:fatJar --no-daemon

FROM eclipse-temurin:11-alpine AS  run

RUN addgroup java && adduser java -DHG java

WORKDIR /app

COPY --from=build /home/gradle/src/server/build/libs/chain-pass-server.jar ./

RUN mkdir ./db && chown -R java:java /app

USER java

EXPOSE 8080
EXPOSE 8888/udp

ENTRYPOINT ["java", "-jar", "chain-pass-server.jar"]