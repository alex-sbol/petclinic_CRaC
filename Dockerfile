# Dockerfile
FROM azul/zulu-openjdk:21-jdk-crac-latest

USER root
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build/libs/spring-petclinic-4.0.0-SNAPSHOT.jar /app/app.jar
COPY scripts/*.sh /app/

RUN chmod +x /app/*.sh

# Persist checkpoints between runs
VOLUME /checkpoints
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
