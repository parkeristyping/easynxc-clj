FROM clojure

# Update packages
RUN apt-get update

# Install ffmpeg
RUN apt-get install -y ffmpeg

# Install Python.
RUN apt-get install -y python-pip

# Install youtube-dl
RUN pip install --upgrade youtube_dl

# Install easynxc
# RUN mkdir -p /usr/src/easynxc
# WORKDIR /usr/src/easynxc
# COPY project.clj /usr/src/easynxc/
# RUN lein deps
# COPY . /usr/src/easynxc
# RUN lein uberjar
# ADD target/easynxc.jar /srv/easynxc.jar

# Start easynxc
WORKDIR /
EXPOSE 8080
# CMD ["java", "-jar", "/srv/easynxc.jar"]
