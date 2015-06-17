#!/bin/sh

date=`date +'%Y%m%d'`
export VERSION=$date

cd  ..
cp -R trunk  HTCaaS


cd HTCaaS

rm -f err
rm -rf build
rm -rf  `find . -name .svn | xargs`
rm -rf log/*
rm -rf certs
rm -rf service/*.pid
rm -rf .settings
rm -rf tmp/*
rm -f conf/*.conf
rm -f conf/*.conf.bak
rm -f dbmon-php/config.php
rm -f activeMQ/data/activemq-*.pid  

cd ..
tar zcvf HTCaaS-$VERSION.tgz  HTCaaS
rm -rf HTCaaS


