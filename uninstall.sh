###HTCaaS Unintsall Shell Script
###Uninstalled Configuration
###JAVA 1.6 jdk, vsftpd, ftp, httpd, unzip, mysql-server, mysql, removal of htcaas user, /etc/limits.conf, /etc/hosts, /etc/profile
. htcaas-server-env.sh
echo ""


### {{{

function uninstall_java {
	#Uninstall Java
	echo ""
	PROGRAM=java
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`ls /usr/java/ |grep jdk1.6.0_41 |wc -l`
		if [[ check -eq 1 ]]; then
			rpm -e jdk
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $PROGRAM"
		else 
			echo "$PROGRAM : Not installed (skip)"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_vsftpd {
	###Uninstall vsftpd
	echo ""
	PROGRAM=vsftpd
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
		if [[ check -eq 1 ]]; then
			service $PROGRAM stop
			yum -y remove $PROGRAM
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $PROGRAM"
		else 
			echo "$PROGRAM : Not installed (skip)"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_ftp {
	###Uninstall ftp
	PROGRAM=ftp
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
		if [[ check -eq 1 ]]; then
			service $PROGRAM stop
			yum -y remove $PROGRAM
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $PROGRAM"
		else 
			echo "$PROGRAM : Not installed (skip)"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_httpd {
	###Uninstall httpd
	PROGRAM=httpd
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
		if [[ check -eq 1 ]]; then
			service $PROGRAM stop
			yum -y remove $PROGRAM
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			setsebool -P httpd_enable_homedirs 0
			echo "Done : $PROGRAM"
		else 
			echo "$PROGRAM : Not installed (skip)"
		fi
	else
	        echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_limits_conf {
	###UnSet Limit.conf
	PROGRAM=limits.conf
	LOC=/etc/security/
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		HEAD=`cat $LOC$PROGRAM  |grep -n HTCaaS |cut -d: -f1 |head -1`
		TAIL=`cat $LOC$PROGRAM  |grep -n HTCaaS |cut -d: -f1 |tail -1`
		if [[ $HEAD -gt 0 && $TAIL -gt 0 ]]; then
			echo "Deleting $PROGRAM..."
			sed -e "$HEAD,$TAIL d" $LOC$PROGRAM > $LOC$PROGRAM.tmp
			mv -f $LOC$PROGRAM.tmp $LOC$PROGRAM
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $LOC$PROGRAM"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)" 
	fi
}

function uninstall_unzip {
	###Uninstall unzip
	PROGRAM=unzip
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
		if [[ check -eq 1 ]]; then
			yum -y remove $PROGRAM
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $PROGRAM"
		else 
			echo "$PROGRAM : Not installed (skip)"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_mysql-server {
	###Uninstall MYSQL-SERVER
	PROGRAM='mysql-server'
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
		if [[ check -eq 1 ]]; then
			/etc/init.d/mysqld stop
			yum -y remove $PROGRAM
			mv -f /var/lib/mysql /var/lib/mysql.bak
			echo "Moved data : /var/lib/mysql.bak"
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $PROGRAM"
		else
	         echo "$PROGRAM : Not installed (skip)"
	    fi
	else 
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_mysql {
	###Uninstall MYSQL
	PROGRAM='mysql'
	TMP='-client'
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM$TMP |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $PROGRAM..."
		check=`yum list installed $PROGRAM|grep $PROGRAM |wc -l`
		if [[ check -eq 1 ]]; then
			yum -y remove $PROGRAM
			sed /$PROGRAM$TMP/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $PROGRAM"
		else 
			echo "$PROGRAM : Not installed (skip)"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_hosts {
	###Unresgiter hostname into /etc/hosts
	PROGRAM='hosts'
	LOC='/etc/'
	echo "Uninstalling $LOC$PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $LOC$PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $LOC$PROGRAM..."
		check=`cat $LOC$PROGRAM|grep "$IP_Address $HOSTNAME" |wc -l`
		if [[ check -eq 1 ]]; then
			sed /"$IP_Address $HOSTNAME"/d $LOC$PROGRAM > $LOC$PROGRAM.tmp;mv -f $LOC$PROGRAM.tmp $LOC$PROGRAM			
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			echo "Done : $LOC$PROGRAM"
		else 
			echo "$LOC$PROGRAM : Not installed (skip)"
		fi
	else
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

function uninstall_profile {
	##Recovering /etc/profies
	PROGRAM='profile'
	LOC='/etc/'
	echo "Uninstalling $LOC$PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $LOC$PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		echo "Checking $LOC$PROGRAM..."
		HEAD=`cat $LOC$PROGRAM  |grep -n HTCaaS |cut -d: -f1 |head -1`
		TAIL=`cat $LOC$PROGRAM  |grep -n HTCaaS |cut -d: -f1 |tail -1`
		if [[ $HEAD -gt 0 && $TAIL -gt 0 ]]; then
			sed -e "$HEAD,$TAIL d" $LOC$PROGRAM > $LOC$PROGRAM.tmp
			mv -f $LOC$PROGRAM.tmp $LOC$PROGRAM
			sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
			source /etc/profile
			echo "Done : $LOC$PROGRAM"
		else
	                echo "$LOC$PROGRAM : Not installed (skip)"
	        fi
	else 
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi	
}

function uninstall_HTCaaS {
	echo "Stopping all htcaas program..."
	./service/mgrctl all stop
	echo "Uninstalling $PROGRAM ..."
	PROGRAM='HTCaaS_ALL'
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then	
		###Removeing HTCaaS_Storage
		echo "Removing HTCaaS_Storage"
		rm -rf $HTTP_HOME/html/$HTCaaS_Storage
	
		###Removing HTCaaS User
		echo ""
		echo "Remove User htcaas..."
		userdel htcaas
		
		sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
		echo "Done : $PROGRAM"
		
		###Uninstall HTCaaS_DB
		uninstall_HTCaaS_DB
			
	else 
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi	
}

function uninstall_HTCaaS_DB {
	PROGRAM='HTCaaS_DB'
	echo "Uninstalling $PROGRAM ..."
	flag=`cat INSTALLED_PROGRAM| grep $PROGRAM |wc -l`
	if [[ flag -eq 1 ]]; then
		###Removing HTCaaS_DB
		echo "Remove DB and Schema..."
		if [[ -f /usr/bin/mysqladmin ]]; then
			mysqladmin -u$MYSQL_USER -p$MYSQL_PASSWD drop $HTCaaS_DB;
		fi
		sed /$PROGRAM/d INSTALLED_PROGRAM > INSTALLED_PROGRAM.tmp;mv -f INSTALLED_PROGRAM.tmp INSTALLED_PROGRAM
		echo "Done : $PROGRAM"
	else 
		echo "Not included in INSTALLED_PROGRAM(skip)"
	fi
}

### }}}


if [[ -z $1 ]]; then
  echo "Usage:"
  echo " uninstall all          : ./uninstall.sh all "
  echo " uninstall java         : ./uninstall.sh java "
  echo " uninstall vsftpd       : ./uninstall.sh vsftpd "
  echo " uninstall ftp          : ./uninstall.sh ftp "
  echo " uninstall httpd        : ./uninstall.sh httpd "
  echo " uninstall limits.conf  : ./uninstall.sh limits "
  echo " uninstall unzip        : ./uninstall.sh unzip "
  echo " uninstall mysql-server : ./uninstall.sh mysql-server "
  echo " uninstall mysql        : ./uninstall.sh mysql "
  echo " uninstall /etc/hosts   : ./uninstall.sh hosts "
  echo " uninstall /etc/profile : ./uninstall.sh profile "
  echo " uninstall HTCaaS_ALL   : ./uninstall.sh HTCaaS "
  echo " uninstall HTCaaS_DB    : ./uninstall.sh HTCaaS_DB "
  exit
fi

if [[ ! -z $1 && $1 == 'java' ]]; then
  uninstall_java
elif [[ ! -z $1 && $1 == 'vsftpd' ]]; then
  uninstall_vsftpd
elif [[ ! -z $1 && $1 == 'ftp' ]]; then
  uninstall_ftp
elif [[ ! -z $1 && $1 == 'httpd' ]]; then
  uninstall_httpd
elif [[ ! -z $1 && $1 == 'limits' ]]; then
  uninstall_limits_conf
elif [[ ! -z $1 && $1 == 'unzip' ]]; then
  uninstall_unzip
elif [[ ! -z $1 && $1 == 'mysql-server' ]]; then
  uninstall_mysql-server
elif [[ ! -z $1 && $1 == 'mysql' ]]; then
  uninstall_mysql
elif [[ ! -z $1 && $1 == 'HTCaaS' ]]; then
  uninstall_HTCaaS
elif [[ ! -z $1 && $1 == 'hosts' ]]; then
  uninstall_hosts
elif [[ ! -z $1 && $1 == 'profile' ]]; then
  uninstall_profile
elif [[ ! -z $1 && $1 == 'HTCaaS_DB' ]]; then
  uninstall_HTCaaS_DB
elif [[ ! -z $1 && $1 == 'all' ]]; then
  uninstall_java
  uninstall_vsftpd
  uninstall_ftp
  uninstall_httpd
  uninstall_limits_conf
  uninstall_unzip
  uninstall_mysql-server
  uninstall_mysql
  uninstall_hosts
  uninstall_profile
  uninstall_HTCaaS
  echo ""
  echo "Completed..."
  echo ""
    
fi	


