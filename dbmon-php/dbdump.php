<?php

  include("common.php");


### function {{{


function _menu() {
  $qry = "SHOW TABLES";
  $ret = mysql_query($qry);
  print mysql_error();

  global $form;
  $preset = $form['table'];

  $opts = "";
  while ($row = mysql_fetch_row($ret)) {
    $table = $row[0];
    if ($table == $preset) $sel = ' selected'; else $sel = '';
    $opts.=<<<EOS
<option value='$table'$sel>$table</option>
EOS;
  }

  print<<<EOS
<form name='menuform' action='$self' method='get'>
<select name='table' onchange='_sel1()'>$opts</select>
<input type='hidden' name='mode' value='schema'>
<input type='button' value='schema' onclick='sf_1()'>
<input type='button' value='data' onclick='sf_2()'>
</form>

<script>
function sf_1() {
  document.menuform.mode.value='schema';
  document.menuform.submit();
}
function sf_2() {
  document.menuform.mode.value='viewdata';
  document.menuform.submit();
}
function _sel1() {
  //document.menuform.submit();
}
</script>
EOS;

}

function _table_columns($table) {
  $qry = "SHOW COLUMNS FROM $table";
  $ret = mysql_query($qry);
  $cols = array();
  while ($row = mysql_fetch_row($ret)) {
    $col = $row[0]; 
    $cols[] = $col;
  }
  return $cols;
}

function _table_schema($table) {
  global $env;
  global $conf;

  $qry = "SHOW COLUMNS FROM $table";

  $grpcol = $conf['table_group_count_columns'][$table];
  //dd($grpcol);

  $ret = mysql_query($qry);

  print<<<EOS
<h1>{$conf['dbname']}.$table</h1>

<table border='1' class='mmdata'>
<tr>
<th>Field</th>
<th>Type</th>
<th>Null</th>
<th>Key</th>
<th>Default</th>
<th>Extra</th>
<th>Group Count</th>
</tr>
EOS;

  global $self;
  while ($row = mysql_fetch_row($ret)) {
    $fld = $row[0];
    if ($grpcol[$fld]) {
      $group = "<a href='$self?mode=groupcount&table=$table&column=$fld'>view</a>";
    } else $group = '';

    print<<<EOS
<tr>
<td>{$row[0]}</td>
<td>{$row[1]}</td>
<td>{$row[2]}</td>
<td>{$row[3]}</td>
<td>{$row[4]}</td>
<td>{$row[5]}</td>
<td>{$group}</td>
</tr>
EOS;
  }
  print<<<EOS
</table>
EOS;
}

function _table_data($table) {
  global $conf;

  $cols = _table_columns($table);
  //dd($cols);

  $max = $conf['max_records_display'];

  $qry = "SELECT count(*) AS count FROM $table";
  $ret = mysql_query($qry);
  $row = mysql_fetch_assoc($ret);
  $total = $row['count'];
  if ($total > $max) {
    $too_many_records = true;
  } else {
    $too_many_records = false;
  }


  $qry = "SELECT * FROM $table";
  if ($too_many_records) $qry .= " LIMIT 0,1000";

  $ret = mysql_query($qry);


  print<<<EOS
<h1>{$conf['dbname']}.$table</h1>
EOS;

  if ($too_many_records) {
    print<<<EOS
<div>
Too many records: Total $total records.
</div>
EOS;
  }

  print<<<EOS
<table border='1' class='mmdata'>
EOS;
  print("<tr>");
  print("<th>#</th>");
  foreach ($cols as $c) {
    print("<th>$c</th>");
  }
  print("</tr>");

  function __row_data($cnt, $row) {
    print("<tr>");
    print("<td>$cnt</td>");
    foreach ($row as $item) {
      print("<td>$item</td>");
    }
    print("</tr>");
  }

  $cnt = 0;
  while ($row = mysql_fetch_row($ret)) {
    $cnt++;
    //dd($row);
    __row_data($cnt, $row);
  }
  print<<<EOS
</table>
EOS;
}


// 특정테이블, 특정 컬럼에 대한 그룹별 카운트
function _table_groupcount($table, $column) {
  global $conf;

  print<<<EOS
<h1>{$conf['dbname']}.$table / column=$column</h1>
EOS;

  $qry = "SELECT COUNT(*) AS count, $column AS value FROM $table GROUP BY $column ORDER BY count DESC";
  $ret = db_query($qry, 1);
  print mysql_error();

  print<<<EOS
<table border='1' class='mmdata'>
<tr>
<th>#</th>
<th>$column</th>
<th>count</th>
</tr>
EOS;

  $cnt = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;
    //dd($row);

    $count = $row['count'];
    $value = $row['value'];

    print("<tr>");
    print("<td>$cnt</td>");
    print("<td>$value</td>");
    print("<td>$count</td>");
    print("</tr>");
   }
  print<<<EOS
</table>
EOS;
}


### function }}}



if ($mode == 'schema') {

  pagehead();
  _menu();

  $table = $form['table'];
  _table_schema($table);

  pagetail();
  exit;


} else if ($mode == 'viewdata') {

  pagehead();
  _menu();

  $table = $form['table'];
  _table_data($table);

  pagetail();
  exit;

} else if ($mode == 'groupcount') {

  pagehead();
  _menu();

  $table = $form['table'];
  $column = $form['column'];
  _table_groupcount($table, $column);

  pagetail();
  exit;

}




  pagehead();
  _menu();
  pagetail();
  exit;

?>
