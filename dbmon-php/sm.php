<?php

  include("common.php");

### function {{{
function _style() {
  print<<<EOS
<style>
span.sel { background-color:yellow; }

span.label { background-color:#444; border:2px solid #999; padding:3 3 3 3px; color:#fff; }

div.bigwarn { font-size:20pt; color:#880000; padding:10 10 20 10px;  }
div.bigwarn span.strong { font-size:20pt; color:#ff0000; }
</style>
EOS;
}

function _exec($cmd, &$output) {
  $command = $cmd;
  $ret = exec($command, $output, $retval);
}

function _get_type($cmd) {

  $type = '';

  if (preg_match("/^javac/", $cmd)) return $type;

  if (preg_match("/kisti.htc.udmanager.server/", $cmd)) {
    $type = 'UD';

  } else if (preg_match("/activeMQ.bin.run.jar/", $cmd)) {
    $type = 'MQ';

  } else if (preg_match("/kisti.htc.dbmanager.server/", $cmd)) {
    $type = 'DB';

  } else if (preg_match("/kisti.htc.monitoring.server/", $cmd)) {
    $type = 'MN';

  } else if (preg_match("/kisti.htc.agentmanager/", $cmd)) {
    $type = 'AM';

  } else if (preg_match("/kisti.htc.acmanager.server/", $cmd)) {
    $type = 'AC';

  } else if (preg_match("/kisti.htc.jobmanager.server/", $cmd)) {
    $type = 'JM';
  }

  return $type;
}

function _truncate_cmd($cmd) {

  $plen = 60;

  $a = substr($cmd, 0, $plen);

  $len = strlen($cmd);
  $z = substr($cmd, $len-$plen, $len-1);

  $str = "$a ... $z";
  return $str;
}

function _process(&$output) {

  $plist = array('MQ'=>true, 'DB'=>true, 'JM'=>true, 'MN'=>true, 'UD'=>true, 'AC'=>true, 'AM'=>true);

  $html =<<<EOS
<table border='1' class='mmdata'>
<tr>
<th>pid</th>
<th>ppid</th>
<th>user</th>
<th>state</th>
<th>etime</th>
<th>time</th>
<th>cpu</th>
<th>mem</th>
<th>type</th>
<th>command</th>
</tr>
EOS;

  foreach ($output as $line) {
    $line = trim($line);
    $data = preg_split("/ +/", $line, 9);
    //print_r($data);

    $pid   = $data[0];
    $ppid  = $data[1];
    $user  = $data[2];
    $state = $data[3];
    $etime = $data[4];
    $time  = $data[5];
    $cpu   = $data[6];
    $mem   = $data[7];
    $cmd   = $data[8];

    // 특정 패턴이 들어있으면 어떤 데몬인지 판단
    $type = _get_type($cmd);
    if (!$type) continue;

    unset($plist[$type]);

    $cmd_s = _truncate_cmd($cmd);

    $html .=<<<EOS
<tr>
<td nowrap>$pid</td>
<td nowrap>$ppid</td>
<td nowrap>$user</td>
<td nowrap>$state</td>
<td nowrap>$etime</td>
<td nowrap>$time</td>
<td nowrap>$cpu</td>
<td nowrap>$mem</td>
<td nowrap>$type</td>
<td class='l'>$cmd_s</td>
</tr>
EOS;
  }
  $html .=<<<EOS
</table>
EOS;
  if ($plist) {
    //dd($plist);
    $missing = join(", ", array_keys($plist));
    print<<<EOS
<div class='bigwarn'>
<span class='strong'>$missing</span> daemon(s) are not running !!!
</div>
<script>
parent.top.document.title = "ERROR:$missing daemon(s) are not running!!!";
</script>
EOS;
  }

  print $html;

}

function _health_graph() {
  print<<<EOS
<table border='0'>
<tr>
<td valign='top'>
 eth0 traffic (3 hours)<br>
 <img src='/health/eth0-3hours.png'><br>
 eth0 traffic (32 hours)<br>
 <img src='/health/eth0-32hours.png'><br>
</td>
<td valign='top'>
 loadavg (3 hours)<br>
 <img src='/health/loadavg-3hours.png'><br>
 loadavg (32 hours)<br>
 <img src='/health/loadavg-32hours.png'><br>
</td>
</tr>
</table>
<a href='/health/'>health monitor</a>
EOS;
}


### function }}}

  pagehead('Server Monitor');
  _style();

  ptitle('Server Monitor');

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
  span.innerHTML = "" + timer + " sec";
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



  // ps -eo pid,ppid,user,etime,time,%cpu,%mem,state,cmd
  $cmd = "ps -eo pid,ppid,user,state,etime,time,%cpu,%mem,cmd  | grep java";
  _exec($cmd, $output);
  //print_r($output);

  _process($output);
/*
  $str = join("\n", $output);
  print<<<EOS
<pre>
$str
</pre>
EOS;
*/

  _health_graph();


  pagetail();
  exit;

?>
