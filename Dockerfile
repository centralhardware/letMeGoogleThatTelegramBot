FROM maven:3.9.4-amazoncorretto-21 as maven

COPY ./ ./

RUN mvn package

FROM openjdk:21-slim

WORKDIR /znatokiBot

COPY --from=maven target/letMeGoogleThatForYou-1.0-SNAPSHOT.jar .

RUN apt update -y && \
    apt upgrade -y

ENV TZ Asia/Novosibirsk

CMD ["java", "-jar", "letMeGoogleThatForYou-1.0-SNAPSHOT.jar" ]