#!/bin/bash
export HTCaaS_DB=htcaas_portal
user=root
passwd=kisti123

echo "insert tables in $HTCaaS_DB"
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/portal/htcaas_portal_all.schema
echo "insert data"
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/portal/attach.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/portal/code.sql
mysql -u$user -p$passwd $HTCaaS_DB  < $HTCaaS_Server/sql/portal/template.sql
echo "finish"

