FROM openjdk:11

RUN mkdir -p /app/db

COPY server/build/libs/chain-pass-server.jar /app

WORKDIR /app

CMD ["java", "-server", "-jar", "chain-pass-server.jar"]
