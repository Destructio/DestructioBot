FROM openjdk:15
COPY /build/libs/ /tmp
WORKDIR /tmp
CMD java -jar DestructioBot-1.3.1-all.jar ##API key