#!/bin/bash

user=root
passwd=shtmddn
db=htcaas_portal
cmd="mysqldump -u$user -p$passwd"
$cmd -d $db > htcaas_portal_all.schema
$cmd $db attach > attach.sql
$cmd $db code > code.sql
$cmd $db template > template.sql



