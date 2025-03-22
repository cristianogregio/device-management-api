FROM maven:3.9.6-openjdk-22 AS builder
WORKDIR /device
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:22-jre
WORKDIR /device
COPY --from=builder /device/target/*.jar device_manage.jar

EXPOSE 8080
CMD ["java", "-jar", "device_manage.jar"]
