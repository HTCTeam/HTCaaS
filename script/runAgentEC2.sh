#!/bin/sh

agentId=$AID

export ANT_HOME=$PWD/agent/apache-ant-1.8.1
export PATH=$ANT_HOME/bin:$PATH

wget http://HOSTNAME:9005/HTCaaS_Storage/agent.zip
unzip -q agent.zip

cd $PWD/agent

chmod -R +x $ANT_HOME/bin

ls -al $ANT_HOME/bin

ant -f run.xml -DagentId=$agentId
