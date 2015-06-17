#!/usr/local/bin/php
<?php

  $a = $argv[1];
  $files = unserialize(base64_decode($a));
  //print_r($files);

  $arg = join(" ", $files);

  // ${USER} 가 들어있는 경우 \${USER} 로 치환
  $arg = preg_replace("/\\$/", "\\\\\$", $arg);

  $command =<<<EOS
/bin/tar c -C / -- $arg
EOS;
  //print $command;
  passthru($command);

?>
