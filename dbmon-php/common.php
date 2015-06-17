<?php

  include("config.php");
  include("func.php");

  session_start();

  $conf['table_group_count_columns'] = array(
    'agent' => array('host'=>1, 'status'=>1, 'CE_id'=>1, 'user_id'=>1),
    'ce' => array('priority'=>1, 'name'=>1),
    'metajob' => array('user_id'=>1, 'app_id'=>1, 'num'=>1, 'total'=>1, 'project_name'=>1, 'status'=>1),
  );

  $conn = mysql_connect($conf['dbhost'], $conf['dbuser'], $conf['dbpass']);
  if (!$conn) die('database connection error');

  $ret = mysql_select_db($conf['dbname']);
  if (!$ret) die("database select db error");

  $self = $_SERVER['PHP_SELF'];
  $form = $_REQUEST;
  $mode = $form['mode'];

  $env = array();
  $env['self'] = $self;

?>
