

back=/mnt/data_storage/htcaas/backup/
mkdir -p $back

pw=kisti123
host=pearl.kisti.re.kr

/usr/local/mysql/bin/mysqldump -h$host -uroot -p$pw htcaas_server > $back/htcaas_server_`date '+%Y-%m-%d'`.sql
/usr/local/mysql/bin/mysqldump -h$host -uroot -p$pw htcaas_portal > $back/htcaas_portal_`date '+%Y-%m-%d'`.sql

tar zcf $back/htcaas_server_sql`date '+%Y-%m-%d'`.tar.gz $back/htcaas_server_`date '+%Y-%m-%d'`.sql
tar zcf $back/htcaas_portal_sql`date '+%Y-%m-%d'`.tar.gz $back/htcaas_portal_`date '+%Y-%m-%d'`.sql

rm -rf $back/htcaas_server_`date '+%Y-%m-%d'`.sql
rm -rf $back/htcaas_portal_`date '+%Y-%m-%d'`.sql


tar zcf $back/htcaas_server_`date '+%Y-%m-%d'`.tar.gz /root/workspace/HTCaaS

