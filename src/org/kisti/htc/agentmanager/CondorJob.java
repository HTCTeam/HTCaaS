package org.kisti.htc.agentmanager;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
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

public class CondorJob {

  private static final Logger logger = LoggerFactory.getLogger(CondorJob.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
  
  private static String AGENT_SCRIPT_FN = "runAgentCondor.sh";
  static String AGENT_WORKSPACE = "/workspace_condor/";
  
  // 슈퍼컴퓨터
  private CondorResource cr;
  
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

  public CondorJob(CondorResource cr, String ceName, String type, String userId, MetaDTO mDTO, int num) {
    this.cr = cr;
    this.ceName = ceName;
    this.type = type;
    this.userId = userId;
    this.mDTO = mDTO;
    this.num = num;
    init();
  }

  public CondorJob(CondorResource cr, String ceName, String type, String userId, MetaDTO mDTO) {
    this.cr = cr;
    this.ceName = ceName;
    this.type = type;
    this.userId = userId;
    this.mDTO = mDTO;
    init();
  }

  public CondorJob(CondorResource cr, String ceName, String type, String userId) {
    this.cr = cr;
    this.ceName = ceName;
    this.type = type;
    this.userId = userId;
    init();
  }

  public CondorJob(CondorResource cr, String ceName, String type) {
    this.userId = null;
    this.cr = cr;
    this.ceName = ceName;
    this.type = type;
    init();
  }

  public void init() {

    if (AgentManager.fileEnabled) {
      AGENT_WORKSPACE = "/workspace_file/";
    }
  }

  // 작업 제출
  // KSCJob::submit
  public boolean submit() {
    if (type.equals("condor")) {
      if (AgentManager.fileEnabled) {
        logger.error("Not implemented");
        return false;
      } else {
        return submitUsingCondor();
      }
      // return submitUsingLoadLDemo();
    } else {
      logger.error("Unknown Submission Type: " + type);
      return false;
    }
  }

  // Condor 제출 스크립트를 생성. cmd 파일에 기록한다.
  // agnetNum 은 에이전트의 개수
  // Condor Command Version
  private void generateCondorSubmitCMD(int agentNum) {

    String url = "http://"+ CondorResource.PLSILOGINNODE + ":" + CondorResource.PLSIWGETPORT + AgentManager.agentStorageAddress + AGENT_SCRIPT_FN;

    logger.info("===== generate " + AGENT_SCRIPT_FN + " =====!!@!@!#@$!!!$@##!!$");
    try {

      String fName = "condor["+agentNum+"]_" + agentIdMap.get(0) + ".cmd";
      // LL 제출 스크립트 파일 경로
      submitCMD = new File(AgentManager.tempDir, fName);
      PrintStream ps = new PrintStream(submitCMD);

      ps.print("# Submit Description file for condor program \n");
      ps.print("Executable = runAgentCondor.sh\n");
      ps.print("Universe = vanilla\n");
      ps.print("Log = "+fName+".log\n");
      
      ps.print("initialdir = /work/htcaas/" + userId + AGENT_WORKSPACE +  "\n");
      ps.print("remote_initialdir = /work/htcaas/\n");
      ps.print("should_transfer_files = YES\n");
      ps.print("when_to_transfer_output = ON_EXIT_OR_EVICT\n");
//      ps.print("transfer_executable = false\n");
//      ps.print("transfer_input_files = run.sh, http://134.75.117.35:9005/htc_storage/runAgentCondor.sh\n");
      ps.print("\n");
      
      for (int i = 0; i < agentNum; i++) {
        ps.print("Output = "+ agentIdMap.get(i) +".out\n");
        ps.print("Error = "+ agentIdMap.get(i) +".err\n");
        ps.print("Arguments = " + agentIdMap.get(i) + " " + userId + "\n");
        ps.print("Queue\n");
        ps.print("\n");
      }

      ps.close();
      
    } catch (Exception e) {
      logger.error("Failed to Generate Condor Submit CMD: " + e.getMessage());
    }

  }

  // CondorJob::submitUsingCondor
  public boolean submitUsingCondor() {
    agentIdMap = new HashMap<Integer, Integer>();

    try {
      if (this.userId != null) {
        for (int i = 0; i < num; i++) {
          int aid = cr.getDBClient().addAgent(userId);
          if (aid != -1) {
            agentIdMap.put(i, aid);
            logger.info("| " + (i + 1) + " New Agent added, AgentID : " + agentIdMap.get(i));
            cr.getDBClient().setAgentCE(agentIdMap.get(i), ceName);
          } else {
            throw new Exception("addAgent ID is -1");
          }
        }
      } else {
        throw new Exception("UserId is null");
        }

      // LL 제출 스크립트를 생성
      generateCondorSubmitCMD(num);

      // 작업제출 {{{
      int port = 22;
      SshExecReturn result1 = null;
      SshExecReturn result2 = null;

      // ssh 클라이언트
      SshClient sc = new SshClient();

      // PLSI Condor 로그인 노드, 사용자아이디, 패스워드, 포트
      Session ss = sc.getSession(ceName, userId, cr.getDBClient().getUserPasswd(userId), port);

      String defaultPath = AgentManager.Condor_Remote_Home + userId + AGENT_WORKSPACE;
      
      result1 = sc.Exec("mkdir -p "+ defaultPath, ss, false);
      if (result1.getExitValue() == 0) {
        sc.ScpTo(AgentManager.tempDir + "/" + submitCMD.getName(), defaultPath, ss, false);
        sc.ScpTo(AgentManager.scriptDir + "/" + AGENT_SCRIPT_FN, defaultPath, ss, false);
        
        // condor_submit  /work/htcaas/아이디/workspace_condor/파일명.cmd
        result2 = sc.Exec("source /opt/condor/condor.sh;cd "+defaultPath+";chmod +x "+AGENT_SCRIPT_FN+";condor_submit " + submitCMD.getName(), ss, true);        
      } else {
        throw new SSHException(result1.getStdError());
      }

      if (!result2.getStdOutput().isEmpty() && result2.getStdError().isEmpty()) {
        String[] out = result2.getStdOutput().split(" ");
//        System.out.println(out[6]);
        String sId = out[6].substring(0, out[6].indexOf("."));
//        System.out.println(sId);
        logger.info("| Successfully submitted, submitID: " + sId);
        cr.getDBClient().increaseCESubmitCount(ceName, 1);
        cr.getDBClient().setAgentSubmitIdMap(agentIdMap, sId);
      } else {
        throw new SubmitException(result2.getStdError());
      }
    // 작업제출 }}}

    } catch (SSHException e1) {
      logger.error("Condor Submission Error:1. Failed to submit a new agent", e1);

      try{
        submitCMD.delete();
        cr.getDBClient().reportSubmitErrorMap(agentIdMap, mDTO.getMetaJobId(), null, ceName, e1.getMessage());
        cr.getDBClient().setMetaJobError(mDTO.getMetaJobId(), e1.getMessage());
        
        if (e1.getMessage().contains("expired")) {
          cr.getDBClient().setMetaJobStatus(mDTO.getMetaJobId(), AgentManager.JOB_STATUS_CANCEL);
          cr.getDBClient().setJobCancel(mDTO.getMetaJobId());
          
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
      logger.error("Condor Submission Error:2. Failed to submit a new agent", e2);
      
      try{
        submitCMD.delete();
        if(mDTO != null){
          cr.getDBClient().reportSubmitErrorMap(agentIdMap, mDTO.getMetaJobId(), null, ceName, e2.getMessage());
          cr.getDBClient().setMetaJobError(mDTO.getMetaJobId(), e2.getMessage());
        }
        
      } catch(Exception e) {
        logger.error("Condor Inner Exception2");
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
      logger.error("Condor Submission Error:3. Failed to submit a new agent", e3);

      try{
        submitCMD.delete();
        cr.getDBClient().reportSubmitErrorMap(agentIdMap, mDTO.getMetaJobId(), null, ceName, e3.getMessage());
        cr.getDBClient().setMetaJobError(mDTO.getMetaJobId(), e3.getMessage());
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
  

  public static void main(String[] args) throws Exception {

    CondorResource cr = new CondorResource("condor");
    CondorJob job = new CondorJob(cr, "glory-mg01", "condor", "p258rsw", null, 496);
//    job.generateCondorSubmitCMD(1);
//     job.submit();
    
  }
}
