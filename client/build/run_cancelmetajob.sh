#!/bin/bash

if [[ -z $HTCaaS_Server ]]; then
  echo "environment HTCaaS_Server is not set"
  exit
fi
 
  cd $HTCaaS_Server

  cmd="java "
  cmd=$cmd" -Djava.util.logging.config.file=conf/cli/log4j.properties"
  cmd=$cmd" -classpath build/"
  cmd=$cmd":conf/cli"    
  cmd=$cmd":lib/shared/commons-logging-1.1.1.jar"
  cmd=$cmd":lib/shared/commons-net-3.2.jar"  
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/shared/mail-1.4.3.jar"    
  cmd=$cmd":lib/cli/acmanager.jar"     
  cmd=$cmd":lib/cli/commons-cli-1.2.jar"     
  cmd=$cmd":lib/cli/geronimo-javamail_1.4_spec-1.6.jar"     
  cmd=$cmd":lib/cli/jobmanager.jar"     
  cmd=$cmd":lib/cli/monitoring.jar"     
  cmd=$cmd":lib/cli/udmanager.jar"    
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"
  cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
  cmd=$cmd":lib/shared/cxf-rt-frontend-simple-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-bindings-soap-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-wsdl-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-databinding-aegis-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-transports-http-3.0.4.jar"
  cmd=$cmd":lib/shared/cxf-rt-transports-http-jetty-3.0.4.jar"
  cmd=$cmd":lib/shared/stax2-api-3.1.4.jar"
  cmd=$cmd":lib/shared/woodstox-core-asl-4.4.1.jar" 
    

  cmd=$cmd" org.kisti.htc.cli.client.CancelMetaJob $1 $2 $3 $4 "

  exec $cmd 2>&1
