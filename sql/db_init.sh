#!/bin/bash
user=$MYSQL_USER
passwd=$MYSQL_PASSWD

echo "Removing data in $HTCaaS_DB"
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/init.sql
echo "finish"

