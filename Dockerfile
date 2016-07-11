FROM java
MAINTAINER Parker Lawrence "parker.alford@gmail.com"

RUN apt-get update
ADD target/easynxc.jar /srv/easynxc.jar
EXPOSE 8080
CMD ["java", "-jar", "/srv/easynxc.jar"]
