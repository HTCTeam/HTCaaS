#!/bin/bash

if [[ -z $HTCaaS_Server ]]; then
  echo "environment HTCaaS_Server is not set"
  exit
fi

if [[ -z $1 ]]; then
  echo "Usage:"
  echo " Run ActiveMQ        : ./run.sh mq "
  echo " Run DB Manager      : ./run.sh db "
  echo " Run Job Manager     : ./run.sh jm "
  echo " Run Monitoring      : ./run.sh mn "
  echo " Run UD Manager      : ./run.sh ud "
  echo " Run Agent Manager   : ./run.sh am "
  echo " Run Account Manager : ./run.sh ac "
  echo " Run GridResourceManager VO_Name : ./run.sh gm (vo_name)"
  exit
fi


if [[ ! -z $1 && $1 == 'mq' ]]; then
  cd $HTCaaS_Server
  exec activeMQ/bin/activemq start 2>&1
fi

if [[ ! -z $1 && $1 == 'db' ]]; then
  cd $HTCaaS_Server

  echo $$ > $HTCaaS_Server/service/db.pid

  cmd="java "
  cmd=$cmd" -Dcxf.config.file=Server.xml"
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/DBManager"

  cmd=$cmd":lib/DBManager/commons-dbcp-1.4.jar"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
#  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/DBManager/commons-pool-1.6.jar"
  cmd=$cmd":lib/DBManager/mysql-connector-java-5.1.18-bin.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/geronimo-servlet_3.0_spec-1.0.jar"
  cmd=$cmd":lib/shared/jetty-continuation-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-http-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-io-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-server-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-util-8.1.15.v20140411.jar"
  cmd=$cmd":lib/DBManager/spring-context-2.5.6.jar"
  cmd=$cmd":lib/DBManager/spring-context-support-2.5.6.jar"
  cmd=$cmd":lib/DBManager/spring-beans-2.5.6.jar"
  cmd=$cmd":lib/DBManager/spring-core-2.5.6.jar"
#  cmd=$cmd":lib/DBManager/neethi-2.0.4.jar"
  cmd=$cmd":lib/shared/commons-logging-1.1.1.jar"
  
  cmd=$cmd" org.kisti.htc.dbmanager.server.Server"
  echo $cmd
  exec $cmd 2>&1
fi

if [[ ! -z $1 && $1 == 'jm' ]]; then
  cd $HTCaaS_Server
  #ant -f build_jm.xml run

  echo $$ > $HTCaaS_Server/service/jm.pid

  cmd="java "
  cmd=$cmd" -Xms256m -Xmx1024m"
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/JobManager"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
#  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/commons-net-3.2.jar"
  cmd=$cmd":lib/shared/jsdldoc.jar"
  cmd=$cmd":lib/shared/mail-1.4.3.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/geronimo-servlet_3.0_spec-1.0.jar"
  cmd=$cmd":lib/shared/jetty-continuation-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-http-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-io-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-server-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-util-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-jaxb-3.0.4.jar"

  cmd=$cmd" org.kisti.htc.jobmanager.server.Server"
  cmd=$cmd" -Dorg.apache.cxf.common.logging.Log4jLogger="
  echo $cmd
  exec $cmd 2>&1

fi

if [[ ! -z $1 && $1 == 'mn' ]]; then
  cd $HTCaaS_Server

  echo $$ > $HTCaaS_Server/service/mn.pid

  #ant -f build_monitoring.xml run
  cmd="java "
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/Monitoring"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
#  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/commons-net-3.2.jar"
  cmd=$cmd":lib/shared/jsdldoc.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/geronimo-servlet_3.0_spec-1.0.jar"
  cmd=$cmd":lib/shared/jetty-continuation-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-http-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-io-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-server-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-util-8.1.15.v20140411.jar"

  cmd=$cmd" org.kisti.htc.monitoring.server.Server"
  echo $cmd
  exec $cmd 2>&1
fi

if [[ ! -z $1 && $1 == 'ud' ]]; then
  cd $HTCaaS_Server

  echo $$ > $HTCaaS_Server/service/ud.pid

  # exec ant -f build_ud.xml run
  # udmanager
  mkdir -p /htcaas/tmp/

  cmd="java "
  cmd=$cmd" -verbosegc "
#  cmd=$cmd" -Dorg.apache.cxf.io.CachedOutputStream.Threshold=1024000"
  cmd=$cmd" -Dorg.apache.cxf.io.CachedOutputStream.OutputDirectory=/mnt/data_storage/htcaas/tmp"
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/UDManager"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
#  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/commons-net-3.2.jar"
  cmd=$cmd":lib/shared/jsdldoc.jar"
  cmd=$cmd":lib/shared/mail-1.4.3.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/geronimo-servlet_3.0_spec-1.0.jar"
  cmd=$cmd":lib/shared/jetty-continuation-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-http-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-io-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-server-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-util-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-jaxb-3.0.4.jar"

  cmd=$cmd" org.kisti.htc.udmanager.server.Server"
  cmd=$cmd
  echo $cmd
  exec $cmd 2>&1
fi

if [[ ! -z $1 && $1 == 'am' ]]; then
  cd $HTCaaS_Server

  echo $$ > $HTCaaS_Server/service/am.pid

  # agent
  cmd="java "
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/AgentManager"
  cmd=$cmd":lib/ACManager/commons-lang-2.6.jar"
  cmd=$cmd":lib/ACManager/bcprov-jdk16-146.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/AgentManager/jsch-0.1.44.jar"
  cmd=$cmd":lib/AgentManager/openstack4j-1.0.2.jar"
  cmd=$cmd":lib/AgentManager/openstack4j_11_25_jdk6_ver4_runable.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/cxf-rt-frontend-simple-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-bindings-soap-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-wsdl-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-aegis-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-jaxb-3.0.4.jar"
  cmd=$cmd":lib/AgentManager/aws-java-sdk-1.2.3.jar"


  cmd=$cmd" org.kisti.htc.agentmanager.AgentManager"
  echo $cmd
  exec $cmd 2>&1

fi

if [[ ! -z $1 && $1 == 'ag' ]]; then
  cd $HTCaaS_Server

#  echo $$ > $HTCaaS_Server/service/ag.pid

  # agent
  cmd="java "
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/Agent"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
#  cmd=$cmd":lib/shared/geronimo-servlet_3.0_spec-1.0.jar"
#  cmd=$cmd":lib/shared/jetty-continuation-8.1.15.v20140411.jar"
#  cmd=$cmd":lib/shared/jetty-http-8.1.15.v20140411.jar"
#  cmd=$cmd":lib/shared/jetty-io-8.1.15.v20140411.jar"
#  cmd=$cmd":lib/shared/jetty-server-8.1.15.v20140411.jar"
#  cmd=$cmd":lib/shared/jetty-util-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/cxf-rt-frontend-simple-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-bindings-soap-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-wsdl-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-aegis-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-jaxb-3.0.4.jar"
  cmd=$cmd":lib/shared/commons-net-3.2.jar"
#  cmd=$cmd":lib/shared/dbmanager.jar"

  cmd=$cmd" org.kisti.htc.agent.Agent"
  echo $cmd
  exec $cmd $2 $3 2>&1

fi

if [[ ! -z $1 && $1 == 'ac' ]]; then
  cd $HTCaaS_Server

  echo $$ > $HTCaaS_Server/service/ac.pid

  # ant -f build_acm.xml run
  # acmanager
  cmd="java "
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/AccountManager"
  cmd=$cmd":lib/ACManager/commons-lang-2.6.jar"
  cmd=$cmd":lib/ACManager/bcprov-jdk16-146.jar"
  cmd=$cmd":lib/ACManager/commons-io-2.0.1.jar"
  cmd=$cmd":lib/ACManager/httpclient-4.1.1.jar"
  cmd=$cmd":lib/ACManager/commons-logging-1.1.3.jar"
  cmd=$cmd":lib/ACManager/commons-logging.jar"
  cmd=$cmd":lib/ACManager/httpclient-cache-4.1.1.jar"
  cmd=$cmd":lib/ACManager/httpcore-4.2.1.jar"
  cmd=$cmd":lib/ACManager/httpmime-4.1.1.jar"
  cmd=$cmd":lib/ACManager/jackson-core-asl-1.8.1.jar"
  cmd=$cmd":lib/ACManager/jackson-mapper-asl-1.8.1.jar"
  cmd=$cmd":lib/ACManager/UIBClient.jar"
  cmd=$cmd":lib/ACManager/xstream-1.4.2.jar"
  cmd=$cmd":lib/ACManager/commons-collections-3.2.1.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
#  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
#  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#  cmd=$cmd":lib/shared/slf4j-api-1.7.9.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/geronimo-servlet_3.0_spec-1.0.jar"
  cmd=$cmd":lib/shared/jetty-continuation-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-http-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-io-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-server-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/jetty-util-8.1.15.v20140411.jar"
  cmd=$cmd":lib/shared/cxf-rt-frontend-simple-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-bindings-soap-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-wsdl-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-aegis-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-transports-http-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-transports-http-jetty-3.0.4.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"

  cmd=$cmd" org.kisti.htc.acmanager.server.Server"
  echo $cmd
  exec $cmd 2>&1
fi

if [[ ! -z $1 && $1 == 'gm' ]]; then
  cd $HTCaaS_Server

  #echo $$ > $HTCaaS_Server/service/gm.pid

  # GridResource
  cmd="java "
  cmd=$cmd" -classpath build"
  cmd=$cmd":conf/AgentManager"
  cmd=$cmd":lib/AgentManAger/commons-codec-1.5.jar"
  cmd=$cmd":lib/ACManager/commons-lang-2.6.jar"
  cmd=$cmd":lib/ACManager/bcprov-jdk16-146.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/AgentManager/jsch-0.1.44.jar"
  cmd=$cmd":lib/AgentManager/openstack4j-1.0.2.jar"
  cmd=$cmd":lib/AgentManager/openstack4j_11_25_jdk6_ver4_runable.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/cxf-rt-frontend-simple-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-bindings-soap-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-wsdl-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-aegis-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-jaxb-3.0.4.jar"
  cmd=$cmd":lib/AgentManager/aws-java-sdk-1.2.3.jar"
  
  cmd=$cmd" org.kisti.htc.agentmanager.GliteResource $2"
  #echo $cmd
  exec $cmd 2>&1

fi

exit


