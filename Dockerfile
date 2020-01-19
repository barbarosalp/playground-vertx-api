FROM openjdk:8-jre-alpine
EXPOSE 8080
RUN mkdir -p /opt/application
WORKDIR /opt/application
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-server", "-jar", "application.jar"]
COPY target/vertx-api-1.0-SNAPSHOT-fat.jar /opt/application/application.jar
