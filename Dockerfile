FROM runmymind/docker-android-sdk
RUN apt-get update
RUN apt-get install -y git rubygems
RUN gem install bundler

ADD . app
RUN cd app && ./gradlew clean build install -PlocalVersion=9000.0.0-test
RUN cd app/dexguard-test-project && bundle install

#RUN cd app/dexguard-test-project && bundle exec bugsnag-maze-runner
#RUN mazerunner
