#!/bin/sh

usage="Usage: $0 agentId userName"

if [ $# -ne 2 ]; then
  echo $usage >&2
  exit 1
fi

agentId=$1
userName=$2

export ANT_HOME=$PWD/agent/apache-ant-1.8.1
export PATH=$ANT_HOME/bin:$PATH

wget http://HOSTNAME:9005/HTCaaS_Storage/pTestAgent.zip -O pTestAgent.zip
unzip -qo pTestAgent.zip
rm -rf pTestAgent.zip

cd $PWD/agent

chmod -R +x $ANT_HOME/bin

ant -f run.xml -DagentId=$agentId -DuserName=$userName
