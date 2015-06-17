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

for ((i=1;i<=5;i++)); do
        echo 'Downloading agent.zip...'
        echo 'try :' $i
        wget -o wgetlog http://HOSTNAME:9005/HTCaaS_Storage/agent.zip -O agent.zip -T 20 -w 10 -c
        echo 'Checking agent file size...'
        orisize=`grep saved wgetlog |awk -F[ '{print $2}'|awk -F/ '{print $1}'`
        echo 'wget agent.zip size :' $orisize
        lssize=`ls -l agent.zip |awk '{print $5}'`
        echo 'ls agent.zip size :' $lssize
        if [ $orisize -eq $lssize ]; then
                echo 'Both size are same'
                break
        else
                echo 'Both size are different. Retry to download'
                if [ $1 -eq 5 ]; then
                	exit 1
                fi
        fi
done
unzip -qo agent.zip
rm -rf agent.zip

cd $PWD/agent

chmod -R +x $ANT_HOME/bin

ant -f run.xml -DagentId=$agentId -DuserName=$userName
