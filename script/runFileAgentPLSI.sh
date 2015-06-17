#!/bin/sh

usage="Usage: $0 agentId"

if [ $# -ne 1 ]; then
  echo $usage >&2
  exit 1
fi

agentId=$1
userName=$2

export ANT_HOME=$PWD/scagent/apache-ant-1.8.1
export PATH=$ANT_HOME/bin:$PATH

unzip -qo scagent.zip
rm -rf scagent.zip

cd $PWD/scagent

chmod -R +x $ANT_HOME/bin

ant -f run.xml -DagentId=$agentId
