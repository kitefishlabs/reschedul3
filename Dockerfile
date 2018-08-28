FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/sand-compass.jar /sand-compass/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sand-compass/app.jar"]
