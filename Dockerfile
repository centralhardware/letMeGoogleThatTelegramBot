FROM gradle:jdk21-graal as gradle

COPY ./ ./

RUN gradle fatJar

FROM findepi/graalvm:java21

WORKDIR /znatokiBot

COPY --from=gradle /home/gradle/build/libs/letMeGoogleThatForYou-1.0-SNAPSHOT-standalone.jar .

CMD ["java", "-jar", "letMeGoogleThatForYou-1.0-SNAPSHOT-standalone.jar" ]


