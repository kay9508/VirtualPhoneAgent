FROM openjdk:11-jre-slim-buster

ENV TZ=Asia/Seoul

WORKDIR /usr/src/app

COPY ./build/libs/virtualphoneagent-0.0.1-SNAPSHOT.jar app.jar

RUN useradd tempUser
USER tempUser

ENTRYPOINT ["java", "-jar", "app.jar"]
