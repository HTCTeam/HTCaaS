###HTCaaS Intsall Shell Script
###Required Program
###JAVA 1.6 jdk, vsftpd, ftp, httpd, unzip, mysql, (Subversion)
. htcaas-server-env.sh

### {{{

function install_java {
	#Install Java
	echo ""
	PROGRAM=java
	echo "Checking $PROGRAM..."
	check=`ls /usr/java/ |grep jdk1.6.0_41|wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Install JAVA 1.6 JDK..."
		wget http://pearl.kisti.re.kr:9005/htc_storage/jdk-6u41-linux-x64-rpm.bin -O jdk-6u41-linux-x64-rpm.bin -c
		chmod +x jdk-6u41-linux-x64-rpm.bin
		./jdk-6u41-linux-x64-rpm.bin
		rm -rf *.bin *.rpm
		java -version
	else 
		echo "$PROGRAM : already installed (skip)"
		/usr/java/jdk1.6.0_41/bin/java -version
	fi
}

function install_vsftpd {
	###Install vsftpd
	echo ""
	PROGRAM=vsftpd
	echo "Checking $PROGRAM..."
	check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Install $PROGRAM..."
		yum -y install $PROGRAM
		sed -e "s/anonymous_enable=YES/anonymous_enable=NO/g" /etc/vsftpd/vsftpd.conf > /etc/vsftpd/vsftpd.conf.tmp
		echo listen_port=50021 >> /etc/vsftpd/vsftpd.conf.tmp
		echo port_enable=YES >> /etc/vsftpd/vsftpd.conf.tmp
		echo max_clients=100000 >> /etc/vsftpd/vsftpd.conf.tmp
		echo max_per_ip=10000 >> /etc/vsftpd/vsftpd.conf.tmp
		echo pasv_enable=YES >> /etc/vsftpd/vsftpd.conf.tmp
		echo pasv_min_port=40000 >> /etc/vsftpd/vsftpd.conf.tmp
		echo pasv_max_port=50000 >> /etc/vsftpd/vsftpd.conf.tmp
		echo connect_from_port_20=YES >> /etc/vsftpd/vsftpd.conf.tmp
		echo ftp_data_port=50020 >> /etc/vsftpd/vsftpd.conf.tmp
		mv -f /etc/vsftpd/vsftpd.conf /etc/vsftpd/vsftpd.conf.ori
		mv -f /etc/vsftpd/vsftpd.conf.tmp /etc/vsftpd/vsftpd.conf
		setsebool -P ftp_home_dir 1
		service $PROGRAM start
	else 
		echo "$PROGRAM : already installed (skip)"
	fi
}

function install_ftp {
	###Install ftp
	echo ""
	PROGRAM=ftp
	echo "Checking $PROGRAM..."
	check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Install $PROGRAM..."
		yum -y install $PROGRAM
	else 
		echo "$PROGRAM : already installed (skip)"
	fi
}

function install_httpd {
	###Install httpd
	echo ""
	PROGRAM=httpd
	echo "Checking $PROGRAM..."
	check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Install $PROGRAM..."
		yum -y install $PROGRAM 
		### For using semanage command
		yum -y install policycoreutils-python
		sed -e "s/Listen 80/Listen 9005/g" -e "s:/var/www:$HTTP_HOME:g" /etc/httpd/conf/httpd.conf > /etc/httpd/conf/httpd.conf.tmp
		mv -f /etc/httpd/conf/httpd.conf /etc/httpd/conf/httpd.conf.ori
		mv -f /etc/httpd/conf/httpd.conf.tmp /etc/httpd/conf/httpd.conf
		setsebool -P httpd_enable_homedirs 1
		setenforce 0
		semanage fcontext -a -t httpd_sys_content_t "$HTTP_HOME/html/$HTCaaS_Storage(/.*)?"
		restorecon -Rv $HTTP_HOME/html/$HTCaaS_Storage
		mkdir -p $HTTP_HOME
		service $PROGRAM start
		setenforce 1
	else 
		echo "$PROGRAM : already installed (skip)"
	fi
}

function install_limits_conf {
	###Set Limits.conf
	echo ""
	PROGRAM=/etc/security/limits.conf
	echo "Checking $PROGRAM..."
	check=`cat $PROGRAM |grep HTCaaS |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Set Limit.conf..."
		/bin/cp -f $PROGRAM $PROGRAM.ori
		echo '#HTCaaS Configuration start' >> $PROGRAM
		echo '*                -       maxlogins       10000' >> $PROGRAM
		echo '*                soft    nofile          10000' >> $PROGRAM
		echo '*                hard    nofile          10000' >> $PROGRAM
		echo '*                soft    stack           20480000' >> $PROGRAM
		echo '*                hard    stack           20480000' >> $PROGRAM
		echo '#HTCaaS Configuration end' >> $PROGRAM
	else 
		echo "$PROGRAM : already added (skip)"
	fi
}

function install_unzip {
	###Install unzip
	echo ""
	PROGRAM=unzip
	echo "Checking $PROGRAM..."
	check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Install $PROGRAM..."
		yum -y install $PROGRAM
	else 
		echo "$PROGRAM : already added (skip)"
	fi
}

###Install Subversion
#echo ""
#echo "Install Subversion..."
#yum -y install subversion


function install_mysql-server {
	###Install MYSQL-SERVER
	echo ""
	PROGRAM='mysql-server'
	PROGRAM2='mysql-client'
	echo "Checking $PROGRAM..."
	check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		echo "Install and launch $PROGRAM..."
		yum -y install $PROGRAM
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM2 |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM2 >> INSTALLED_PROGRAM
		fi
		/etc/init.d/mysqld start
		/usr/bin/mysqladmin -u$MYSQL_USER password $MYSQL_PASSWD
	else 
		echo "$PROGRAM : already added (skip)"
	fi
}

function install_mysql {
	###Install MYSQL
	echo ""
	PROGRAM='mysql'
	echo "Checking $PROGRAM..."
	check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		echo "Install and launch $PROGRAM..."
		yum -y install $PROGRAM
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM'-client' |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM'-client' >> INSTALLED_PROGRAM
		fi
	else 
		echo "$PROGRAM'-client' : already added (skip)"
	fi
}

function install_hosts {
	###Resgiter hostname into /etc/hosts
	echo ""
	PROGRAM='/etc/hosts'
	echo "Checking $PROGRAM..."
	check=`cat $PROGRAM|grep "$IP_Address $HOSTNAME" |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Register IP in $PROGRAM..."
		/bin/cp -f $PROGRAM $PROGRAM.ori
		echo $IP_Address $HOSTNAME >> $PROGRAM
	else 
		echo "$PROGRAM : already added (skip)"
	fi	
}

function install_profile {
	###Regiser HTCaaS Server and Client Location in /etc/profile
	echo ""
	PROGRAM='/etc/profile'
	echo "Checking $PROGRAM..."
	check=`cat $PROGRAM|grep "HTCaaS_Server" |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		echo "Register environment variable in $PROGRAM"
		/bin/cp -f $PROGRAM $PROGRAM.ori
		echo '#HTCaaS Configuration start' >> $PROGRAM
		echo "export HTCaaS_Server=$HTCaaS_Server" >> $PROGRAM
		echo "export HTCaaS_Client=$HTCaaS_Server/client" >> $PROGRAM
		tag=`echo $PATH |grep $HTCaaS_Server |wc -l`
		if [[ tag -eq 0 ]]; then
			export PATH=$HTCaaS_Server:$HTCaaS_Client/bin:$JAVA_HOME/bin:$ANT_HOME/bin:${PATH}
			echo "PATH=${PATH}" >> $PROGRAM
		else
			export PATH=${PATH}
			echo "PATH=${PATH}" >> $PROGRAM
		fi
		echo '#HTCaaS Configuration end' >> $PROGRAM
		
	else 
		echo "$PROGRAM : already added (skip)"
	fi
}

function install_HTCaaS {	
	
	echo ""
	source /etc/profile
	PROGRAM='HTCaaS_ALL'
	echo "Checking $PROGRAM..."
	check=`cat INSTALLED_PROGRAM|grep $PROGRAM |wc -l`
	if [[ check -eq 0 ]]; then
		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
		if [[ flag -eq 0 ]]; then
			echo $PROGRAM >> INSTALLED_PROGRAM
		fi
		###Making HTCaaS_Storage
			echo ""
			echo "mkdir HTCaaS_Storage"
			mkdir -p $HTTP_HOME/html/$HTCaaS_Storage
	
		###Set HTCaaS Configuration File
		echo ""
		echo "Set HTCaaS Configuratio File..."
		sed -e "s/HOSTNAME/$IP_Address/g" -e "s/HTCaaS_DB/$HTCaaS_DB/g" -e "s/USER/$MYSQL_USER/g" -e "s/PASSWORD/$MYSQL_PASSWD/g" $HTCaaS_Server/conf/HTCaaS_Server.conf > $HTCaaS_Server/conf/HTCaaS_Server.conf.tmp
		sed -e "s/HOSTNAME/$IP_Address/g" $HTCaaS_Server/conf/HTCaaS_Client.conf > $HTCaaS_Server/conf/HTCaaS_Client.conf.tmp
		sed -e "s/HOSTNAME/$IP_Address/g" $HTCaaS_Server/conf/HTCaaS_Agent.conf > $HTCaaS_Server/conf/HTCaaS_Agent.conf.tmp
		mv -f $HTCaaS_Server/conf/HTCaaS_Server.conf.tmp $HTCaaS_Server/conf/HTCaaS_Server.conf
		mv -f $HTCaaS_Server/conf/HTCaaS_Client.conf.tmp $HTCaaS_Server/conf/HTCaaS_Client.conf
		mv -f $HTCaaS_Server/conf/HTCaaS_Agent.conf.tmp $HTCaaS_Server/conf/HTCaaS_Agent.conf
	
		###Register IP Adress of Script file
		echo ""
		echo "Change IP of Script file..."
		for i in $HTCaaS_Server/script/*; do
			echo $i
			sed -e "s/HOSTNAME/$IP_Address/g" -e "s/HTCaaS_Storage/$HTCaaS_Storage/g" $i > $i.tmp
			mv -f $i.tmp $i
		done
	
		### Build and Deploy HTCaaS Server / Create DB and Schema
		echo ""
		echo "Build and deploy HTCaaS Server..."
		sh build.sh all
		sh generate_interface.sh
		
		###Install HTCaaS_DB
		install_HTCaaS_DB
	
		###Add HTCaaS User and passwd
		echo ""
		echo "Add User htcaas..."
		useradd htcaas
		echo 'htcaas:htcaas' | chpasswd
		
		###Create access.sql and insert it
		echo "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'%' identified by '$MYSQL_PASSWD' with grant option;" > sql/access.sql
		echo "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'$HOSTNAME' identified by '$MYSQL_PASSWD' with grant option;" >> sql/access.sql
		echo "GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'127.0.0.1' identified by '$MYSQL_PASSWD' with grant option;" >> sql/access.sql
		echo "FLUSH PRIVILEGES;" >> sql/access.sql
		mysql -u$MYSQL_USER -p$MYSQL_PASSWD < sql/access.sql
		rm -f sql/access.sql
	else 
		echo "$PROGRAM : already added (skip)"
	fi
}

function install_HTCaaS_DB {
	###Making HTCaaS_DB
	echo ""
	echo "Create DB and Schema..."
	PROGRAM='HTCaaS_DB'
	echo "Checking $PROGRAM..."
	check=`cat INSTALLED_PROGRAM |grep $PROGRAM| wc -l`
	if [[ check -eq 0 ]]; then
	 		flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
			if [[ flag -eq 0 ]]; then
				echo $PROGRAM >> INSTALLED_PROGRAM
			fi
			mysqladmin -u$MYSQL_USER -p$MYSQL_PASSWD create $HTCaaS_DB;
			sh sql/db_insert.sh
	else
		echo "$PROGRAM : already added (skip)"
	fi
}

### }}}

if [[ -z $1 ]]; then
  echo "Usage:"
  echo " install all          : ./install.sh all "
  echo " install java         : ./install.sh java "
  echo " install vsftpd       : ./install.sh vsftpd "
  echo " install ftp          : ./install.sh ftp "
  echo " install httpd        : ./install.sh httpd "
  echo " install limits.conf  : ./install.sh limits "
  echo " install unzip        : ./install.sh unzip "
  echo " install mysql-server : ./install.sh mysql-server "
  echo " install mysql        : ./install.sh mysql "
  echo " install /etc/hosts   : ./install.sh hosts "
  echo " install /etc/profile : ./install.sh profile "
  echo " install HTCaaS_ALL   : ./install.sh HTCaaS "
  echo " install HTCaaS_DB    : ./install.sh HTCaaS_DB "
  exit
fi

if [[ ! -z $1 && $1 == 'java' ]]; then
  install_java
elif [[ ! -z $1 && $1 == 'vsftpd' ]]; then
  install_vsftpd
elif [[ ! -z $1 && $1 == 'ftp' ]]; then
  install_ftp
elif [[ ! -z $1 && $1 == 'httpd' ]]; then
  install_httpd
elif [[ ! -z $1 && $1 == 'limits' ]]; then
  install_limits_conf
elif [[ ! -z $1 && $1 == 'unzip' ]]; then
  install_unzip
elif [[ ! -z $1 && $1 == 'mysql-server' ]]; then
  install_mysql-server
elif [[ ! -z $1 && $1 == 'mysql' ]]; then
  install_mysql
elif [[ ! -z $1 && $1 == 'HTCaaS' ]]; then
  install_HTCaaS
elif [[ ! -z $1 && $1 == 'hosts' ]]; then
  install_hosts
elif [[ ! -z $1 && $1 == 'profile' ]]; then
  install_profile
elif [[ ! -z $1 && $1 == 'HTCaaS_DB' ]]; then
  install_HTCaaS_DB
elif [[ ! -z $1 && $1 == 'all' ]]; then
  install_java
  install_vsftpd
  install_ftp
  install_httpd
  install_limits_conf
  install_unzip
  install_mysql-server
  install_hosts
  install_profile
  install_HTCaaS
  echo ""
  echo "Completed..."
  echo "======================================================================="
  echo "Open a new terminal and run following commands to start HTCaaS Server :)"
  echo "################################# "
  echo "#cd $HTCaaS_Server                "
  echo "#. htcaas-server-env.sh           "
  echo "#serv                             " 
  echo "#./mgrctl all start               "
  echo "################################# "
  echo ""
  echo "To run HTCaaS examples, refer to README file :)"
  echo ""
    
fi	



