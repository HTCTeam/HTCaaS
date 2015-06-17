<?php

  include("common.php");


### function {{{

function _total_count($sql_from_join_where) {
  $qry = "SELECT COUNT(*) AS total $sql_from_join_where";
  $ret = db_query($qry);
  $row = mysql_fetch_assoc($ret);
  $total = $row['total'];
  return $total;
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

function _view($sjid, $mjid) {
  global $form;

  $select_items = "R.*, J.seq";
  $sql_join = " LEFT JOIN job J ON R.job_id=J.id";

  if ($sjid) $sql_where = "R.job_id='$sjid'";
  else if ($mjid) $sql_where = "R.metajob_id='$mjid'";
  else die('Error'.':'.__FILE__.':'.__LINE__);

  $sql_order = " ORDER BY R.id DESC";

  $sql_from_join_where = "FROM result R $sql_join WHERE $sql_where";
  $total = _total_count($sql_from_join_where);
  $ipp = 100;
  $page = $form['page'];

  list($start, $last, $page) = pager_calc_page($ipp, $total);
  //dd("$total, $start, $last, $page");


  global $mode;
  $id = $form['id'];
  print<<<EOS
<form name='sform'>
<input type='hidden' name='mode' value='$mode'>
<input type='hidden' name='id' value='$id'>
<input type='hidden' name='mjid' value='$mjid'>
<input type='hidden' name='page' value='$page'>
</form>
EOS;
  // 페이지 이동
  $html = pager_html($total, $page, $last, $ipp, 'sform');
  print $html;


  $qry = "SELECT $select_items FROM result R $sql_join WHERE $sql_where $sql_order";
  $qry .= " LIMIT $start,$ipp";
  //dd($qry);
  $ret = db_query($qry);

  $hi = "#,id,LFN,size,job_id,metajob_id,seq,download";
  table_head($hi);

  // 변수명#옵션
  // 옵션 _:일반변수, L:왼쪽정렬 R:오른쪽정렬 C:가운데정렬
  $tdk = "id,LFN#L,size#_R,job_id,metajob_id,seq,download#_";
  $items = preg_split("/,/", $tdk);

  $cnt = 0;
  $sizesum = 0;
  while ($row = mysql_fetch_assoc($ret)) {
    $cnt++;

    //dd($seq);
    //dd($row);

    $id = $row['id'];
    $seq = $row['seq'];

    $fields = array();
    $fields[] = array($cnt, '');

    $var = array();

    $lfn = $row['LFN'];

    $size = filesize($lfn);
    $sizesum += $size;

    list($s, $u) = FormatByteDown($size,3,1);
    $var['size'] = "$s$u";

    $var['download'] =<<<EOS
<a href="DownloadJobFiles.php?mode=sj&mjid=$mjid&seq=$seq">down</a>
EOS;

    _push($fields, $items, $row, $var);

    print("<tr>");
    for ($i = 0; $i < count($fields); $i++) {
      list($str, $attr) = $fields[$i];
      table_data_text($str, $attr, $nowrap=1);
    }
    print("</tr>");
  }

  $script=<<<EOS
function _down(mjid) {
  var url = "result.php?mode=download&mjid="+mjid;
  document.location.href = url;
}
EOS;

  list($s, $u) = FormatByteDown($sizesum,3,1);
  $dn = span_link('download', "_down('$mjid')");
  $ss = "$s$u<br>$dn";

  $hi = "#,,,$ss,,,";
  table_head($hi, $table_open=false);

  table_close($script);

}


### function }}}

// 주어진 mjid 결과를 다운로드함
if ($mode == 'download') {
  $mjid = $form['mjid'];

  $qry = "SELECT * FROM metajob WHERE id='$mjid'";
  $ret = db_query($qry);
  $row = mysql_fetch_assoc($ret);
  if (!$row) die('metajob $mjid not found');

  $qry = "SELECT * FROM result WHERE metajob_id='$mjid'";
  $ret = db_query($qry);

  $files = array();
  while ($row = mysql_fetch_assoc($ret)) {
    //dd($row);
    $file = $row['LFN'];
    $files[] = $file;
  }
  $a = base64_encode(serialize($files));

  header("Content-disposition: attachment; filename=\"$mjid.tar\"");
  header("Content-type: application/octetstream");
  header("Cache-Control: no-cache");
  header("Pragma: no-cache");
  header("Expires: -1");

  $path = $conf['htcaas_php_path'];
  $command =<<<EOS
$path/cli/cli.tar.php "$a"
EOS;
  passthru($command);

  exit;
}

if ($mode == 'view') {

  pagehead();

  $sjid = $form['sjid'];
  $mjid = $form['mjid'];

       if ($sjid) ptitle("Result List of a SubJob id=$sjid");
  else if ($mjid) ptitle("Result List of a MetaJob id=$mjid");
  else die('Error'.':'.__FILE__.':'.__LINE__);

  _view($sjid, $mjid);

  pagetail();
  exit;
}


  pagehead();
  pagetail();
  exit;


?>
