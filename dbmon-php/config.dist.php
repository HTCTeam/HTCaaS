<?php

  $conf = array();

  // database
  $conf['dbhost'] = 'localhost';
  $conf['dbuser'] = 'root';
  $conf['dbpass'] = '';
  $conf['dbname'] = 'htcaas';

  $conf['max_records_display'] = 1000;

  // log file path
  $conf['server_log_dir'] = "/root/HTCaaS/log";

  // php folder path
  $conf['htcaas_php_path'] = dirname($_SERVER['SCRIPT_FILENAME']);

  // Active MQ admin monitor
  $conf['activemq_url1'] = "http://ACTIVE_MQ:8161/admin/queues.jsp";

?>
