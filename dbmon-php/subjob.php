<?php

  include("common.php");


### function {{{

function _style() {
  print<<<EOS
<style>
span.lbl { padding:3 3 3 3px; color:#eeeeee; background:#555; border:#c0c0c0 1px solid;
 height:20px; font-weight:bold; margin:0 0 0 0 px; }
</style>
EOS;
}

function _total_count($sql_from_join_where) {
  $qry = "SELECT COUNT(*) AS total $sql_from_join_where";
  $ret = db_query($qry);
  $row = mysql_fetch_assoc($ret);
  $total = $row['total'];
  return $total;
}


function _view() {
  global $form;

  $mjid = $form['id'];

  $select_items = "J.*, A.host, CE.name CEname";
  $sql_from = " FROM Job J";
  $sql_join = " LEFT JOIN Agent A ON J.agent_id=A.id"
             ." LEFT JOIN CE ON J.CE_id=CE.id"
              ;
 
  $w = array();
  $w[] = "J.metajob_id='$mjid'";
  $v = $form['host']; if ($v != '') $w[] = "A.host LIKE '%$v%'";
  $v = $form['status']; if ($v != '') $w[] = "J.status='$v'";
  $sql_where = " WHERE ".join(" AND ", $w);

  $sql_order = " ORDER BY J.seq DESC";


  $sql_from_join_where = "$sql_from $sql_join $sql_where";
  $total = _total_count($sql_from_join_where);
  $ipp = 100;
  $page = $form['page'];

  list($start, $last, $page) = pager_calc_page($ipp, $total);
  //dd("$total, $start, $last, $page");

  global $mode;
  print<<<EOS
<form action='$self' method='get' name='sform'>
EOS;

  $v = $form['host'];
  print<<<EOS
<span class='lbl'>host</span><input type='text' name='host' size='10' value='$v' onclick="this.value=''">
EOS;

  $list = array('','preparing','running', 'done', 'failed');
  $preset = $form['status'];
  $opt = option_general($list, $preset);
  print<<<EOS
<span class='lbl'>status</span><select name='status'>$opt</select>
EOS;

  global $mode;
  print<<<EOS
<input type='hidden' name='mode' value='$mode'>
<input type='hidden' name='id' value='{$form['id']}'>
<input type='hidden' name='page' value='$page'>

<input type='button' onclick='_submit()' value=' OK ' style="width:60px; height:30px;">
</form>

<script>
function _submit() {
  document.sform.submit();
}
</script>
EOS;

  // 페이지 이동
  $html = pager_html($total, $page, $last, $ipp, 'sform');
  print $html;


  $qry = "SELECT $select_items $sql_from $sql_join $sql_where $sql_order";
  $qry .= " LIMIT $start,$ipp";
//dd($qry);
  $ret = db_query($qry);

  $hi = "#,metajob_id,id,seq,name,errormsg,startTimestamp,lastUpdateTime,elapsed"
       .",status,CE,agent_id,host,results";
  table_head($hi);

  // 변수앞에 _을 붙이면 일반변수, 그렇지 않으면 $row의 key값
  $tdk = "metajob_id,id,seq,name,errormsg,startTimestamp,lastUpdateTime,_elapsed"
        .",status,CEname,agent_id,host,_results";
  $items = preg_split("/,/", $tdk);

  $cnt = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;
    //dd($row);
    $id = $row['id'];

    $results = span_link('results', "_list_results('$id')");

    $s = $row['startTimestamp'];
    $e = $row['lastUpdateTime'];
    $elapsed = diff_time($e, $s);

    $fields = array();
    $fields[] = array($cnt, $atr1);
    foreach ($items as $item) {
      $a = substr($item, 0, 1); // $item = '_cnt'
      if ($a == '_') {
        $key = substr($item, 1); // $key = 'cnt'
        $value = $$key; // $value = $cnt
      } else if ($a == '') {
        $value = '';
      } else {
        $value = $row[$item];
      }
      $fields[] = array($value, $atr1);
    }

    print("<tr>");
    for ($i = 0; $i < count($fields); $i++) {
      list($str, $attr) = $fields[$i];
      table_data_text($str, $attr, $nowrap=1);
    }
    print("</tr>");
  }
  $script=<<<EOS
function _list_results(subjob_id) {
  var url = "result.php?mode=view&sjid="+subjob_id;
  document.location.href = url;
}
EOS;
  table_close($script);

}


### function }}}

if ($mode == 'view') {

  pagehead();

  $mjid = $form['id'];
  ptitle("SubJobs list of a MetaJob id=$mjid");
  _style();
  _view();

  pagetail();
  exit;
}


  pagehead();
  pagetail();
  exit;


?>
