
### {{{

function _build_message {
  # message
  cmd="javac "
  cmd=$cmd" -classpath"
  cmd=$cmd" build"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd":lib/shared/commons-net-3.2.jar"
  cmd=$cmd":lib/shared/jsdldoc.jar"
  cmd=$cmd" -Xmaxerrs 10"
  cmd=$cmd" -d build"
  cmd=$cmd" src/org/kisti/htc/message/*.java"
  cmd1=$cmd
  echo "----------- message -----------"
  echo $cmd1
  $cmd1
}

function _build_dbmanager {
  # dbmanager
  cmd="javac "
  cmd=$cmd" -classpath"
  cmd=$cmd" build"
  cmd=$cmd":lib/shared/log4j-1.2.16.jar"
  cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
  cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
  cmd=$cmd":lib/DBManager/commons-dbcp-1.4.jar"
  cmd=$cmd":lib/shared/cxf-manifest.jar"
  cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
  cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
  cmd=$cmd" -Xmaxerrs 10"
  cmd=$cmd" -d build"
  cmd=$cmd" src/org/kisti/htc/constant/*.java"
 
 #cmd=$cmd" src/org/kisti/htc/dbmanager/beans/*.java"
  cmd=$cmd""\
" src/org/kisti/htc/dbmanager/beans/WMS.java"\
" src/org/kisti/htc/dbmanager/beans/CE.java"\
" src/org/kisti/htc/dbmanager/beans/CE_Limit.java"\
" src/org/kisti/htc/dbmanager/beans/Constant.java"\
" src/org/kisti/htc/dbmanager/beans/Job.java"\
" src/org/kisti/htc/dbmanager/beans/MetaJob.java"\
" src/org/kisti/htc/dbmanager/beans/Result.java"\
" src/org/kisti/htc/dbmanager/beans/ServiceInfra.java"\
" src/org/kisti/htc/dbmanager/beans/User.java"\
" src/org/kisti/htc/dbmanager/beans/AgentInfo.java"\
""

  #cmd=$cmd" src/org/kisti/htc/dbmanager/dao/*.java"
  cmd=$cmd" src/org/kisti/htc/dbmanager/dao/AgentDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/AgentDAO.java"\
" src/org/kisti/htc/dbmanager/dao/ApplicationDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/ApplicationDAO.java"\
" src/org/kisti/htc/dbmanager/dao/CEDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/CEDAO.java"\
" src/org/kisti/htc/dbmanager/dao/DAOBase.java"\
" src/org/kisti/htc/dbmanager/dao/DAOFactory.java"\
" src/org/kisti/htc/dbmanager/dao/DAOUtil.java"\
" src/org/kisti/htc/dbmanager/dao/EnvDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/EnvDAO.java"\
" src/org/kisti/htc/dbmanager/dao/JobDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/JobDAO.java"\
" src/org/kisti/htc/dbmanager/dao/MetaJobDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/MetaJobDAO.java"\
" src/org/kisti/htc/dbmanager/dao/NoticeDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/NoticeDAO.java"\
" src/org/kisti/htc/dbmanager/dao/ResultDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/ResultDAO.java"\
" src/org/kisti/htc/dbmanager/dao/ServerEnvDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/ServerEnvDAO.java"\
" src/org/kisti/htc/dbmanager/dao/ServiceCodeDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/ServiceCodeDAO.java"\
" src/org/kisti/htc/dbmanager/dao/ServiceInfraDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/ServiceInfraDAO.java"\
" src/org/kisti/htc/dbmanager/dao/SubmitErrorDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/SubmitErrorDAO.java"\
" src/org/kisti/htc/dbmanager/dao/UserDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/UserDAO.java"\
" src/org/kisti/htc/dbmanager/dao/WMSCEDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/WMSCEDAO.java"\
" src/org/kisti/htc/dbmanager/dao/WMSDAOImpl.java"\
" src/org/kisti/htc/dbmanager/dao/WMSDAO.java"
#" src/org/kisti/htc/dbmanager/dao/GFSDAOImpl.java"\
#" src/org/kisti/htc/dbmanager/dao/GFSDAO.java"\

  cmd=$cmd" src/org/kisti/htc/dbmanager/server/Database.java"
  cmd=$cmd" src/org/kisti/htc/dbmanager/server/DatabaseImpl.java"
  cmd=$cmd" src/org/kisti/htc/dbmanager/server/Server.java"
  #cmd=$cmd" src/org/kisti/htc/dbmanager/client/*.java"
  cmd2=$cmd
  echo "----------- db -----------"
  echo $cmd2
  $cmd2
}

function _build_jobmanager {
# jobmanager
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/log4j-1.2.16.jar"
cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/shared/cxf-manifest.jar"
cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
cmd=$cmd":lib/shared/commons-net-3.2.jar"
cmd=$cmd":lib/shared/jsdldoc.jar"
cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/jobmanager/client/*.java"
cmd=$cmd" src/org/kisti/htc/jobmanager/server/*.java"
cmd3=$cmd
  echo "----------- jobmanager -----------"
  echo $cmd3
  $cmd3
}

function _build_monitoring {
# monitoring
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/log4j-1.2.16.jar"
cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/shared/cxf-manifest.jar"
cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
cmd=$cmd":lib/shared/commons-net-3.2.jar"
cmd=$cmd":lib/shared/jsdldoc.jar"
cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/monitoring/client/*.java"
cmd=$cmd" src/org/kisti/htc/monitoring/server/*.java"
cmd=$cmd" src/org/kisti/htc/dbmanager/beans/CE.java"
cmd4=$cmd
  echo "----------- monitoring  -----------"
  echo $cmd4
  $cmd4
}


function _build_udmanager {
# udmanager
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/log4j-1.2.16.jar"
cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/shared/cxf-manifest.jar"
cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
cmd=$cmd":lib/shared/commons-net-3.2.jar"
cmd=$cmd":lib/shared/jsdldoc.jar"
cmd=$cmd":lib/UDManager/commons-io-2.0.1.jar"
cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/udmanager/bean/*.java"
cmd=$cmd" src/org/kisti/htc/udmanager/client/*.java"
cmd=$cmd" src/org/kisti/htc/udmanager/server/*.java"
cmd5=$cmd
  echo "----------- ud -----------"
  echo $cmd5
  $cmd5
}

function _build_agentmanager {
# agentmanager
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/log4j-1.2.16.jar"
#cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/shared/cxf-manifest.jar"
cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"
cmd=$cmd":lib/shared/commons-net-3.2.jar"
cmd=$cmd":lib/shared/jsdldoc.jar"
cmd=$cmd":lib/AgentManager/commons-codec-1.10.jar"
cmd=$cmd":lib/AgentManager/aws-java-sdk-1.2.3.jar"
cmd=$cmd":lib/AgentManager/jsch-0.1.44.jar"
cmd=$cmd":lib/AgentManager/udmanager.jar"
cmd=$cmd":lib/AgentManager/openstack4j-1.0.2.jar"
cmd=$cmd":lib/AgentManager/openstack4j_11_25_jdk6_ver4_runable.jar"
cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/agentmanager/AgentManager.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/AgentMonitoringInfo.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/AmazonCloud.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/BackendResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/CheckWorkQueue.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/CloudJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/ClusterResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/ClusterJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/CloudResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/ClusterJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/ClusterResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/CondorJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/CondorResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/DeleteFileAndDirUtil.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/GliteJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/GliteResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/LLJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SubmitException.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/LocalJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/LocalMachine.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/MatchWorkQueue.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/ResourceScheduler.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SGEJob.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SGEResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SSHException.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SshClient.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SshExecReturn.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/SubmitWorkQueue.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/LLResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/WorkQueue.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/OpenstackResource.java"
cmd=$cmd" src/org/kisti/htc/agentmanager/OpenstackJob.java"

cmd6=$cmd
  echo "----------- agentmanager -----------"
  echo $cmd6
  $cmd6
}

function  _build_accountmanager {
# acmanager
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/log4j-1.2.16.jar"
cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/ACManager/commons-lang-2.6.jar"
cmd=$cmd":lib/ACManager/bcprov-jdk16-146.jar"
cmd=$cmd":lib/ACManager/commons-io-2.0.1.jar"
cmd=$cmd":lib/ACManager/httpmime-4.1.1.jar"
cmd=$cmd":lib/ACManager/jackson-core-asl-1.8.1.jar"
cmd=$cmd":lib/ACManager/jackson-mapper-asl-1.8.1.jar"
cmd=$cmd":lib/ACManager/httpclient-4.1.1.jar"
cmd=$cmd":lib/ACManager/httpclient-cache-4.1.1.jar"
cmd=$cmd":lib/ACManager/httpcore-4.1.jar"
cmd=$cmd":lib/ACManager/UIBClient.jar"
cmd=$cmd":lib/ACManager/xstream-1.4.2.jar"
cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
cmd=$cmd":lib/shared/cxf-rt-frontend-simple-3.0.4.jar"
cmd=$cmd":lib/shared/cxf-rt-databinding-aegis-3.0.4.jar"
cmd=$cmd":lib/shared/cxf-rt-transports-http-3.0.4.jar"
cmd=$cmd":lib/shared/cxf-rt-transports-http-jetty-3.0.4.jar"
cmd=$cmd":lib/shared/cxf-rt-wsdl-3.0.4.jar"
cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/ACManager/jackson-core-asl-1.8.1.jar"
cmd=$cmd":lib/ACManager/jackson-mapper-asl-1.8.1.jar"
cmd=$cmd":lib/ACManager/httpcore-4.2.1.jar"
cmd=$cmd":lib/ACManager/commons-io-2.0.1.jar"
cmd=$cmd":lib/ACManager/httpclient-4.2.1.jar"
cmd=$cmd":lib/ACManager/httpmime-4.1.1.jar"

cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/acmanager/client/*.java"
cmd=$cmd" src/org/kisti/htc/acmanager/server/*.java"
cmd7=$cmd
  echo "----------- account manager -----------"
  echo $cmd7
  $cmd7
}

function _build_util {
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/util/StdOut.java"
cmd=$cmd" src/util/StdIn.java"
cmd=$cmd" src/util/DebugMessage.java"
cmd=$cmd" src/util/mLogger.java"
cmd=$cmd" src/util/mLoggerFactory.java"
cmd8=$cmd
  echo "----------- util -----------"
  echo $cmd8
  $cmd8
}

function _build_javadoc {
  echo "----------- javadoc -----------"

  lib=""
  lib=$lib" lib/shared/cxf-manifest.jar"
  lib=$lib":lib/shared/cxf-core-3.0.4.jar"
  lib=$lib":lib/shared/log4j-1.2.16.jar"
  lib=$lib":lib/shared/slf4j-log4j12-1.5.8.jar"
  lib=$lib":lib/shared/slf4j-api-1.5.8.jar"
  lib=$lib":lib/shared/activemq-all-5.10.2.jar"
  lib=$lib":lib/shared/commons-net-3.2.jar"
  lib=$lib":lib/shared/jsdldoc.jar"

  src=""
  src=$src" src/org/kisti/htc/message/*.java"
  src=$src" src/org/kisti/htc/dbmanager/beans/*.java"
  src=$src" src/org/kisti/htc/dbmanager/dao/*.java"
  src=$src" src/org/kisti/htc/dbmanager/server/*.java"
  src=$src" src/org/kisti/htc/dbmanager/client/*.java"
  src=$src" src/org/kisti/htc/jobmanager/client/*.java"
  src=$src" src/org/kisti/htc/jobmanager/server/*.java"
  src=$src" src/org/kisti/htc/monitoring/client/*.java"
  Src=$src" src/org/kisti/htc/monitoring/server/*.java"
  src=$src" src/org/kisti/htc/udmanager/bean/*.java"
  src=$src" src/org/kisti/htc/udmanager/client/*.java"
  src=$src" src/org/kisti/htc/udmanager/server/*.java"
  src=$src" src/org/kisti/htc/agentmanager/*.java"
  src=$src" src/org/kisti/htc/acmanager/client/*.java"
  src=$src" src/org/kisti/htc/acmanager/server/*.java"
  echo $src

  javadoc  -locale ko_KR -encoding UTF-8 -charset UTF-8 -docencoding UTF-8 -classpath $lib -d doc -private -verbose $src
}

function _build_constant {
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/constant/*.java"
cmd9=$cmd
  echo "----------- util -----------"
  echo $cmd9
  $cmd9
}

function  _build_client {
# client
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/cxf-manifest.jar"
cmd=$cmd":lib/shared/cxf-core-3.0.4.jar"
cmd=$cmd":lib/shared/log4j-1.2.16.jar"
#cmd=$cmd":lib/shared/slf4j-log4j12-1.5.8.jar"
#cmd=$cmd":lib/shared/slf4j-api-1.5.8.jar"
cmd=$cmd":lib/shared/commons-net-3.2.jar"
cmd=$cmd":lib/shared/jsdldoc.jar"
cmd=$cmd":lib/cli/acmanager.jar"
cmd=$cmd":lib/cli/jobmanager.jar"
cmd=$cmd":lib/cli/udmanager.jar"
cmd=$cmd":lib/cli/commons-cli-1.2.jar"
cmd=$cmd":lib/cli/monitoring.jar"
cmd=$cmd":lib/shared/wsdl4j-1.6.3.jar"
cmd=$cmd":lib/cli/geronimo-javamail_1.4_spec-1.6.jar"
#cmd=$cmd":lib/cli/neethi-2.0.4.jar"
cmd=$cmd":lib/shared/xmlschema-core-2.2.1.jar"


cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/cli/client/*.java"
cmd10=$cmd
  echo "----------- client -----------"
  echo $cmd10
  $cmd10
}

function  _build_agent {
# agent
cmd="javac "
cmd=$cmd" -classpath"
cmd=$cmd" build"
cmd=$cmd":lib/shared/cxf-manifest.jar"
cmd=$cmd":lib/shared/activemq-all-5.10.2.jar"

cmd=$cmd" -Xmaxerrs 10"
cmd=$cmd" -d build"
cmd=$cmd" src/org/kisti/htc/agent/*.java"
cmd11=$cmd
  echo "----------- agent -----------"
  echo $cmd11
  $cmd11
}

### }}}


mkdir -p build

if [[ -z $1 ]]; then
  echo "Usage:"
  echo " Build Message        : ./build.sh msg "
  echo " Build DB Manager      : ./build.sh db "
  echo " Build Job Manager     : ./build.sh jm "
  echo " Build Monitoring      : ./build.sh mn "
  echo " Build UD Manager      : ./build.sh ud "
  echo " Build Agent Manager   : ./build.sh am "
  echo " Build Account Manager : ./build.sh ac "
  echo " Build util            : ./build.sh util "
  echo " Build constant        : ./build.sh con "
  echo " Build client        : ./build.sh cli "
  echo " Build agent        : ./build.sh ag "
  echo " Build javadoc : ./build.sh doc "
  echo " Build All : ./build.sh all "
  exit
fi


if [[ ! -z $1 && $1 == 'msg' ]]; then
  _build_message
fi

if [[ ! -z $1 && $1 == 'db' ]]; then
  _build_dbmanager
fi

if [[ ! -z $1 && $1 == 'jm' ]]; then
  _build_jobmanager
fi
 
if [[ ! -z $1 && $1 == 'mn' ]]; then
  _build_monitoring
fi

if [[ ! -z $1 && $1 == 'ud' ]]; then
  _build_udmanager
fi

if [[ ! -z $1 && $1 == 'am' ]]; then
  _build_agentmanager
fi

if [[ ! -z $1 && $1 == 'ac' ]]; then
  _build_accountmanager
fi

if [[ ! -z $1 && $1 == 'util' ]]; then
  _build_util
fi

if [[ ! -z $1 && $1 == 'con' ]]; then
  _build_constant
fi

if [[ ! -z $1 && $1 == 'cli' ]]; then
  _build_client
fi

if [[ ! -z $1 && $1 == 'ag' ]]; then
  _build_agent
fi

if [[ ! -z $1 && $1 == 'all' ]]; then
  _build_util
  _build_constant
  _build_message
  _build_dbmanager
  _build_udmanager
  _build_jobmanager
  _build_monitoring
  _build_agentmanager
  _build_accountmanager
  _build_client
  _build_agent

  find build > build/list.txt
fi

if [[ ! -z $1 && $1 == 'doc' ]]; then
  _build_javadoc
fi


