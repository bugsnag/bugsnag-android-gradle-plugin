FROM openjdk
WORKDIR /app
ADD . /app
RUN ./gradlew clean build install -PlocalVersion=9000.0.0-test
RUN git clone https://github.com/bugsnag/dexguard-test-project.git
RUN cd dexguard-test-project
RUN mazerunner
