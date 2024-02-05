FROM eclipse-temurin:17-jre-jammy
WORKDIR /opt/app
COPY target/*.jar /app/*.jar
COPY src/main/resources/Mock_data.csv /opt/app/data/Mock_data.csv
ENTRYPOINT ["java", "-jar", "/app/*.jar"]