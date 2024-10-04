FROM gradle:jdk21-graal as gradle

COPY ./ ./

RUN gradle fatJar

FROM findepi/graalvm:java21

WORKDIR /znatokiBot

COPY --from=gradle /home/gradle/build/libs/letMeGoogleThatForYou-1.0-SNAPSHOT-standalone.jar .

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
  CMD curl --fail http://localhost:80/health || exit 1

CMD ["java", "-jar", "letMeGoogleThatForYou-1.0-SNAPSHOT-standalone.jar" ]