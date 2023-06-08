FROM maven:3.9.1-amazoncorretto-20 as maven

COPY ./ ./

RUN mvn package

FROM bitnami/java:20

WORKDIR /znatokiBot

COPY --from=maven target/letMeGoogleThatForYou-1.0-SNAPSHOT.jar .

RUN apt update -y && \
    apt upgrade -y && \
    apt install tzdata curl

ENV TZ Asia/Novosibirsk

CMD ["java", "-jar", "letMeGoogleThatForYou-1.0-SNAPSHOT.jar" ]