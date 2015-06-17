#!/bin/bash
echo "Preparing HTCaaS Server Execution..."
echo "Setting HTCaaS Server Configuration Path..."
pwd=`pwd`
export HTCaaS_Server=$pwd
#export JAVA_HOME=/usr/java/jdk1.6.0_27
#export CXF_HOME=/home/seungwoo/apache-cxf-2.2.8
export ANT_HOME=$HTCaaS_Server/apache-ant-1.8.1
PATH=${PATH}:$HTCaaS_Server:$ANT_HOME/bin
chmod -R +x $ANT_HOME/bin
#chmod +x DBManager
#chmod +x UDManager
#chmod +x start*
#chmod +x submit*
chmod +x $HTCaaS_Server/script/*.sh
chmod 400 $HTCaaS_Server/conf/AgentManager/glite/userkey.pem
chmod 400 $HTCaaS_Server/conf/AgentManager/ksc/userkey.pem
chmod 400 $HTCaaS_Server/conf/AgentManager/local/userkey.pem
chmod 600 $HTCaaS_Server/conf/AgentManager/glite/biomed.proxy
chmod 600 $HTCaaS_Server/conf/AgentManager/glite/vo.france-asia.org.proxy
chmod 600 $HTCaaS_Server/conf/AgentManager/local/local.proxy
echo "Completed..." 
