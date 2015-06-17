#!/bin/bash
ant -f build_ud.xml generate.interface2
ant -f build_db.xml generate.interface2
ant -f build_mn.xml generate.interface2
ant -f build_acm.xml generate.interface2
ant -f build_jm.xml generate.interface2
ant -f build_con.xml jar2

#ant -f build_htcaasclient.xml deployToLocal

ant -f build_htcaaswebapi.xml generate.interface2

#ant -f build_agent.xml deploy-zip
./agent-deploy.sh
