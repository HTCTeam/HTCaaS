
#
# htcaas_web.jar 생성 스크립트
#

### {{{

targets=(
  " org/kisti/htc/monitoring/server/Monitoring"
  " org/kisti/htc/dbmanager/beans/CE"
  " org/kisti/htc/dbmanager/beans/CE_Limit"
  " org/kisti/htc/dbmanager/beans/Constant"
  " org/kisti/htc/dbmanager/beans/Job"
  " org/kisti/htc/dbmanager/beans/MetaJob"
  " org/kisti/htc/dbmanager/beans/Result"
  " org/kisti/htc/dbmanager/beans/ServiceInfra"
  " org/kisti/htc/dbmanager/beans/User"
  " org/kisti/htc/dbmanager/beans/WMS"
  " org/kisti/htc/jobmanager/server/JobManager"
  " org/kisti/htc/udmanager/bean/DataHandlerFile"
  " org/kisti/htc/udmanager/server/UserDataManager"
  " org/kisti/htc/acmanager/client/ACManagerClient"
  " org/kisti/htc/acmanager/client/ACManagerClientImpl"
  " org/kisti/htc/acmanager/client/ACManagerClientImpl\$Password"
  " org/kisti/htc/acmanager/client/Client"
  " org/kisti/htc/acmanager/server/ACManager"
  " org/kisti/htc/constant/AgentConstant"
  " org/kisti/htc/constant/JobConstant"
  " org/kisti/htc/constant/MetaJobConstant"
  " org/kisti/htc/constant/ResourceConstant"
)

function _build {

  # copy java files src --> build
  cd src
  for t in ${targets[@]}
  do
    javafile=$t".java"
    echo cp $javafile ../build/$javafile
    cp $javafile ../build/$javafile
  done 
  cd ..

 
  # make file list
  cd build
  files=""
  for t in ${targets[@]}
  do
     files=$files" "$t".class"
     files=$files" "$t".java"
  done 
  allfiles=$files
  echo $allfiles

  # make jar file
  targetjar="htcaas_web.jar"

  cd build
  cmd="jar cvf ../$targetjar  $allfiles"
  #echo $cmd
  $cmd
  cd ..

  ls -la $targetjar
}

### }}}


if [[ -z $1 ]]; then
  echo " Build htcaas_web.jar for CLI client"
  echo "   run : $0  make"
  exit
fi

if [[ ! -z $1 && $1 == 'make' ]]; then
  mkdir -p build
  _build
fi


