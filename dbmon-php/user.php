<?php

  include("common.php");


### function {{{

function _push(&$fields, &$items, &$row, &$var) {
  foreach ($items as $item) {
    list($key, $opt) = preg_split("/#/", $item);
  
    if (strstr($opt, '_')) $value = $var[$key];
    else $value = $row[$key];

    $atr1 = '';
         if (strstr($opt, 'L')) { $atr1 = " class='l'"; }
    else if (strstr($opt, 'R')) { $atr1 = " class='r'"; }
    else if (strstr($opt, 'C')) { $atr1 = " class='c'"; }

    $fields[] = array($value, $atr1);
  }
}

function _view() {

  $select_items = "U.*";
  $sql_from = " FROM user U";
  $sql_join = "";
  $sql_where = '1'; 
  $sql_order = "";

  $qry = "SELECT $select_items $sql_from  $sql_join WHERE $sql_where $sql_order";
  $ret = db_query($qry);

  $hi = "#,userid,service_Infra_id,name,keepAgentNO";
  table_head($hi);

  // 변수명#옵션
  // 옵션 _:일반변수, L:왼쪽정렬 R:오른쪽정렬 C:가운데정렬
  $tdk = "userid#C,service_Infra_id,name,keepAgentNO";
  $items = preg_split("/,/", $tdk);

  $cnt = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;
    $id = $row['id'];

    $var = array();
    //$var['agents'] = span_link('agents', "_list1('$id')");

    $fields = array();
    $fields[] = array($cnt, '');
    _push($fields, $items, $row, $var);

    print("<tr>");
    for ($i = 0; $i < count($fields); $i++) {
      list($str, $attr) = $fields[$i];
      table_data_text($str, $attr, $nowrap=1);
    }
    print("</tr>");
  }
  $script=<<<EOS
/*
function _list1(id) {
  var url = "agent.php?mode=view&userid="+id;
  document.location.href = url;
}
*/
EOS;
  table_close($script);
}


### function }}}

  pagehead();

  ptitle('User List');
  _view();

  pagetail();
  exit;

?>
