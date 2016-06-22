FROM maven:3-jdk-8

ADD . $MAVEN_HOME

RUN cd $MAVEN_HOME \
 && mvn clean package -Dmaven.test.skip \
 && mv $MAVEN_HOME/target /oidc-proxy \
 && rm -r $MAVEN_HOME

ENTRYPOINT ["sh", "/oidc-proxy/bin/run.sh"]