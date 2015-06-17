<?php

  include("common.php");

  $page_title = "CE List";

### function {{{
function _style() {
  print<<<EOS
<style>
span.label { background-color:#444; border:2px solid #999; padding:3 3 3 3px; color:#fff; }
</style>
EOS;
}

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


function _list() {
  global $form;


  ## {{
  print<<<EOS
<table border='0'>
<form name='form' method='get' action='$self'>
<tr>

<td>
<input type='hidden' name='mode' value='$mode'>
<input type='submit' value='OK' style="width:50;height:50;">
</td>

<td>
EOS;

  if ($form['ckrf']) $ck = ' checked'; else $ck = '';
  print<<<EOS
<span class='label'>Refresh</span>
<input type='checkbox' name='ckrf' id='ckrf'$ck onclick='_clk_reload()'><label for='ckrf'>automatic</label>
EOS;

  $v = $form['rftime']; $sel = array(); if (!$v) { $v = 5; } $sel[$v] = ' selected';
  print<<<EOS
<select name='rftime' onchange='_change_rftime()'>
<option value='2'$sel[2]>2 seconds</option>
<option value='5'$sel[5]>5 seconds</option>
<option value='10'$sel[10]>10 seconds</option>
<option value='30'$sel[30]>30 seconds</option>
<option value='60'$sel[60]>60 seconds</option>
</select>
<span id='spanTimer'></span>
EOS;
  print("<br>");

  print<<<EOS
</td>

</tr>
</form>
</table>
EOS;

  global $page_title;
  print<<<EOS
<script>
  var timer = 5;
  var form = document.form;
  var span = document.getElementById('spanTimer');
  var stoptimer = 0;

function _reload() {
  if (!form.ckrf.checked) return;
  form.submit();
}
function _countdown() {
  if (_count_checked() > 0) stoptimer = true;
  else stoptimer = false;
  if (stoptimer) {
     setTimeout("_countdown()", 1000);
     return;
  }
  if (!form.ckrf.checked) return;
  var str = "" + timer + " sec";
  parent.top.document.title = "$page_title (" + str + ")";
  span.innerHTML = str;
  timer--;
  if (timer <= 0) _reload();
  setTimeout("_countdown()", 1000);
}
function _change_rftime() {
  var interval = parseInt(form.rftime.value);
  timer = interval;
}
function _clk_reload() {
  setTimeout("_countdown()", 1000);
}
function _onload() {
  setTimeout("_countdown()", 1000);
  var interval = parseInt(form.rftime.value);
  timer = interval;
}

if (window.addEventListener) {
  window.addEventListener("load", _onload, false);
} else if (document.attachEvent) {
  window.attachEvent("onload", _onload);
}
</script>
EOS;

  ## }}


  $sql_from = " FROM CE CE";

  $select_items = "CE.*, SI.name SIname"
     .", IF(CE.available,'1','0') avail"
     .", IF(CE.banned,'1','0') banned";

  $sql_join = " LEFT JOIN service_infra SI ON CE.service_Infra_id=SI.id";

  $w = array('1');

  $sql_where = " WHERE ".join(" AND ", $w);

  $sql_order = " ORDER BY CE.name";
  //$sql_order = " ORDER BY CE.banned, CE.available,CE.lastUpdateTime DESC";

  $qry = "SELECT $select_items $sql_from $sql_join $sql_where $sql_order";
  //dd($qry);

  $ret = db_query($qry);


  print<<<EOS
<input type='button' onclick='_setavail(1)' value='Set Available'>
<input type='button' onclick='_setavail(0)' value='Set UnAvailable'>
<script>
function _count_checked() {
  var form = document.form2;

  var cbs = document.getElementsByName('cb[]');
  var len = cbs.length;

  var count = 0;
  for (i = 0; i < len; i++) {
    var cb = cbs[i];
    if (cb.checked) { count++; }
  }
  return count;
}
function _setavail(flag) {
  var form = document.form2;

  var count = _count_checked();
  if (count == 0) { alert("No Selection!!"); return; }

  if (!confirm("Mark the selected "+ count +" CE(s) as available?")) return;

  if (flag) form.mode2.value = '1';
  else form.mode2.value = '0';

  form.target = 'hiddenframe';
  form.mode.value = 'setavail';
  form.submit();
}
</script>
EOS;

  print<<<EOS
<iframe name='hiddenframe' width='100' height='100' style="display:none"></iframe>
EOS;

  $hi = "#,,ID,name,sInfra,banned,available,lastUpdate,elapsed,maxRunningTime,free,free(%),total,limit";
  table_head($hi);
  print<<<EOS
<form name='form2' action='$env[self]' method='post'>
<input type='hidden' name='mode' value=''>
<input type='hidden' name='mode2' value=''>
EOS;

  // 변수명#옵션
  // 옵션 _:일반변수, L:왼쪽정렬 R:오른쪽정렬 C:가운데정렬
  $tdk = "cb#_,id,name,SIname,banned#_,avail#_,lastUpdateTime,elapsed#_,maxRunningTime,freeCPU,percent#_R,totalCPU,limitCPU";
  $items = preg_split("/,/", $tdk);
  
  $count_banned = 0;
  $count_avail  = 0;
  $count_total = 0;

  $sum = array();
  $cnt = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;
    $count_total++;

    //dd($row);
    //var_dump($row);
    $id = $row['id'];
    //$var['id'] = span_link($id, "_view_md('$id')");

    $t1 = $row['lastUpdateTime'];
    $t2 = date('Y-m-d H:i:s');
    $var['elapsed'] = diff_time($t2, $t1);

    if ($row['avail'] == '1') {
      $count_avail++;
      $var['avail'] = 'avail';
    } else {
      $var['avail'] = '-';
    }

    if ($row['banned'] == '1') {
      $count_banned++;
      $var['banned'] = 'banned';
    } else {
      $var['banned'] = '-';
    }

    $free = $row['freeCPU'];   $sum['free'] += $free;
    $total = $row['totalCPU']; $sum['total'] += $total;
    $limit = $row['limitCPU']; $sum['limit'] += $limit;
    $var['percent'] = sprintf("%2.1f%%", $free/$total*100);

    $var['cb'] =<<<EOS
<input type='checkbox' name='cb[]' value='$id'>
EOS;

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
function _view_md(mjid) {
  var url = "$env[self]?mode=view&mjid="+mjid;
  document.location.href = url;
}
EOS;

  $free = $sum['free'];
  $total = $sum['total'];
  $limit = $sum['limit'];
  $percent = sprintf("%2.1f%%", $sum['free']/$sum['total']*100);
  $hi = "#,,,$count_total,,$count_banned,$count_avail,,,,$free,$percent,$total,$limit";
  table_head($hi, $table_open=false);

  print<<<EOS
</form>
EOS;
  table_close($script);
}


// metajob 상세정보
function _view() {
  global $form;
  global $env;

  $mjid = $form['mjid'];

  ptitle('Subjob Statistics', 1);

  $qry = "SELECT X.*, A.host
 FROM (SELECT COUNT(*) count, status, agent_id FROM job WHERE metajob_id='$mjid' GROUP BY status, agent_id) X
 LEFT JOIN Agent A ON X.agent_id=A.id
 ORDER BY A.host";

  $ret = db_query($qry);

  $info = array();
  $aids = array();
  while ($row = mysql_fetch_assoc($ret)) {
    //dd($row);
    $count = $row['count'];
    $status = $row['status'];
    $aid = $row['agent_id'];
    $host = $row['host'];

    if ($aid == '') continue;

    $aids[$aid] = true;
    $info[$aid][$status] += $count;
    $info[$aid]['host'] = $host;
  }
  //dd($info);

  print<<<EOS
<table border='1' class='mmdata'>
<tr>
<th>#</th>
<th>agent_id</th>
<th>host</th>
<th>done</th>
<th>running</th>
<th>failed</th>
</tr>
EOS;
  $cnt = 0;
  $sum = array();
  foreach ($aids as $aid => $t) {
    $cnt++;
    //dd($aid);

    $v1 = $info[$aid]['done'];    $sum['v1'] += $v1;
    $v2 = $info[$aid]['running']; $sum['v2'] += $v2;
    $v3 = $info[$aid]['failed'];  $sum['v3'] += $v3;

    $host = $info[$aid]['host'];

    print<<<EOS
<tr>
<td>{$cnt}</td>
<td>{$aid}</td>
<td>{$host}</td>
<td>{$v1}</td>
<td>{$v2}</td>
<td>{$v3}</td>
</tr>
EOS;
  }

  print<<<EOS
<tr>
<th></th>
<th></th>
<th>Total</th>
<th>{$sum['v1']}</th>
<th>{$sum['v2']}</th>
<th>{$sum['v3']}</th>
</tr>
</table>
EOS;


  $select_items = "MJ.*, U.userid, U.service_Infra_id, A.name appname";
  $sql_join = " LEFT JOIN User U ON MJ.user_id=U.id"
             ." LEFT JOIN application A ON MJ.app_id=A.id";

  $qry = "SELECT $select_items FROM metajob MJ $sql_join WHERE MJ.id='$mjid' $sql_order";
  //dd($qry);
  $ret = db_query($qry);

  $row = mysql_fetch_assoc($ret);
  //dd($row);

  ptitle('Metajob Raw Data', 1);
  print<<<EOS
<table border='1' class='mmdata'>
EOS;

  $keys = array_keys($row);
  //dd($keys);

  $jsdl = $row['JSDL'];
  $jsdl = htmlspecialchars($jsdl); 
  $jsdl = nl2br($jsdl);
  $row['JSDL'] = $jsdl;

  foreach ($keys as $key) {
    //print $key;
    print<<<EOS
<tr>
 <th>$key</th>
 <td class='l'>{$row[$key]}</td>
</tr>
EOS;
  }

  print<<<EOS
</table>
EOS;

}

function _set_ce_avail($id, $avail_flag) {
  if ($avail_flag) {
    $qry = "UPDATE CE SET available=true WHERE id='$id'";
  } else {
    $qry = "UPDATE CE SET available=false WHERE id='$id'";
  }
  $ret = mysql_query($qry);
  //print("$qry<br>");
}


### function }}}

### modes {{{
if ($mode == 'setavail') {
  //dd($form);

  $cbs = $form['cb']; // array
  $mode2 = $form['mode2'];

  foreach ($cbs as $cb) {
    print $cb;
    if ($mode2 == '1') _set_ce_avail($cb, true);
    else _set_ce_avail($cb, false);
  }

  print<<<EOS
<script>
//alert("Changed !!");
parent.document.location.reload();
</script>
EOS;
  exit;
}

### modes }}}

  pagehead($page_title);
  ptitle('CE List');
  _style();

  _list();

  pagetail();
  exit;

?>
