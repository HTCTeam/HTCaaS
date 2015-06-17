#!/bin/bash
echo "deploying agent.zip..."
/bin/cp -f conf/HTCaaS_Client.conf conf/HTCaaS_Client.conf.tmp
/bin/cp -f conf/HTCaaS_Agent.conf conf/HTCaaS_Client.conf
ant -f build_agent.xml deploy-zip 
/bin/mv -f conf/HTCaaS_Client.conf.tmp conf/HTCaaS_Client.conf
/bin/cp -f $HTCaaS_Server/script/*.sh $HTTP_HOME/html/$HTCaaS_Storage/
echo "finish!"
