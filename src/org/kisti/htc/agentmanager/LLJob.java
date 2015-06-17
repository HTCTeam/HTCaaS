package org.kisti.htc.agentmanager;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.kisti.htc.message.DTO;
import org.kisti.htc.message.DirectConsumer;
import org.kisti.htc.message.MessageCommander;
import org.kisti.htc.message.MetaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class LLJob {

  private static final Logger logger = LoggerFactory.getLogger(LLJob.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
  
  private static String AGENT_SCRIPT_FN = "runAgentPLSI.sh";
  static String AGENT_WORKSPACE = "/workspace/";
  
  // 슈퍼컴퓨터
  private LLResource kr;
  
  private String ceName;
  private String limitClass;
  
  // LL 제출 스크립트 (File 객체)
  private File submitCMD;
  
  private Map<Integer, Integer> agentIdMap;
  private int agentId;
  private String userId;
  private String type;
  private MetaDTO mDTO;
  
  // 실행할 에이전트의 개수
  private int num;

  public LLJob(LLResource kr, String ceName, String type, String userId, MetaDTO mDTO, int num) {
    this.kr = kr;
    this.ceName = ceName;
    this.type = type;
    this.userId = userId;
    this.mDTO = mDTO;
    this.num = num;
    init();
  }

  public LLJob(LLResource kr, String ceName, String type, String userId, MetaDTO mDTO) {
    this.kr = kr;
    this.ceName = ceName;
    this.type = type;
    this.userId = userId;
    this.mDTO = mDTO;
    init();
  }

  public LLJob(LLResource kr, String ceName, String type, String userId) {
    this.kr = kr;
    this.ceName = ceName;
    this.type = type;
    this.userId = userId;
    init();
  }

  public LLJob(LLResource kr, String ceName, String type) {
    this.userId = null;
    this.kr = kr;
    this.ceName = ceName;
    this.type = type;
    init();
  }

  public void init() {

    // 주어진 CE 이름으로 CE.limitClass 값을 조회
    limitClass = AgentManager.dbClient.getCELimitClass(ceName);
    if (AgentManager.fileEnabled) {
      AGENT_WORKSPACE = "/workspace_file/";
    }
    if (AgentManager.pTestAgentEnabled) {
      AGENT_SCRIPT_FN = "runPTestAgentPLSI.sh";
      logger.info("=====init" + AGENT_SCRIPT_FN + "=====!!@!@!#@$!!!$@##!!$");
    }
  }

  // 작업 제출
  // KSCJob::submit
  public boolean submit() {
    if (type.equals("LoadL")) {
      if (AgentManager.fileEnabled) {
        return submitUsingLoadLFile();
      } else {
        return submitUsingLoadL();
      }
      // return submitUsingLoadLDemo();
    } else if (type.equals("SGE")) {
      return submitUsingSGE();
    } else {
      logger.error("Unknown Submission Type: " + type);
      return false;
    }
  }

  // LL 제출 스크립트를 생성. cmd 파일에 기록한다.
  // agnetNum 은 에이전트의 개수
  // Load Leveler Command Version
  private void generateLLSubmitCMD(int agentNum) {

    boolean tflag = false;
    boolean dflag = false;

    String url = "http://"+ LLResource.PLSILOGINNODE + ":" + LLResource.PLSIWGETPORT + AgentManager.agentStorageAddress + AGENT_SCRIPT_FN;
    String buffer = "";

    logger.info("===== generate " + AGENT_SCRIPT_FN + " =====!!@!@!#@$!!!$@##!!$");
    try {

      // LL 제출 스크립트 파일 경로
      submitCMD = new File(AgentManager.tempDir, agentIdMap.get(0) + "_" + agentNum + ".cmd");
      PrintStream ps = new PrintStream(submitCMD);

if (dflag) logger.info("-------- 1 -------------");

      String content = ""
      + "#!/bin/bash\n"
      + "# @ job_type = serial\n"
      + "# @ job_name = HTCaaS_$(stepid)_$(step_name)" + "\n"
      + "## @ output = $(job_name).out\n"
      + "## @ error = $(job_name).err\n"
      + "# @ resources = ConsumableCpus(1) ConsumableMemory(100mb)\n";
      
      if (tflag) ps.print("#!/bin/bash\n");
      if (tflag) ps.print("# @ job_type = serial\n");
      if (tflag) ps.print("# @ job_name = HTCaaS_$(stepid)_$(step_name)" + "\n");
      if (tflag) ps.print("## @ output = $(job_name).out\n");
      if (tflag) ps.print("## @ error = $(job_name).err\n");
      if (tflag) ps.print("# @ resources = ConsumableCpus(1) ConsumableMemory(100mb)\n");
      
if (dflag) logger.info("-------- 2 -------------");

      int time = 0;
      if(mDTO != null){
        time = kr.getDBClient().getMetaJobAJobTime(mDTO.getMetaJobId()); //second
      }

      if(time > 0 && time <= 36000){
        time = time + AgentManager.aWallClockTime;  //add the taken time by agent
        
        int minute = time / 60;
        int second = time % 60;
        int hour = 0;
        if(minute > 59){
          hour = minute / 60;
          minute = minute % 60;
        }
        content += "# @ wall_clock_limit = " + hour + ":"+minute+":"+second+"\n";
        if (tflag) ps.print("# @ wall_clock_limit = " + hour + ":"+minute+":"+second+"\n");
if (dflag) logger.info("-------- 3 -------------");

      } else if(time > 36000){
        time += AgentManager.aWallClockTime;  //add the taken time by agent
        
        int minute = time / 60;
        int second = time % 60;
        int hour = 0;
        if(minute > 59){
          hour = minute / 60;
          minute = minute % 60;
        }
        content += "# @ wall_clock_limit = " + hour + ":"+minute+":"+second+"\n";
        if (tflag) ps.print("# @ wall_clock_limit = " + hour + ":"+minute+":"+second+"\n");
if (dflag) logger.info("-------- 4 -------------");
        
      }else{
        content += "# @ wall_clock_limit = " + AgentManager.dWallClockTime + ":00:00"+"\n"; //set default time
        if (tflag) ps.print("# @ wall_clock_limit = " + AgentManager.dWallClockTime + ":00:00"+"\n"); //set default time
      }
      content += "# @ class = " + limitClass + "\n";
      if (tflag) ps.print("# @ class = " + limitClass + "\n");
      content += "# @ cluster_list = " + ceName + "\n";
      if (tflag) ps.print("# @ cluster_list = " + ceName + "\n");
      content += "# @ initialdir = /work/htcaas/\n"; // add
      if (tflag) ps.print("# @ initialdir = /work/htcaas/\n"); // add
      // ps.print("#@ tasks_per_node = 2\n");

if (dflag) logger.info("-------- 5 -------------");

      for (int i = 0; i < agentNum; i++) {

        content += "# @ step_name = Agent_" + agentIdMap.get(i) + "\n";
        if (tflag) ps.print("# @ step_name = Agent_" + agentIdMap.get(i) + "\n");
        content += "# @ queue\n";
        if (tflag) ps.print("# @ queue\n");
      }
      content += "case $LOADL_STEP_NAME in\n";
      if (tflag) ps.print("case $LOADL_STEP_NAME in\n");
if (dflag) logger.info("-------- 6 -------------");

      for (int i = 0; i < agentNum; i++) {
        content += "Agent_" + agentIdMap.get(i) + ")\n";
        if (tflag) ps.print("Agent_" + agentIdMap.get(i) + ")\n");
        content += "hostname\n";
        if (tflag) ps.print("hostname\n");
        content += "mkdir -p " + userId + "/" + agentIdMap.get(i) + "\n";
        if (tflag) ps.print("mkdir -p " + userId + "/" + agentIdMap.get(i) + "\n");
        content += "cd " + userId + "/" + agentIdMap.get(i) + "\n";
        if (tflag) ps.print("cd " + userId + "/" + agentIdMap.get(i) + "\n");
if (dflag) logger.info("-------- 7 -------------");
        
        // 에이전트 실행 스크립트를 가져옴
        buffer = "wget "+ url + " -O " + AGENT_SCRIPT_FN + "\n";
        content += buffer;
        if (tflag) ps.print(buffer);
        
        // 실행 모드로 변경
        content += "chmod +x " + AGENT_SCRIPT_FN + "\n";
        if (tflag) ps.print("chmod +x " + AGENT_SCRIPT_FN + "\n");
        
      // 스크립트를 실행
        content += "./" + AGENT_SCRIPT_FN + " " + agentIdMap.get(i) + " " + userId + "\n";
        if (tflag) ps.print("./" + AGENT_SCRIPT_FN + " " + agentIdMap.get(i) + " " + userId + "\n");
        
        content += "rm -rf ../" + agentIdMap.get(i) + "\n";
        if (tflag) ps.print("rm -rf ../" + agentIdMap.get(i) + "\n");
        content += ";;\n";
        if (tflag) ps.print(";;\n");
      }
if (dflag) logger.info("-------- 8 -------------");

      content += "esac\n";
      if (tflag) ps.print("esac\n");

      logger.info(content);
      if (!tflag) ps.print(content);
if (dflag) logger.info("-------- 9 -------------");

      ps.close();
      
    } catch (Exception e) {
      logger.error("Failed to Generate LoadL Submit CMD: " + e.getMessage());
    }

  }

  // Load Leveler Command Version
  private void generateFileLLSubmitCMD() {

    try {
      submitCMD = new File(AgentManager.tempDir, UUID.randomUUID() + ".cmd"); // .cmd file
      PrintStream ps = new PrintStream(submitCMD);

      String content = ""
      + "#!/bin/bash\n"
      + "#@ job_type = serial\n"
      + "#@ job_name = HTCaaS_Agent_" + agentIdMap.get(0) + "\n"
      + "#@ output = $(job_name).out\n"
      + "#@ error = $(job_name).err\n"
      + "#@ resources = ConsumableCpus(1) ConsumableMemory(100mb)\n"
      + "#@ wall_clock_limit = " + (7 * 24) + ":00:00\n"
      + "#@ class = " + limitClass + "\n"
      + "#@ cluster_list = " + ceName + "\n"
      + "#@ initialdir = "+ AgentManager.PLSI_Remote_Home + userId + AGENT_WORKSPACE + agentIdMap.get(0) + " \n" // add
      + "#@ queue\n"
      + "hostname\n"
      + "chmod +x runFileAgentPLSI.sh\n"
      + "./runFileAgentPLSI.sh " + agentIdMap.get(0);
      logger.info(content);

      ps.print("#!/bin/bash\n");
      ps.print("#@ job_type = serial\n");
      ps.print("#@ job_name = HTCaaS_Agent_" + agentIdMap.get(0) + "\n");
      ps.print("#@ output = $(job_name).out\n");
      ps.print("#@ error = $(job_name).err\n");
      ps.print("#@ resources = ConsumableCpus(1) ConsumableMemory(100mb)\n");
      ps.print("#@ wall_clock_limit = " + (1 * 24) + ":00:00\n");
      ps.print("#@ class = " + limitClass + "\n");
      ps.print("#@ cluster_list = " + ceName + "\n");
      ps.print("#@ initialdir = "+ AgentManager.PLSI_Remote_Home + userId + AGENT_WORKSPACE + agentIdMap.get(0) + " \n"); // add
      ps.print("#@ queue\n");
      ps.print("hostname\n");
      ps.print("chmod +x runFileAgentPLSI.sh\n");
      ps.print("./runFileAgentPLSI.sh " + agentIdMap.get(0));

      ps.close();
    } catch (Exception e) {
      logger.error("Failed to Generate LoadL Submit CMD: " + e.getMessage());
    }

  }

  // SGE Command Version
  private void generateSGESubmitCMD() {
    try {
      submitCMD = new File(AgentManager.tempDir, UUID.randomUUID() + ".cmd");
      PrintStream ps = new PrintStream(submitCMD);

      ps.println("#!/bin/bash");
      ps.println("#$ -V");
      ps.println("#$ -q normal");
      ps.println("#$ -l h_rt=01:00:00");
      ps.println("#$ -wd /scratch/swrho/agent/" + agentIdMap.get(0));
      ps.println();
      ps.println("export AGENT_HOME=/scratch/swrho/agent/" + agentIdMap.get(0));
      ps.println("export ANT_HOME=$AGENT_HOME/apache-ant-1.8.1");
      ps.println("export PATH=$ANT_HOME/bin:$PATH");
      ps.println();
      ps.println("echo $PATH");
      ps.println();
      ps.println("chmod -R +x $ANT_HOME/bin");
      ps.println();
      ps.println("runAgent.sh");

      ps.close();

    } catch (Exception e) {
      logger.error("Failed to Generate Submit CMD: " + e.getMessage());
    }
  }

  // retrieve a sub message from InputQueue
  public static DTO requestSubJob(String userId) {
    logger.info("+ Retrieving a sub message from InputQueue");
    DirectConsumer messageConsumer = new DirectConsumer("subDirectConsumer");
    DTO dto;
    try {
      dto = messageConsumer.getMessage(userId, 1000);
      logger.info("| JobID: " + dto.getJobId() + ", Executable: " + dto.getExecutable() + ", Input: " + dto.getInputFiles() + ", Output: " + dto.getOutputFiles());
      AgentManager.dbClient.decreaseMetaJobNum(dto.getMetaJobId());
      AgentManager.dbClient.checkMetaJobStatusByNum(dto.getMetaJobId());
    } catch (Exception e) {
      logger.error("Failed to retrive a sub job message from InputQueue", e);
      return null;
    }

    return dto;
  }

  public boolean submitUsingLoadLDemo() {

    return false;
  }

  // KSCJob::submitUsingLoadL
  public boolean submitUsingLoadL() {
    agentIdMap = new HashMap<Integer, Integer>();

    try {
      if (this.userId != null) {
        for (int i = 0; i < num; i++) {
          int aid = kr.getDBClient().addAgent(userId);
          if (aid != -1) {
            agentIdMap.put(i, aid);
            logger.info("| " + (i + 1) + " New Agent added, AgentID : " + agentIdMap.get(i));
            kr.getDBClient().setAgentCE(agentIdMap.get(i), ceName);
          } else {
            throw new Exception("addAgent ID is -1");
          }
        }
      } else {
        for (int i = 0; i < num; i++) {
          int aid = kr.getDBClient().addAgent(userId);
          if (aid != -1) {
            agentIdMap.put(i, aid);
            logger.info("| " + (i + 1) + " New Agent added, AgentID : " + agentIdMap.get(i));
            kr.getDBClient().setAgentCE(agentIdMap.get(i), ceName);
          } else {
            throw new Exception("addAgent ID is -1");
          }
        }
      }

      // LL 제출 스크립트를 생성
      generateLLSubmitCMD(num);

      // 작업제출 {{{
      int port = 22;
      SshExecReturn result1 = null;
      SshExecReturn result2 = null;

      // ssh 클라이언트
      SshClient sc = new SshClient();

      // PLSI 로그인 노드, 사용자아이디, 패스워드, 포트
      Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, kr.getDBClient().getUserPasswd(userId), port);
      
      // /htcaas/아이디/workspace
      result1 = sc.Exec("mkdir -p "+ AgentManager.PLSI_Remote_Home + userId + AGENT_WORKSPACE, ss, false);
      if (result1.getExitValue() == 0) {
        sc.ScpTo(AgentManager.tempDir + "/" + submitCMD.getName(), AgentManager.PLSI_Remote_Home + userId + AGENT_WORKSPACE, ss, false);
        
        // llsubmit  /htcaas/아이디/workspace/파일명.cmd
        result2 = sc.Exec("llsubmit "+ AgentManager.PLSI_Remote_Home + userId + AGENT_WORKSPACE + submitCMD.getName(), ss, true);
        
      } else {
        throw new SSHException(result1.getStdError());
      }

      if (!result2.getStdOutput().isEmpty() && result2.getStdError().isEmpty()) {
        String[] out = result2.getStdOutput().split(" ");
        logger.info("| Successfully submitted, submitID: " + out[1]);
        kr.getDBClient().increaseCESubmitCount(ceName, 1);
        kr.getDBClient().setAgentSubmitIdMap(agentIdMap, out[1]);
      } else {
        throw new SubmitException(result2.getStdError());
      }
    // 작업제출 }}}

    } catch (SSHException e1) {
      logger.error("LoadLeveler Submission Error:1. Failed to submit a new agent", e1);

      try{
        submitCMD.delete();
        kr.getDBClient().reportSubmitErrorMap(agentIdMap, mDTO.getMetaJobId(), null, ceName, e1.getMessage());
        kr.getDBClient().setMetaJobError(mDTO.getMetaJobId(), e1.getMessage());
        
        if (e1.getMessage().contains("expired")) {
          kr.getDBClient().setMetaJobStatus(mDTO.getMetaJobId(), AgentManager.JOB_STATUS_CANCEL);
          kr.getDBClient().setJobCancel(mDTO.getMetaJobId());
          
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          
          /* Remove active-mq messagges */
          MessageCommander mc = new MessageCommander();
          int num = mc.removeMessage(mDTO.getMetaJobId(), userId);
          logger.info(num + " jobs is removed");
        }
      }catch(Exception e){
        logger.error("SSH Inner Exception1");
        e.printStackTrace();
      }

      return false;

    } catch (SubmitException e2) {
      logger.error("LoadLeveler Submission Error:2. Failed to submit a new agent", e2);
      
      try{
        submitCMD.delete();
        if(mDTO != null){
          kr.getDBClient().reportSubmitErrorMap(agentIdMap, mDTO.getMetaJobId(), null, ceName, e2.getMessage());
          kr.getDBClient().setMetaJobError(mDTO.getMetaJobId(), e2.getMessage());
        }
        
      } catch(Exception e) {
        logger.error("LL Inner Exception2");
        e.printStackTrace();
      }

      // 메타 작업 상태를 failed 로 변경
      // kr.getDBClient().setMetaJobStatus(mDTO.getMetaJobId(), "failed");

      // 작업 상태를 취소함
      // kr.getDBClient().setJobCancel(mDTO.getMetaJobId());

      // try {
      // Thread.sleep(1000);
      // } catch (InterruptedException e) {
      // // TODO Auto-generated catch block
      // e.printStackTrace();
      // }
      // logger.info("Removing metaJob :" + userId);
      // MessageCommander mc = new MessageCommander();
      // mc.removeMessage(mDTO.getMetaJobId(), userId);

      return false;

    } catch (Exception e3) {
      logger.error("LoadLeveler Submission Error:3. Failed to submit a new agent", e3);

      try{
        submitCMD.delete();
        kr.getDBClient().reportSubmitErrorMap(agentIdMap, mDTO.getMetaJobId(), null, ceName, e3.getMessage());
        kr.getDBClient().setMetaJobError(mDTO.getMetaJobId(), e3.getMessage());
      }catch(Exception e){
        logger.error("Inner Exception3");
        e.printStackTrace();
      }

      // Because of ssh error, this error may occur
      /*
       * if(e3.getMessage().contains("Auth fail")){
       * kr.getDBClient().setMetaJobStatus(mDTO.getMetaJobId(),
       * "canceled"); kr.getDBClient().setJobCancel(mDTO.getMetaJobId());
       * 
       * try { Thread.sleep(1000); } catch (InterruptedException e) { //
       * TODO Auto-generated catch block e.printStackTrace(); }
       * 
       * Remove active-mq messagges MessageCommander mc = new
       * MessageCommander(); int num =
       * mc.removeMessage(mDTO.getMetaJobId(), userId); logger.info(num +
       * " jobs is removed"); }
       */
      return false;
    }
    
    // 스크립트 파일을 삭제
    //submitCMD.delete();

    return true;
  }

  public boolean submitUsingLoadLFile() {
    if (this.userId != null) {
      agentId = kr.getDBClient().addAgent(userId);
    } else {
      agentId = kr.getDBClient().addAgent();
    }
    DTO dto = requestSubJob(userId);

    logger.info("| New Agent added, AgentID : " + agentId);
//    AgentManager.dbClient.setAgentHost(agentId, "???");

    AgentManager.dbClient.setAgentCurrentJob(agentId, dto.getJobId(), dto.getMetaJobId());
    kr.getDBClient().setAgentCE(agentId, ceName);

    // make cmd file to submit
    generateFileLLSubmitCMD();

    int port = 22;
    SshExecReturn result1 = null;
    SshExecReturn result2 = null;

    SshClient sc = new SshClient();

    try {
      Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, kr.getDBClient().getUserPasswd(userId), port);
      sc.Exec("rm -rf /home/" + userId + AGENT_WORKSPACE + agentId, ss, false); // auth
                                            // fail
      result1 = sc.Exec("mkdir -p /home/" + userId + AGENT_WORKSPACE + agentId + "/scagent/workspace", ss, false);
      if (result1.getExitValue() == 0) {
        sc.ScpTo(AgentManager.scriptDir + "/runFileAgentPLSI.sh", "/home/" + userId + AGENT_WORKSPACE + agentId, ss, false);
        sc.ScpTo(AgentManager.tempDir + "/" + submitCMD.getName(), "/home/" + userId + AGENT_WORKSPACE + agentId, ss, false);

        // scagent.zip 을 복사
        sc.ScpTo("/var/www/html/htc_storage_seungwoo/scagent.zip", "/home/" + userId + AGENT_WORKSPACE + agentId, ss, false);

        for (String inputFile : dto.getInputFiles()) {
          sc.ScpTo(new File(inputFile).getAbsolutePath(), "/home/" + userId + AGENT_WORKSPACE + agentId + "/scagent/workspace", ss, false);
        }

        // write jobmsg
        try {
          PrintStream ps = new PrintStream(new File("tmp/jobmsg-" + agentId));

          ps.println("[MetaJobID] " + dto.getMetaJobId());
          ps.println("[JobID] " + dto.getJobId());
          ps.println("[UserID] " + dto.getUserId());
          ps.println("[AppName] " + dto.getAppName());
          ps.println("[Executable] " + dto.getExecutable());

          for (String arg : dto.getArguments()) {
            ps.println("[Arguments]: " + arg);
          }
          for (String input : dto.getInputFiles()) {
            ps.println("[InputFiles]: " + new File(input).getName());
          }
          for (String output : dto.getOutputFiles()) {
            ps.println("[OutputFiles]: " + new File(output).getName());
          }

          ps.close();
        } catch (Exception e) {
          logger.error("Failed to Generate jobmsg: " + e.getMessage());
        }

        logger.info("Copying jobmsg :" + agentId);
        sc.ScpTo("tmp/jobmsg-" + agentId, "/home/" + userId + AGENT_WORKSPACE + agentId + "/scagent/workspace", ss, false);

        logger.info("llsubmint cmd :" + agentId);
        result2 = sc.Exec("llsubmit /home/" + userId + AGENT_WORKSPACE + agentId + "/" + submitCMD.getName(), ss, true);
      } else {
        throw new Exception(result1.getStdError());
      }

      if (!result2.getStdOutput().isEmpty() && result2.getStdError().isEmpty()) {
        String[] out = result2.getStdOutput().split(" ");
        logger.info("| Successfully submitted, submitID: " + out[1]);
        kr.getDBClient().setAgentSubmitId(agentId, out[1]);
      } else {
        throw new Exception(result2.getStdError());
      }

    } catch (Exception e) {
      logger.error("LoadLeveler File Submission Error. Failed to submit a new agent", e);

      kr.getDBClient().reportSubmitError(agentId, mDTO.getMetaJobId(), null, ceName, e.getMessage());
      submitCMD.delete();

      return false;
    }

    submitCMD.delete();

    AgentManager.dbClient.setAgentPushed(agentId);

    AgentManager.checkQueue.addJob(new AgentMonitoringInfo(agentId, dto));

    AgentManager.dbClient.setJobStatus(dto.getJobId(), AgentManager.JOB_STATUS_PRE);

    return true;
  }

  public boolean submitUsingSGE() {

    agentId = kr.getDBClient().addAgent();
    logger.info("| New Agent added, AgentID : " + agentId);

    kr.getDBClient().setAgentCE(agentId, ceName);

    // LL 제출 스크립트를 생성
    generateLLSubmitCMD(num);

    String errorMsg = "";

    SshClient sc = new SshClient();

    try {
      Session ss = sc.getSession("seungwoo", "shtmddn", "tachyon2.ksc.re.kr", 22);
      sc.ScpTo("tmp/runWorker.sh", "/scratch/swrho/agent/" + agentId, ss, false);

      SshExecReturn result = sc.Exec("llsubmit -X " + ceName + " /scratch/swrho/agent/" + agentId + "/" + submitCMD.getName(), ss, true);

    } catch (Exception e) {

    }

    submitCMD.delete();

    return true;
  }

  public static void main(String[] args) throws Exception {

    LLResource kr = new LLResource("PLSI");
    LLJob job = new LLJob(kr, "kisti.glory", "LoadL", "p143ksw");
    job.generateLLSubmitCMD(3);
    // job.submit();
  }
}
