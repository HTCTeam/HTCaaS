<?php

function pagehead($title='') {
  global $conf;
  $now = date("Y.m.d H:i:s");

  $url1 = $conf['activemq_url1'];

  print<<<EOS
<html>
<head>
<title>$title</title>

<link rel='stylesheet' type='text/css' href='style.css'/>

<script type="text/javascript" src="script.js"></script>

</head>
<body>
<a href='log.php'>log monitor</a>
:: <a href='sm.php'>server monitor</a>
:: <a href='$url1' target=_blank>activemq</a>
:: <a href='health.php'>health monitor</a>
:: $now
<br>
EOS;
  pagemenu();
}

function pagetail() {
  print<<<EOS
</body>
</html>
EOS;
}

function pagemenu() {
  print<<<EOS
<div class='menu'>
Menu
:: <a href='metajob.php'>metajob</a>
:: <a href='user.php'>user</a>
:: <a href='ce.php'>resource</a>
:: <a href='agent.php'>agent</a>
:: <a href='dbdump.php'>(raw data)</a>
</div>
EOS;
}

function ptitle($title, $level=0) {
  if ($level == 0) {
    print<<<EOS
<div class='ptitle'>
$title
</div>
EOS;
  } else if ($level == 1) {
    print<<<EOS
<div class='ptitle2'>$title</div>
EOS;
  }
}

// for debug
function dd($msg) {
       if (is_string($msg)) print($msg);
  else if (is_array($msg)) { print("<pre>"); print_r($msg); print("</pre>"); }
  else print_r($msg);
}

function table_head($keys, $table_open=true) {

  if (is_string($keys)) {
    $list = preg_split("/,/", $keys);
  } else if (is_array($keys)) {
    $list = $keys;
  } else {
    die('Error'.':'.__FILE__.':'.__LINE__);
  }

  if ($table_open) {
    print<<<EOS
<table border='1' class='mmdata'>
EOS;
  }

  print("<tr>");
  foreach ($list as $a) {
    print<<<EOS
<th>$a</th>
EOS;
  }
  print("</tr>");
}

function  table_close($script='') {
  print<<<EOS
</table>

<script>
$script
</script>
EOS;
}

function table_data_row(&$row, $keys, $attr, $nowrap=0) {

  if (is_string($keys)) {
    $list = preg_split("/,/", $keys);
  } else if (is_array($keys)) {
    $list = $keys;
  } else {
    die('Error'.':'.__FILE__.':'.__LINE__);
  }

  foreach ($list as $a) {
    $val = $row[$a];
    table_data_text($val, $attr, $nowrap);
  }
}
function table_data_text($text, $attr, $nowrap=0) {
  if ($nowrap) $attr .= " nowrap";

  print<<<EOS
<td$attr>$text</td>
EOS;
}


function db_query($qry, $print=false) {
  //$print = true;
  if ($print) print("QUERY : $qry\n");
  $ret = mysql_query($qry);
  $err = mysql_error();
  if ($err) {
    print("Query : $qry");
    print("ERROR : $err");
  }
  return $ret;
}

function span_link($title, $onclick) {
  $html=<<<EOS
<span class='link' onclick="$onclick">$title</span>
EOS;
  return $html;
}


function Pager_f($formname, $page, $total, $ipp) {
  global $conf, $env;
  $html = '';

  $btn_prev = "Prev";
  $btn_next = "Next";
  $btn_prev10 = "<<<";
  $btn_next10 = ">>>";

  $last = ceil($total/$ipp);
  if ($last == 0) $last = 1;

  $start = floor(($page - 1) / 10) * 10 + 1;
  $end = $start + 9;

  //dd("$formname / page=$page / total=$total / ipp=$ipp / start=$start / last=$last / end=$end");

  $html .= "<table border='0' cellpadding='2' cellspacing='0'><tr>"; # table 1

  $attr1 = " onmouseover=\"this.className='pager_on'\""
         ." onmouseout=\"this.className='pager_off'\""
         ." class='pager_off' align='center' style='cursor:pointer;'";
  $attr2 = " onmouseover=\"this.className='pager_sel_on'\""
         ." onmouseout=\"this.className='pager_sel_off'\""
         ." class='pager_sel_off' align='center' style='cursor:pointer;'";
 
  # previous link
  if ($start > 1) {
    $prevpage = $start - 1;
    $html .= "<td$attr1 align=center onclick=\"pager_Go('$prevpage')\">$btn_prev10</td>\n";
  } else $html .= "<td align=center class='pager_static'>$btn_prev10</td>\n";

  if ($page > 1) {
    $prevpage = $page - 1;
    $html .= "<td$attr1 align=center onclick=\"pager_Go('$prevpage')\">$btn_prev</td>\n";
  } else $html .= "<td align=center class='pager_static'>$btn_prev</td>\n";


  if ($end > $last) $end = $last;
  $html .= "</td>";
  for ($i = $start; $i <= $end; $i++) {
    $s = "$i";
    if ($i != $page) {
      $html .= "<td$attr1 onclick=\"pager_Go('$i')\">$s</td>\n";
    } else {
      $html .= "<td$attr2>$s</td>\n";
    }
  }

  # next link
  if ($page < $last) {
    $nextpage = $page + 1;
    $html .= "<td$attr1 align=center onclick=\"pager_Go('$nextpage')\">$btn_next</td>\n";
  } else $html .= "<td align=center class='pager_static'>$btn_next</td>\n";

  if ($end < $last) {
    $nextpage = $end + 1;
    $html .= "<td$attr1 align=center onclick=\"pager_Go('$nextpage')\">$btn_next10</td>\n";
  } else $html .= "<td align=center class='pager_static'>$btn_next10</td>\n";

  $html .= "</tr></table>\n";
  $html .=<<<EOS
<script>
function pager_Go(page) {
  document.$formname.page.value = page;
  document.$formname.submit();
}
</script>
EOS;
  return $html;
}

function pager_html($total, $page, $last, $ipp, $formname) {
  global $env;
  global $form;

  if (!$total) $tot_s = '0';
  else $tot_s = number_format($total);

  $pager = Pager_f($formname, $page, $total, $ipp);

  $html=<<<EOS
<table border='0' cellpadding='3' cellspacing='1'>
<tr><td style="border:3px solid #eeeeee;">
<table border='0' width='600'>
<tr>
<td align='center'>$pager</td>
<td align='center'>TOTAL {$tot_s}&nbsp;&nbsp;PAGE $page/{$last}</td>
</tr>
</table>
</td></tr></table>
EOS;
  return $html;
}


// 페이지 계산
//   list($start, $last, $page) = pager_calc_page($ipp, $total);
function pager_calc_page($ipp, $total) {
  global $form;

  $page = $form['page'];
  if ($page == '') $page = 1;
  $last = ceil($total/$ipp);
  if ($last == 0) $last = 1;
  if ($page > $last) $page = $last;
  $start = ($page-1) * $ipp;

  return array($start, $last, $page);
}


// 시간 뺄셈
// $ts1 - $ts2
// return: 11d 22h 33m 44s
function diff_time($ts1, $ts2) {
  if ($ts1 == '') return '';
  if ($ts2 == '') return '';

  @list($y,$m,$d,$h,$i,$s) = preg_split("/[- :]/", $ts1);
  @$t1 = mktime($h,$i,$s,$m,$d,$y);
  //if ($t1 == 943887600) return "--d --h --m --s";

  @list($y,$m,$d,$h,$i,$s) = preg_split("/[- :]/", $ts2);
  @$t2 = mktime($h,$i,$s,$m,$d,$y);

  unset($h,$i,$m,$d,$y);

  $s = $t1 - $t2;
  if ($s > 60) {
    $m = floor($s / 60);
    $s = $s % 60;
  } else $m = '0';
  if ($m > 60) {
    $h = floor($m / 60);
    $m = $m % 60;
  } else $h = '0';
  if ($h > 24) {
    $d = floor($h / 24);
    $h = $h % 24;
  } else $d = '0';

  if ($d) {
    $elapsed = "{$d}d {$h}h {$m}m {$s}s";
  } else if ($h) {
    $elapsed = "{$h}h {$m}m {$s}s";
  } else if ($m) {
    $elapsed = "{$m}m {$s}s";
  } else if ($s) {
    $elapsed = "{$s}s";
  }

  //$elapsed = "{$d}d {$h}h {$m}m {$s}s";
  return $elapsed;
}

// select option (general)
function option_general($list, $preset) {
  $opts = "<option value=''>= select =</option>";
  if (!in_array($preset, $list)) $list[] = $preset;
  foreach ($list as $v) {
    if ($v == $preset) $s = ' selected'; else $s = '';
    $opts .= "<option value='$v'$s>$v</option>";
  }
  return $opts;
}

// select option (user)
function option_user($preset) {
  $qry = "SELECT userid FROM user ORDER BY userid";
  $ret = mysql_query($qry);

  $opts = "<option value=''>= select =</option>";
  while ($row = mysql_fetch_assoc($ret)) {
    $v = $row['userid'];
    if ($v == $preset) $s = ' selected'; else $s = '';
    $opts .= "<option value='$v'$s>$v</option>";
  }
  return $opts;
}


# format byte data (reused from phpMyAdmin)
//list($fs, $fu) = FormatByteDown($bytes,3,1);
function FormatByteDown($value, $limes=6, $comma=0) {
  $dh           = pow(10, $comma);
  $li           = pow(10, $limes);
  $return_value = $value;

  $byteUnits    = array('Bytes', 'KB', 'MB', 'GB');
  $unit         = $byteUnits[0];

  if ($value >= $li*1000000) {
    $value = round($value/(1073741824/$dh))/$dh;
    $unit  = $byteUnits[3];
  } else if ($value >= $li*1000) {
    $value = round($value/(1048576/$dh))/$dh;
    $unit  = $byteUnits[2];
  } else if ($value >= $li) {
    $value = round($value/(1024/$dh))/$dh;
    $unit  = $byteUnits[1];
  }

  if ($unit != $byteUnits[0]) {
    $return_value = number_format($value, $comma, '.', ',');
  } else {
    $return_value = number_format($value, 0, '.', ',');
  }

  return array($return_value, $unit);
}


function getvalue($v1, $v2, $v3) {
       if ($v1) return $v1;
  else if ($v2) return $v2;
  else          return $v3;
}



?>
