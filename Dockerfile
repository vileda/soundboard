FROM jboss/wildfly

ADD target/soundboard.war /opt/jboss/wildfly/standalone/deployments/soundboard.war