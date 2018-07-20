FROM runmymind/docker-android-sdk
RUN apt-get update
RUN apt-get install -y git rubygems
RUN gem install bundler

ADD . bugsnag-android-gradle-plugin
RUN cd bugsnag-android-gradle-plugin && ./gradlew clean build install -PlocalVersion=9000.0.0-test
RUN cd bugsnag-android-gradle-plugin/dexguard-test-project && bundle install

RUN cd bugsnag-android-gradle-plugin/dexguard-test-project && bundle exec bugsnag-maze-runner --verbose
#RUN mazerunner
