<?php

  include("common.php");

  $page_title = "MetaJob";

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


function _listview() {
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

  $sd = getvalue($form['sd'], $_SESSION['metajob_sd'], date('Y-m-d'));
  $ed = getvalue($form['ed'], $_SESSION['metajob_ed'], date('Y-m-d'));
  $_SESSION['metajob_sd'] = $sd;
  $_SESSION['metajob_ed'] = $ed;
  print<<<EOS
<span class='label'>Start Date</span>
<input name="sd" value="$sd" size="12" class='a' onkeypress='keypress_text()'$dis><a href="javascript:void(0)" onclick="if(self.gfPop)gfPop.fStartPop(document.form.sd,document.form.ed);return false;" ><img class="PopcalTrigger" align="absmiddle" src="/dbmon/utl/DateRange/calbtn.gif" width="34" height="22" border="0" alt=""></a>
~
<input name="ed" value="$ed" size="12" class='a' onkeypress='keypress_text()'$dis><a href="javascript:void(0)" onclick="if(self.gfPop)gfPop.fEndPop(document.form.sd,document.form.ed);return false;" ><img class="PopcalTrigger" align="absmiddle" src="/dbmon/utl/DateRange/calbtn.gif" width="34" height="22" border="0" alt=""></a>
<iframe width=132 height=142 name="gToday:contrast:agenda.js" id="gToday:contrast:agenda.js" src="/dbmon/utl/DateRange/ipopeng.htm" scrolling="no" frameborder="0" style="visibility:visible; z-index:999; position:absolute; top:-500px; left:-500px;">
</iframe>
EOS;

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
  //document.location.reload();
}
function _countdown() {
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



  $select_items = "MJ.*, U.userid, U.service_Infra_id, A.name appname";
  $sql_join = " LEFT JOIN User U ON MJ.user_id=U.id"
             ." LEFT JOIN application A ON MJ.app_id=A.id";

  $w = array('1');
  $sd = $form['sd'];
  $ed = $form['ed'];
  $w[] = "DATE(MJ.startTimestamp) >= '$sd'";
  $w[] = "DATE(MJ.startTimestamp) <= '$ed'";
  $sql_where = join(" AND ", $w);

  $sql_order = " ORDER BY MJ.startTimestamp DESC";

  $qry = "SELECT $select_items FROM metajob MJ $sql_join WHERE $sql_where $sql_order";
  //dd($qry);
  $ret = db_query($qry);

  $hi = "#,id,status,user,appname,startTime,updateTime,elapsed,num,total,subjobs,results";
  table_head($hi);

  // 변수명#옵션
  // 옵션 _:일반변수, L:왼쪽정렬 R:오른쪽정렬 C:가운데정렬
  $tdk = "id#_,status,userid,appname,startTimestamp,lastUpdateTime,elapsed#_,num,total,subjobs#_,results#_";
  $items = preg_split("/,/", $tdk);
  
  $cnt = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;
    //dd($row);
    $id = $row['id'];
    $var['id'] = span_link($id, "_view_md('$id')");

    $var['subjobs'] = span_link('subjobs', "_list_subjobs('$id')");
    $var['results'] = span_link('results', "_list_results('$id')");

    $s = $row['startTimestamp'];
    $e = $row['lastUpdateTime'];
    $var['elapsed'] = diff_time($e, $s);

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
  wopen(url,1000,1000,1,1);
  //document.location.href = url;
}
function _list_subjobs(id) {
  var url = "subjob.php?mode=view&id="+id;
  document.location.href = url;
}
function _list_results(mjid) {
  var url = "result.php?mode=view&mjid="+mjid;
  document.location.href = url;
}
EOS;
  table_close($script);

}

// metajob 상세정보
function _view() {
  global $form;
  global $env;

  $mjid = $form['mjid'];

##{{
  ptitle('Subjob Statistics', 1);

  $qry = "SELECT X.*, A.host
 FROM (SELECT COUNT(*) count, status, agent_id FROM job WHERE metajob_id='$mjid' GROUP BY status, agent_id) X
 LEFT JOIN Agent A ON X.agent_id=A.id
 ORDER BY A.host";
  //dd($qry);

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
<th>preparing</th>
<th>running</th>
<th>done</th>
<th>failed</th>
<th>(Total)</th>
</tr>
EOS;
  $cnt = 0;
  $sum = array();
  foreach ($aids as $aid => $t) {
    $cnt++;
    //dd($aid);

    $vsum = 0;
    $v1 = $info[$aid]['preparing']; $sum['v1'] += $v1; $vsum += $v1;
    $v2 = $info[$aid]['running'];   $sum['v2'] += $v2; $vsum += $v2;
    $v3 = $info[$aid]['done'];      $sum['v3'] += $v3; $vsum += $v3;
    $v4 = $info[$aid]['failed'];    $sum['v4'] += $v4; $vsum += $v4;
    $sum['vsum'] += $vsum;

    $host = $info[$aid]['host'];

    unset($info[$aid]);

    print<<<EOS
<tr>
<td>{$cnt}</td>
<td>{$aid}</td>
<td>{$host}</td>
<td>{$v1}</td>
<td>{$v2}</td>
<td>{$v3}</td>
<td>{$v4}</td>
<td>{$vsum}</td>
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
<th>{$sum['v4']}</th>
<th>{$sum['vsum']}</th>
</tr>
</table>
EOS;
  //dd($info);
##}}

  $select_items = "MJ.*, U.userid, U.service_Infra_id, A.name appname";
  $sql_join = " JOIN User U ON MJ.user_id=U.id"
             ." JOIN application A ON MJ.app_id=A.id";

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

### function }}}


### modes {{{
if ($mode == 'view') {
  $mjid = $form['mjid'];

  pagehead($page_title);
  _style();
  ptitle("Metajob Information (id=$mjid)");
  _view();
  pagetail();
  exit;
}
### modes }}}

  pagehead($page_title);
  _style();
  ptitle('MetaJob List');

  _listview();

  pagetail();
  exit;

?>
