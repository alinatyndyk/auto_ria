FROM openjdk:17-alpine

MAINTAINER Alina Tyndyk

RUN apk add bash

RUN mkdir -p $HOME/springboot-lib
RUN mkdir /app
WORKDIR /app

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8080
EXPOSE 5678
ENTRYPOINT ["java", "--add-opens", "java.base/java.net=ALL-UNNAMED", "-jar", "app.jar"]