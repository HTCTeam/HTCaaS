#!/bin/sh

usage="Usage: $0 agentId"

if [ $# -ne 1 ]; then
  echo $usage >&2
  exit 1
fi

agentId=$1

export ANT_HOME=$PWD/samplingagent/apache-ant-1.8.1
export PATH=$ANT_HOME/bin:$PATH

wget http://HOSTNAME:9005/HTCaaS_Storage/samplingagent.zip
unzip -q samplingagent.zip

cd $PWD/samplingagent

chmod -R +x $ANT_HOME/bin

ls -al $ANT_HOME/bin

ant -f samplingrun.xml -DagentId=$agentId
