FROM openjdk:11

RUN mkdir -p /app

COPY service/build/libs/chain-pass-service.jar /app

WORKDIR /app

CMD ["java", "-server", "-jar", "chain-pass-service.jar"]
