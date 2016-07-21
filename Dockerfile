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
RUN mkdir -p /usr/src/easynxc
COPY . /usr/src/easynxc
WORKDIR /usr/src/easynxc
RUN lein uberjar

# Start easynxc
WORKDIR /
EXPOSE 8080
CMD ["java", "-jar", "/usr/src/easynxc/target/easynxc.jar"]
