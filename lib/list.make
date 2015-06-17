#!/bin/sh

#
# usage:
# list.make > list.txt
#

jars=$(find . -name "*.jar")
#echo $jars

for jar in $jars
do
  echo ============== $jar
  jar tvf $jar
done


