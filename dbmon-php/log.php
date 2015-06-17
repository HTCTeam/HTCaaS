<?php

  include("common.php");

  $page_title = 'Log View';

### function {{{

function _style() {

  global $form;
  $ftsz = $form['ftsz'];

       if ($ftsz == '1') $sty = "font-size:4pt; line-height:90%;";
  else if ($ftsz == '2') $sty = "font-size:8pt; line-height:90%;";
  else if ($ftsz == '3') $sty = "font-size:10pt; line-height:100%;";
  else if ($ftsz == '4') $sty = "font-size:11pt; line-height:110%;";
  else if ($ftsz == '5') $sty = "font-size:12pt; line-height:120%;";

  print<<<EOS
<style>
span.sel { background-color:yellow; }

div.log { margin:5 5 5 5px; padding:0 0 0 0px; border:0; background-color:#eeeeee; }
div.log pre.code { margin:0 0 0 0; padding:0 0 0 0; width:100%; overflow:hidden; border:0px solid red; $sty }
div.log pre.c1 { color:red; }
div.log pre.c2 { color:blue; }
div.log pre.c3 { color:green; }
div.log pre.chigh { color:#000; background-color:yellow; }

span.label { background-color:#444; border:2px solid #999; padding:3 3 3 3px; color:#fff; }
div.title { font-weight:normal; font-size:11pt; color:blue; }
</style>
EOS;
}

function _exec($cmd, &$output) {
/*
  $ssh = "/usr/bin/ssh -p 8222  -i /www/dbmon/ssh/id_rsa"
    ." -o UserKnownHostsFile=/www/dbmon/ssh/knownhosts"
    ." -o StrictHostKeyChecking=no"
    ." p330ksw@150.183.249.51";

  $command = "$ssh $cmd";
  //print $command;

  $ret = exec($command, $output, $retval);
*/

  $command = $cmd;
  $ret = exec($command, $output, $retval);
  
}

function _highlight(&$line, $mode) {
  global $form;

  $highl = $form['highl'];

  // 기본적으로 ERORR 는 highligh 처리
  if ($highl == '') $highl = "ERROR,error";
  //if (!$highl) return '';

  // 콤마가 포함되면 콤마로 나누어서 egrep 으로 처리
  if (preg_match("/,/", $highl)) {
    $items = preg_split("/,/", $highl);

    $len = count($items);
    foreach ($items as $k) {
      if (preg_match("/$k/", $line)) { $cls = 'chigh'; break; }
    }

  } else {

    if (preg_match("/$highl/", $line)) $cls = 'chigh';

  }
  return $cls;

}

// 로그메시지 앞부분 시간 [2013-10-16 16:13:13] 
// 지금으로 부터 몇 초 전인지
function _logtime($line) {
  $now = time();

  $a = substr($line, 0, 1);
  if ($a != "[") return -1; // error (포맷이 안 맞음)

  //0123456789012345678901
  // [2013-10-02 03:41:14] log message
  $timestr = substr($line, 1, 19);
  list($y, $m, $d, $h, $i, $s) = preg_split("/[ :-]/", $timestr);
  //print("$y,$m,$d,$h,$i,$s<br>");

  $t1 = mktime($h, $i, $s, $m, $d, $y);
  //$t1 += 8*3600;

  $str = $now - $t1;
  return $str;
}

function _process(&$output, $mode, $latest) {
  global $form;
  //dd($output);

  if ($latest == 'unlimited') {
    $timelimit = false;
  } else {
    $timelimit = true;
  }


  print<<<EOS
<div class='log'>
EOS;

  foreach ($output as $line) {
    $line = trim($line);

    if ($timelimit) {
      $lt = sprintf("%3d", _logtime($line));
      if ($lt >= 0) {
        if ($lt > $latest) continue;

      }
    }

    $cls = _highlight($line, $mode);

    print<<<EOS
<pre class='code $cls'>$lt $line</pre>
EOS;
  }
  print<<<EOS
</div>
EOS;

}

function _do($log) {
  global $form;

  // 라인개수
  $lines = getvalue($form['lines'], $_SESSION['log_lines'], 30);
  if (!$lines) $lines = 30;

  $search = $form['search'];
  if ($search) {

    // 콤마가 포함되면 콤마로 나누어서 egrep 으로 처리
    $items = preg_split("/,/", $search);
    $len = count($items);
    if ($len > 1) {
      $tmp = array();
      foreach ($items as $k) {
        $tmp[] = trim($k);
      }
      $find = join("|", $tmp);
      $cmd = "egrep \"$find\" $log | tail -n $lines";

    } else {
      $cmd = "grep $search $log | tail -n $lines";
    }

  } else {
    $cmd = "tail -n $lines  $log";
  }

  $t1 = microtime(true);

  _exec($cmd, $output);
  //dd($output);

  $t2 = microtime(true);
  $diff = sprintf("%10.3f", $t2 - $t1);

 print<<<EOS
<div class='title'>
$log (elapsed $diff)
</div>
EOS;


  // reverse order
  if ($form['reod']) $output = array_reverse($output);

  // 최근 몇초까지의 로그만 출력함
  $latest = getvalue($form['latest'], $_SESSION['log_latest'], 'unlimited');

  _process($output, $mode, $latest);
}

function _getvalue_checkboxes() {
  global $form;

  $lgac = $form['lgac'];
  $lgam = $form['lgam'];
  $lgdb = $form['lgdb'];
  $lgjm = $form['lgjm'];
  $lgmn = $form['lgmn'];
  $lgud = $form['lgud'];

  $cflag = false; // checked if any one
  if ($lgac or $lgam or $lgdb or $lgjm or $lgmn or $lgud) $cflag = true;
  if (!$cflag) {
    // get from the session
    list($lgac, $lgam, $lgdb, $lgjm, $lgmn, $lgud) = $_SESSION['lgchks'];
  }

  $cflag = false; // checked if any one
  if ($lgac or $lgam or $lgdb or $lgjm or $lgmn or $lgud) $cflag = true;
  if (!$cflag) {
    $lgac = 'on'; // check one of them
  }

  // session save
  $_SESSION['lgchks'] = array($lgac, $lgam, $lgdb, $lgjm, $lgmn, $lgud);

  return array($lgac, $lgam, $lgdb, $lgjm, $lgmn, $lgud);
}

### function }}}


  pagehead($page_title);
  _style();

  ptitle('Log View');

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


  list($lgac, $lgam, $lgdb, $lgjm, $lgmn, $lgud) = _getvalue_checkboxes();

  $cflag = false;
  if ($lgac or $lgam or $lgdb or $lgjm or $lgmn or $lgud) $cflag = true;
  if (!$cflag) $lgac = 'on';


  if ($lgac) { $ck1 = ' checked'; } else $ck1 = '';
  if ($lgam) { $ck2 = ' checked'; } else $ck2 = '';
  if ($lgdb) { $ck3 = ' checked'; } else $ck3 = '';
  if ($lgjm) { $ck4 = ' checked'; } else $ck4 = '';
  if ($lgmn) { $ck5 = ' checked'; } else $ck5 = '';
  if ($lgud) { $ck6 = ' checked'; } else $ck6 = '';

  print<<<EOS
<span class='label'>Log</span>
<input type='checkbox' name='lgac' id='lgac'$ck1><label for='lgac'>AC</label>
<input type='checkbox' name='lgam' id='lgam'$ck2><label for='lgam'>AM</label>
<input type='checkbox' name='lgdb' id='lgdb'$ck3><label for='lgdb'>DB</label>
<input type='checkbox' name='lgjm' id='lgjm'$ck4><label for='lgjm'>JM</label>
<input type='checkbox' name='lgmn' id='lgmn'$ck5><label for='lgmn'>MN</label>
<input type='checkbox' name='lgud' id='lgud'$ck6><label for='lgud'>UD</label>
EOS;
  print("&nbsp;&nbsp;");

  $v = $form['ftsz']; $sel = array(); if (!$v) { $v = 3; } $sel[$v] = ' checked';
  print<<<EOS
<span class='label'>Font</span>
<input type='radio' name='ftsz' id='ftsz1' value='1'$sel[1]><label for='ftsz1'>1(small)</label>
<input type='radio' name='ftsz' id='ftsz2' value='2'$sel[2]><label for='ftsz2'>2</label>
<input type='radio' name='ftsz' id='ftsz3' value='3'$sel[3]><label for='ftsz3'>3(normal)</label>
<input type='radio' name='ftsz' id='ftsz4' value='4'$sel[4]><label for='ftsz4'>4</label>
<input type='radio' name='ftsz' id='ftsz5' value='5'$sel[5]><label for='ftsz5'>5(large)</label>
EOS;
  print("<br>");

  if ($form['reod']) $ck = ' checked'; else $ck = '';
  print<<<EOS
<span class='label'>Order</span><input type='checkbox' name='reod' id='reod'$ck><label for='reod'>reverse</label>
EOS;
  print("&nbsp;&nbsp;");


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

  $v = getvalue($form['lines'], $_SESSION['log_lines'], 30);
  $sel = array(); if (!$v) { $v = 30; } $sel[$v] = ' selected';
  print<<<EOS
<span class='label'>Lines</span><select name='lines'>
<option value='10'$sel[10]>10</option>
<option value='20'$sel[20]>20</option>
<option value='30'$sel[30]>30</option>
<option value='40'$sel[40]>40</option>
<option value='50'$sel[50]>50</option>
<option value='100'$sel[100]>100</option>
<option value='200'$sel[200]>200</option>
<option value='300'$sel[300]>300</option>
</select>
EOS;
  print("&nbsp;&nbsp;");

  $v = $form['search'];
  print<<<EOS
<span class='label'>Grep</span><input type='text' name='search' size='20' value="$v" onclick='this.select()'
 onfocus="stoptimer=1;" onblur="stoptimer=0;">
EOS;
  print("&nbsp;&nbsp;");

  $v = $form['highl'];
  //if (!$v) $v = "ERROR,error";
  print<<<EOS
<span class='label'>Highlight</span><input type='text' name='highl' size='20' value="$v" onclick='this.select()'
 onfocus="stoptimer=1;" onblur="stoptimer=0;">
EOS;
  print("&nbsp;&nbsp;");


  $v = getvalue($form['latest'], $_SESSION['log_latest'], 'unlimited');
  $sel = array(); if (!$v) { $v = 'unlimited'; } $sel[$v] = ' selected';
  print<<<EOS
<span class='label'>Latest</span><select name='latest'>
<option value='10'$sel[10]>10 seconds</option>
<option value='20'$sel[20]>20 seconds</option>
<option value='30'$sel[30]>30 seconds</option>
<option value='60'$sel[60]>60 seconds</option>
<option value='120'$sel[120]>120 seconds</option>
<option value='300'$sel[300]>3000 seconds</option>
<option value='unlimited'{$sel['unlimited']}>unlimited</option>
</select>
EOS;
  print("<br>");



  print<<<EOS
</td>


</tr>
</form>
</table>
EOS;

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

  list($lgac, $lgam, $lgdb, $lgjm, $lgmn, $lgud) = _getvalue_checkboxes();
  //print("lgac=$lgac, lgam=$lgam, lgdb=$lgdb, lgjm=$lgjm, lgmn=$lgmn, lgud=$lgud");

  $server_log_dir = $conf['server_log_dir'];

  $log_ac = "$server_log_dir/ac.log";
  $log_am = "$server_log_dir/am.log";
  $log_db = "$server_log_dir/db.log";
  $log_jm = "$server_log_dir/jm.log";
  $log_mn = "$server_log_dir/mn.log";
  $log_ud = "$server_log_dir/ud.log";

  if ($lgac) {
    $log = $log_ac;
    _do($log);
  }

  if ($lgam) {
    $log = $log_am;
    _do($log);
  }

  if ($lgdb) {
    $log = $log_db;
    _do($log);
  }

  if ($lgjm) {
    $log = $log_jm;
    _do($log);
  }

  if ($lgmn) {
    $log = $log_mn;
    _do($log);
  }

  if ($lgud) {
    $log = $log_ud;
    _do($log);
  }

  pagetail();
  exit;

?>
