<?php

  include("common.php");


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

function _get_ipp() {
  global $form;
  $ipp = $form['ipp'];
  if ($ipp == '') $ipp = 100; // default
  if ($ipp < 10) $ipp = 10; # min
  else if ($ipp > 10000) $ipp = 10000; # max
  return $ipp;
}

function _listview() {
  global $form;


  $page = $form['page'];
  ## {{
  print<<<EOS
<table border='0'>
<form name='form' method='get' action='$self'>
<tr>

<td>
<input type='hidden' name='mode' value='$mode'>
<input type='hidden' name='page' value='$page'>
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

  $td = $form['td'];
  if ($td == '') $td = date('Y-m-d');
  print<<<EOS
<span class='label'>Date</span><input
 name="td" value="$td" size="10"><a href="javascript:void(0)" onclick="if(self.gfPop)gfPop.fPopCalendar(document.form.td);return false;" ><img class="PopcalTrigger" align="absmiddle" src="/dbmon/utl/DateInput/calbtn.gif" width="34" height="22" border="0" alt=""></a>
<iframe width=174 height=189 name="gToday:normal:agenda.js" id="gToday:normal:agenda.js" src="/dbmon/utl/DateInput/ipopeng.htm" scrolling="no" frameborder="0" style="visibility:visible; z-index:999; position:absolute; top:-500px; left:-500px;"></iframe>
EOS;

  $t1 = $form['t1'];
  $t2 = $form['t2'];
  if ($t1 == '') $t1 = '00:00:00';
  if ($t2 == '') $t2 = '23:59:59';
  print<<<EOS
<span class='label'>Time Range</span>
<input type=text name='t1' size='10' value='$t1' onclick="this.select()">
~ <input type=text name='t2' size='10' value='$t2' onclick="this.select()">
EOS;
  print('  ');
  print('<br>');

  $list = array('','new','pushed','submitted','running','done','canceled','failed'
    ,'running-stopped', 'new-zombie', 'submitted-zombie', 'running-zombie'
    ,'running-zombie-stopped','submit-error','agentQuid');
  $preset = $form['status'];
  $opt = option_general($list, $preset);
  print<<<EOS
<span class='label'>Status</span><select name='status'>$opt</select>
EOS;
  print('  ');

  $preset = $form['userid'];
  $opt = option_user($preset);
  print<<<EOS
<span class='label'>User</span><select name='userid'>$opt</select>
EOS;
  print('  ');


  $ipp = _get_ipp();
  $sel = array(); $sel[$ipp] = ' selected';
  print<<<EOS
  <span class='label'>#/Page</span><select name='ipp'>
<option value='10'$sel[10]>10</option>
<option value='20'$sel[20]>20</option>
<option value='50'$sel[50]>50</option>
<option value='100'$sel[100]>100</option>
<option value='200'$sel[200]>200</option>
<option value='500'$sel[500]>500</option>
<option value='1000'$sel[1000]>1000</option>
<option value='2000'$sel[2000]>2000</option>
</select>
<br>
EOS;
  print('  ');


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


  //dd($form);

  $w = array('1');

  $td = $form['td'];
  $t1 = $form['t1'];
  $t2 = $form['t2'];
  $w[] = "AG.submittedTimestamp >= '$td $t1'";
  $w[] = "AG.submittedTimestamp <= '$td $t2'";

  $v = $form['status'];
  if ($v != '') $w[] = "AG.status='$v'";

  $v = $form['userid'];
  if ($v != '') $w[] = "U.userid='$v'";

  $sql_where = " WHERE ".join(" AND ", $w);


  $select_items = "AG.*, U.userid Uname, CE.name CEname, IF(AG.quit,'quit','') AS AGquit, J.name Jname";

  $sql_from = " FROM agent AG";

  $sql_join = " LEFT JOIN User U ON AG.user_id=U.id"
             ." LEFT JOIN CE ON AG.CE_id=CE.id"
             ." LEFT JOIN job J ON AG.currentJob=J.id"
             ;

  $sql_order = " ORDER BY AG.submittedTimestamp DESC";

  $qry = "SELECT COUNT(*) AS total $sql_from $sql_join $sql_where";
  $ret = db_query($qry);
  $row = mysql_fetch_assoc($ret);
  $total = $row['total'];

  $ipp = _get_ipp();
  $page = $form['page'];
  list($start, $last, $page) = pager_calc_page($ipp, $total);


  $qry = "SELECT $select_items $sql_from $sql_join $sql_where $sql_order";
  $qry .= " LIMIT $start,$ipp";

  // 페이지 이동
  $html = pager_html($total, $page, $last, $ipp, 'form');
  print $html;


  //dd($qry);
  $ret = db_query($qry);

  $hi = "#,agent.id,submitId,status"
       .",host,CE,CE.name,user.name,quit"
       .",submittedTimestamp,runningTime,runningTimestamp,waitingTime,lastSignal"
       .",currentJob,job.name";
  table_head($hi);

  // 변수명#옵션
  // 옵션 _:일반변수, L:왼쪽정렬 R:오른쪽정렬 C:가운데정렬
  $tdk = "id#_,submitId,status"
       .",host,CE_id,CEname,Uname,AGquit"
       .",submittedTimestamp,runningTime,runningTimestamp,waitingTime,lastSignal"
       .",currentJob,Jname";
  $items = preg_split("/,/", $tdk);
  
  $cnt = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;
    //dd($row);

    $id = $row['id'];

    $status = $row['status'];

    $var['id'] = span_link($id, "_view('$id')");

    //$s = $row['startTimestamp'];
    //$e = $row['lastUpdateTime'];
    //$var['elapsed'] = diff_time($e, $s);

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
function _view(agid) {
  var url = "$env[self]?mode=view&agid="+agid;
  wopen(url, 500,500,1,1);
}
EOS;
  table_close($script);

}


// agent 상세정보
function _view1() {
  global $form;

  $agid = $form['agid'];

  $sql_from = " FROM agent AG";

  $sql_where = " WHERE AG.id='$agid'";

  $qry = "SELECT AG.* $sql_from $sql_where";

  //dd($qry);
  $ret = db_query($qry);

  $row = mysql_fetch_assoc($ret);

  ptitle('Agent Raw Data', 1);
  print<<<EOS
<table border='1' class='mmdata'>
EOS;

  $keys = array_keys($row);

  foreach ($keys as $key) {
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

if ($mode == 'view') {

  pagehead();

  $agid = $form['agid'];
  ptitle("agents [id=$agid]");

  _view1();

  pagetail();
  exit;
}


  pagehead();
  _style();
  ptitle('Agent List');

  _listview();

  pagetail();
  exit;


?>
