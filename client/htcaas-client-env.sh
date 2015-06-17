#!/bin/bash
echo "Preparing HTCaaS Shell Client Execution..."
echo "Setting HTCaaS Sheel Client Configuration Path..."
pwd=`pwd`
export HTCaaS_Client=$pwd
export HTCaaS_Server=$HTCaaS_Client
export JAVA_HOME=/usr/java/default
export ANT_HOME=/usr/local/apache-ant-1.8.1
chmod +x $HTCaaS_Client/build/*.sh
chmod +x $HTCaaS_Client/bin/*
PATH=$HTCaaS_Client/bin:${PATH}:$ANT_HOME/bin:$JAVA_HOME/bin
echo "Completed..." 
