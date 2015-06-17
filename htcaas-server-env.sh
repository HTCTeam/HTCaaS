#! /bin/bash
echo "Setting HTCaaS Server Configuration Path..."
#export HTCaaS_INSTALL_PATH=/usr/local/
export HTCaaS_Server=`pwd`
export HTCaaS_Client=$HTCaaS_Server/client
export MYSQL_USER='root'
export MYSQL_PASSWD='kisti'
export HTCaaS_DB='htcaas_server'
export IP_Address=`hostname -i |awk '{print $1}'`
export HTTP_HOME=/www
export HTCaaS_Storage=htc_storage
export ANT_HOME=$HTCaaS_Server/apache-ant-1.8.1
export JAVA_HOME=/usr/java/jdk1.6.0_41

chmod -R +x $HTCaaS_Client/build/
chmod +x $HTCaaS_Client/bin/*

PATH=$HTCaaS_Server:$HTCaaS_Client/bin:$JAVA_HOME/bin:$ANT_HOME/bin:${PATH}

alias s='cd $HTCaaS_Server'
alias c='cd $HTCaaS_Client'
alias serv='cd $HTCaaS_Server/service'
alias logmq='tail -f $HTCaaS_Server/activeMQ/data/activemq.log'
alias logdb='tail -f $HTCaaS_Server/log/db.log'
alias logmn='tail -f $HTCaaS_Server/log/mn.log'
alias logam='tail -f $HTCaaS_Server/log/am.log'
alias logac='tail -f $HTCaaS_Server/log/ac.log'
alias logud='tail -f $HTCaaS_Server/log/ud.log'
alias logjm='tail -f $HTCaaS_Server/log/jm.log'
alias vilogmq='vi $HTCaaS_Server/activeMQ/data/activemq.log'
alias vilogdb='vi $HTCaaS_Server/log/DBManager/DBManager.log'
alias vilogmn='vi $HTCaaS_Server/log/Monitoring/Monitoring.log'
alias vilogam='vi $HTCaaS_Server/log/AgentManager/AgentManager.log'
alias vilogac='vi $HTCaaS_Server/log/AccountManager/AccountManager.log'
alias vilogud='vi $HTCaaS_Server/log/UDManager/UDManager.log'
alias vilogjm='vi $HTCaaS_Server/log/JobManager/JobManager.log'
alias conf='vi $HTCaaS_Server/conf/HTCaaS_Server.conf'
alias net="netstat -ant | egrep '9000|9001|9002|9003|9004|9005|61616|2011|8161|50021' | grep LISTEN"
  
chmod -R +x $ANT_HOME/bin
chmod +x *.sh
chmod +x activeMQ/bin/active*
chmod +x $HTCaaS_Server/script/*.sh
chmod 400 $HTCaaS_Server/conf/AgentManager/glite/userkey.pem
chmod 644 $HTCaaS_Server/conf/AgentManager/glite/usercert.pem
chmod 400 $HTCaaS_Server/conf/AgentManager/ksc/userkey.pem
chmod 400 $HTCaaS_Server/conf/AgentManager/local/userkey.pem
chmod 600 $HTCaaS_Server/conf/AgentManager/glite/biomed.proxy
chmod 600 $HTCaaS_Server/conf/AgentManager/glite/vo.france-asia.org.proxy
chmod 600 $HTCaaS_Server/conf/AgentManager/local/local.proxy
chmod -R 777 $HTCaaS_Server/log
chmod -R 777 $HTCaaS_Server/client/etc

echo "Completed..."
