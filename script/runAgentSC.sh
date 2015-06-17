#!/bin/sh

usage="Usage: $0 agentId gangaConfig proxyFile"

if [ $# -ne 3 ]; then
  echo $usage >&2
  exit 1
fi

agentId=$1
gangaConfig=$2
proxyFile=$3

export ANT_HOME=$PWD/agent/apache-ant-1.8.1
export PATH=$ANT_HOME/bin:$PATH

wget http://HOSTNAME:9005/HTCaaS_Storage/agentsc.zip
unzip -q agentsc.zip

cd $PWD/agentsc

chmod -R +x $ANT_HOME/bin

ls -al $ANT_HOME/bin

ant -f run.xml -DagentId=$agentId -DgangaConfig=$gangaConfig -DproxyFile=$proxyFile
