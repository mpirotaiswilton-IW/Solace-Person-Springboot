FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
COPY target/*.jar /app/*.jar
ENTRYPOINT ["java", "-jar", "/app/*.jar"]docker