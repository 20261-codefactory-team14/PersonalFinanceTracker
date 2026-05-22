FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/FinanceTracker-0.0.1-SNAPSHOT.jar app.jar
ENV TZ=America/Bogota
RUN useradd -ms /bin/bash springuser
USER springuser
ENTRYPOINT ["java", "-jar", "app.jar"]
