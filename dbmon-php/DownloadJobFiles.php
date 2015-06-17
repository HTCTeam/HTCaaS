<?php

// DownloadJobFiles.php

  include("common.php");


### function {{{

### function }}}

// 주어진 metajob 결과를 모두 다운로드함
// DownloadJobFiles.php?mode=mj&mjid=아이디
if ($mode == 'mj') {

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

// 주어진 subjob 결과를 모두 다운로드함
// DownloadJobFiles.php?mode=sj&mjid=아이디&seq=시퀀스번호
else if ($mode == 'sj') {

  $mjid = $form['mjid'];
  $seq = $form['seq'];

  $qry = "SELECT * FROM metajob WHERE id='$mjid'";
  $ret = db_query($qry);
  $row = mysql_fetch_assoc($ret);
  if (!$row) die('metajob not found');

  $qry = "SELECT R.*, J.seq
 FROM result R
 LEFT JOIN job J ON R.job_id=J.id
 WHERE R.metajob_id='$mjid'
 AND J.seq='$seq'";

  $ret = db_query($qry);

  $files = array();
  while ($row = mysql_fetch_assoc($ret)) {
    //dd($row);
    $file = $row['LFN'];
    $files[] = $file;
  }
  $a = base64_encode(serialize($files));

  $len = count($files);
  if ($len == 1) { // 파일이 한개인 경우
    $file = $files[0];
    $fn = basename($file);

    header("Content-disposition: attachment; filename=\"$fn\"");
    header("Content-type: application/octetstream");
    header("Cache-Control: no-cache");
    header("Pragma: no-cache");
    header("Expires: -1");

    $path = $conf['htcaas_php_path'];
    $command =<<<EOS
$path/cli/cli.cat.php "$a"
EOS;
    passthru($command);

  } else { // 파일이 여러개인경우

    header("Content-disposition: attachment; filename=\"$mjid-$seq.tar\"");
    header("Content-type: application/octetstream");
    header("Cache-Control: no-cache");
    header("Pragma: no-cache");
    header("Expires: -1");

    $path = $conf['htcaas_php_path'];
    $command =<<<EOS
$path/cli/cli.tar.php "$a"
EOS;
    passthru($command);
  }
  exit;
}

else die('error');


?>
