FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.2_8-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-Xms32m","-Xmx96m","-XX:+UseSerialGC","-XX:ActiveProcessorCount=1","-jar","/app/app.jar"]