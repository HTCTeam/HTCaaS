#!/bin/bash
user=$MYSQL_USER
passwd=$MYSQL_PASSWD
db=$HTCaaS_DB
host=localhost

echo "Getting schema and data from Database $db.
cmd="mysqldump -h$host -u$user -p$passwd"
$cmd -d $db > htcaas_server_all.schema
$cmd $db agent_scaling_metric > agent_scaling_metric.sql
$cmd $db am_env > am_env.sql
$cmd $db application > application.sql
$cmd $db ce_selection_metric > ce_selection_metric.sql
$cmd $db description > description.sql
$cmd $db notice > notice.sql
$cmd $db server_env > server_env.sql
$cmd $db service_code > service_code.sql
$cmd $db service_infra > service_infra.sql
$cmd $db user > user.sql
$cmd $db user_group > user_group.sql
$cmd --routines --no-create-info --no-data --no-create-db --skip-opt $db > function.sql



