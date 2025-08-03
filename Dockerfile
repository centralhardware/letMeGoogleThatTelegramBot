FROM gradle:jdk24-graal as gradle

COPY ./ ./

RUN gradle installDist

FROM openjdk:24-slim

WORKDIR /app

COPY --from=gradle /home/gradle/build/install/letMeGoogleThatForYou/ .

CMD ["bin/letMeGoogleThatForYou"]
