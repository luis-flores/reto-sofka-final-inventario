FROM openjdk:17
WORKDIR /app
COPY build/libs/inventory-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]