#!/bin/bash
user=$MYSQL_USER
passwd=$MYSQL_PASSWD

echo "insert tables in $HTCaaS_DB"
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/htcaas_server_all.schema
echo "insert data"
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/agent_scaling_metric.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/am_env.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/application.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/ce_selection_metric.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/description.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/notice.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/server_env.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/service_code.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/service_infra.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/user.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/user_group.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/function.sql
echo "finish"

