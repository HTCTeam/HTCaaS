<?php

  include("common.php");


### function {{{


### function }}}

  pagehead();

  ptitle('Health Monitor');

  $url1_title = "http://amga.kisti.re.kr/health/";
  $url1 = "http://amga.kisti.re.kr/health/";

  $url2_title = "http://150.183.158.172/health/";
  $url2 = "http://150.183.249.51:9005/health/";

  $attr = " align='center'";
  $atr2 = " width='481' height='225' border='1'";
  print<<<EOS
<table border='0'>
<tr>
<td $attr colspan='2'><a href='$url1' target='_blank'>$url1_title</a></td>
</tr>
<tr>
<td $attr><a href='$url1/loadavg.html'><img src='$url1/loadavg-3hours.png' $atr2></a></td>
<td $attr><a href='$url1/loadavg.html'><img src='$url1/loadavg-32hours.png' $atr2></a></td>
</tr>

<tr>
<td $attr colspan='2'><a href='$url2' target='_blank'>$url2_title</a></td>
</tr>
<tr>
<td $attr><a href='$url2/loadavg.html'><img src='$url2/loadavg-3hours.png' $atr2></a></td>
<td $attr><a href='$url2/loadavg.html'><img src='$url2/loadavg-32hours.png' $atr2></a></td>
</tr>
</table>
EOS;


  pagetail();
  exit;

?>
