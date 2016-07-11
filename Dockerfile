FROM clojure
MAINTAINER Parker Lawrence "parker.alford@gmail.com"
RUN apt-get update

RUN mkdir -p /usr/src/easynxc
WORKDIR /usr/src/easynxc
COPY project.clj /usr/src/easynxc/
RUN lein deps
COPY . /usr/src/easynxc
RUN lein uberjar
EXPOSE 8080
CMD ["java", "-jar", "target/easynxc.jar"]
