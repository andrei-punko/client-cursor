FROM openjdk:17-jdk-alpine
EXPOSE 9080
ADD target/client-cursor-0.0.1-SNAPSHOT.jar /usr/local/app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/usr/local/app.jar"]
