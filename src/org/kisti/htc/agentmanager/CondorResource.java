package org.kisti.htc.agentmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kisti.htc.dbmanager.server.Database;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Session;


public class CondorResource extends BackendResource {
  
    private Logger logger = Logger.getLogger(this.getClass());
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
  
  //default PLSI setting
  static String PLSILOGINNODE = "134.75.117.35";
  static String PLSIWGETPORT = "9005";
  static String PLSIID = "plsiportal";
  static String PLSIPASSWD = "zltmxl^^456";
  static String PLSIRESOURCECMD = "conmon";
  

  public static File kscDir;
  public static File gangaConfig;
  private String voName;

  private List<String> ceList;

  public boolean needToRepeat = true;
  long timeout = 3000;
  
  private static String DBManagerURL;
  private static String SSLClientPath;
  private static String SSLClientPassword;
  private static String SSLCAPath;
  private static String SSLCAPassword;
  private static boolean SSL = false;
  private static Database dbClient;

  public static boolean dbFlag = false;

  // constructor
  public CondorResource(String voName) {
 
    if (voName.equals("condor")) {
      logger.info("SuperComputer condor initialization");
      Properties prop = new Properties();
        
      try {
        logger.info("reading configuration :" + AgentManager.configPath);
        prop.load(new FileInputStream(AgentManager.configPath));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
        
      PLSILOGINNODE = prop.getProperty("PLSI_LOGINNODE");
      PLSIID = prop.getProperty("PLSI_ID");
      PLSIPASSWD = prop.getProperty("PLSI_PASSWD");
      PLSIRESOURCECMD = prop.getProperty("PLSI_RESOURCE_CMD_CONDOR");
      PLSIWGETPORT = prop.getProperty("PLSI_WGET_PORT");
      
      logger.info("PLSILOGINNODE : " + PLSILOGINNODE);
      logger.info("PLSIRESOURCECMD : " + PLSIRESOURCECMD);
    }
    
    this.type = "ksc";
    this.name = voName;
    this.voName = voName;

    kscDir = new File("conf/AgentManager/ksc");
    if (!kscDir.exists() || !kscDir.isDirectory()) {
      logger.error("kscDir not exist");
      System.exit(1);
    }

    if(!dbFlag){
      try {
        Properties prop = new Properties();
        prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));
        
        DBManagerURL = prop.getProperty("DBManager.Address");
        
        if(prop.getProperty("SSL.Authentication").equals("true")){
          SSL = true;
          DBManagerURL = DBManagerURL.replace("http", "https");
          SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
          SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
          SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
          SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
        }
        
        System.out.println("DBManagerURL: "+DBManagerURL);
        
      } catch (Exception e) {
        System.out.println("Failed to load config file: " + e.getMessage());
        System.exit(1);
      }

    }
  }
  
  public void prepareDBClient(){
    // 2. prepare DBManager client
    logger.info("prepare dbmanager client");
    ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
    factory.setServiceClass(Database.class);
    factory.setAddress(DBManagerURL);
    factory.setDataBinding(new AegisDatabinding());
    dbClient = (Database) factory.create();
    
  }

  public Database getDBClient() {
    if(dbFlag){
      return AgentManager.dbClient;
    }else{
      if(dbClient==null){
        prepareDBClient();
      }
      return this.dbClient;
    }
  }

  // SuperComputer::getCEList
  public int getCEList(int ceMetric) {
    // try {
    // updateCEInfo();
    // } catch (Exception e) {
    // logger.error(e);
    // }

    if (ceMetric == AgentManager.freeCPU || ceMetric == AgentManager.roundrobin) {

      logger.info(" getCEList  voName : " + voName);
      ceList = getDBClient().getCENameList(voName, true, false);

    } else if (ceMetric == AgentManager.intelligent) {
      // ceList = getDBClient().getIntelligentCEList(voName, 100,
      // 0, 5, 1000);
    }

    logger.info("ceList size:" + ceList.size());

    return ceList.size();
  }

  public void initCEList(int ceMetric) {

  }

  public String getHighPriorityCE() {

    int pri = -9999;
    String ceName = null;
    logger.info("ceList size:" + ceList.size());
    for (String ce : ceList) {
      int pri_temp = getDBClient().getCEPriority(ce);
      int fCPU = getDBClient().getCEFreeCPU(ce);
      if (pri <= pri_temp && fCPU>0 ) {
        pri = pri_temp;
        ceName = ce;
        logger.info("ce priority : " + ceName +" "+ pri);
      }
    }

    return ceName;
  }
  
  public String getHighFreeCPUCE() {
    
    int cpu = 0;
    String ceName = null;
    logger.info("ceList size:" + ceList.size());
    for (String ce : ceList) {
      int cpu_temp = getDBClient().getCEFreeCPU(ce);
      if (cpu < cpu_temp ) {
        cpu = cpu_temp;
        ceName = ce;
        logger.info("CE FREECPU : " + ceName +" "+ cpu);
      }
    }

    return ceName;

        
  }
  
  
  public String getNextCEName(int ceMetric){
    
    String currentCE = null;
    
    ceList = getDBClient().getCENameList(voName, true, false);
    
    if(ceList==null || ceList.isEmpty() ){
      return null;
    }else{
      switch (ceMetric){
      case AgentManager.freeCPU :
        currentCE = getHighFreeCPUCE();
        break;
      case AgentManager.priority :
        currentCE = getHighPriorityCE();
        break;
      default :
        currentCE = ceList.get(0);
      }
      
//      if(getDBClient().increaseCESubmitCount(currentCE, 1)){
//        return currentCE;
//      }else{
//        return null;
//      }
    }
    
    return currentCE;
  }

  public String getNextCE(int ceMetric) {

    String currentCE = null;
    if (ceList != null) {

      if (ceList.isEmpty()) {
        if (ceMetric == AgentManager.roundrobin) {
          logger.error("re-cycling ce list.");

          getCEList(ceMetric);
          if (!ceList.isEmpty()) {
            // currentCE = ceList.get(0);
            currentCE = getHighPriorityCE();

          } else {
            currentCE = null;
          }
        } else {
          currentCE = null; // ceList is empty.
        }

      } else {
        // currentCE = ceList.get(0); // ceList is not empty.
        currentCE = getHighPriorityCE();
      }
    } else {
      return null;
    }

    if (currentCE == null) {
      // No MORE CE
      if (needToRepeat) {
        logger.error("Repeat CEList");
        try {
          Thread.sleep(5000);
          // updateCEInfo(); //uncomment this;.
        } catch (Exception e) {
          logger.error(e +"");
        }
        logger.error("No more CE. Waiting to get new CE List!");
        getCEList(ceMetric);
        getDBClient().initCESubmitCount(voName);
        // getNextCE(ceMetric);
        // currentCE = ceList.get(0);
        currentCE = getHighPriorityCE();
        // if (getDBClient().getCEFreeCPU(currentCE)>0 &&
        // getDBClient().increaseCESubmitCount(currentCE, 1)) {

        if (getDBClient().increaseCESubmitCount(currentCE, 1)) {
          if (ceMetric != AgentManager.freeCPU)
            ceList.remove(currentCE);
          return currentCE;
        } else {
          return null;
        }
      } else {
        return null;
      }
    }

    boolean success = getDBClient().increaseCESubmitCount(currentCE, 1);

    if (success) {
      if (ceMetric == AgentManager.roundrobin || ceMetric == AgentManager.intelligent) {
        ceList.remove(currentCE);
      }
      return currentCE;

    } else {
      // CE is full
      // Remove this one and move to the next one
      ceList.remove(currentCE);
      if (ceList.isEmpty()) {
        return null;
      } else {
        currentCE = getHighPriorityCE();
        if (getDBClient().increaseCESubmitCount(currentCE, 1)) {
          return currentCE;
        } else {
          ceList.remove(currentCE);
          return getNextCE(AgentManager.CE_SELECTION_METRIC);
        }
      }
    }
  }

  public static boolean ready(Reader in, long timeout) throws IOException {

    while (true) {
      long now = System.currentTimeMillis();

      try {
        while (in.ready() == false && timeout > 0) {
          Thread.sleep(100);
          timeout -= 100;
        }
        return in.ready();

      } catch (IOException e) {
        throw e;

      } catch (Exception e) {
        // ignore

      } finally {
        // adjust timer by length of last nap
        timeout -= System.currentTimeMillis() - now;

      }

    }
  }

  public void updateCEInfo() throws Exception {

    logger.info("+ Updating SuperComputer Resource Info using 'conmon'");
    
    List<String> plsiList = new ArrayList<String>();
    
    SshExecReturn result1 = null;
    
    SshClient sc = new SshClient();

    try {
      Session ss = sc.getSession(PLSILOGINNODE, PLSIID, PLSIPASSWD, 22);

      
      result1 = sc.Exec(PLSIRESOURCECMD, ss, true);
      
      if (result1.getExitValue() == 0) {
    
        if (!result1.getStdOutput().isEmpty() && result1.getStdError().isEmpty()) {
            String out = result1.getStdOutput();
            
            logger.debug(out);
            
            String[] out2 = out.split("\n");
            Pattern pattern = Pattern.compile("\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)");
            Matcher matcher;
            for(String line : out2){
              matcher = pattern.matcher(line);
              if (matcher.find()) {
                String cluster = matcher.group(1);
                String nodes = matcher.group(2);
                String availableCPU = matcher.group(3).split("/")[0];

                String totalCPU = matcher.group(4);
                String waitingJobs = matcher.group(5).split("/")[0];
                String pendingJobs = matcher.group(6).split("/")[0];
                String runningJobs = matcher.group(7).split("/")[0];

                logger.info("| cluster:" + cluster + " nodes:" + nodes + " availableCPU:" + availableCPU + " totalCPU:" + totalCPU);
                if (cluster.equals("Total")) {
                  continue;
                }
                // cluster, total cpu, available cpu, running jobs, waiting jobs
                plsiList.add(cluster + " " + totalCPU + " " + availableCPU + " " + runningJobs + " " + waitingJobs);
              }
            }
            
        } else {
          throw new SubmitException(result1.getStdError());
        }

      } else {
        throw new SSHException(result1.getStdError());
      }

    } catch (SSHException e1) {
      logger.error("Condor Monitoring Error:1. Failed to update ceinfo", e1);

      try{
        
      }catch(Exception e){
        logger.error("SSH Inner Exception1");
        e.printStackTrace();
      }

    } catch (Exception e2) {
      logger.error("Cluster Monitoring Error:2. Failed to update ceinfo", e2);

      try{

      }catch(Exception e){
        logger.error("Inner Exception2");
        e.printStackTrace();
      }
    }

    // update SCCE info
    getDBClient().updateSCCEInfo("condor", plsiList);

    logger.info("| ServiceInfra: " + voName + " " + plsiList.size() + " SCCEs updated to Database");
  }

  public String getVoName() {
    return voName;
  }

  public void setVoName(String voName) {
    this.voName = voName;
  }
  
public void cancelZombieJob(){
  
    logger.debug("===Condor cancelZombieJob===");
    
    int port = 22;
    SshExecReturn result2 = null;
    SshClient sc = new SshClient();
    int siId = getDBClient().getServiceInfraId(AgentManager.CONDOR);
    List<Integer> list = getDBClient().getAgentSubmittedZombieList(siId);
    for (Integer aid : list) {
      logger.error("Submitted Zombie Agent ID :  " + aid);

      String userId = getDBClient().getAgentUserId(aid);
      String ce = getDBClient().getAgentCEName(aid);
      String submitId = getDBClient().getAgentSubmitId(aid);

      try {

        Session ss = sc.getSession(ce, userId, getDBClient().getUserPasswd(userId), port);
        // llcancel 명령 실행
        String cmd = "source /opt/condor/condor.sh;condor_rm " + submitId;
        logger.info(cmd);

        result2 = sc.Exec(cmd, ss, true);

        if (!result2.getStdOutput().isEmpty() && result2.getStdError().isEmpty()) {
          logger.info("| Successfully canceled, submitID: " + submitId);
          // logger.info(result2.getStdOutput());
          getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);

        } else {
          throw new Exception(result2.getStdError());
        }
      } catch (Exception e) {
        logger.error("| Cancel is failed, submitId " + submitId + " : " + e);
        if(e.toString().contains("not found")){
          getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
        }
        continue;
        
      }
    }

  }

  public static void main(String[] args) throws Exception {

    CondorResource sc = new CondorResource("condor");
    
//    BackendResource br = sc;
//    br.cancelZombieJob();
    sc.updateCEInfo();
    // System.out.println(sc.getCEList(AgentManager.CE_SELECTION_METRIC));
    // System.out.println(5/(float)59*100);
    // sc.updateCEInfo();
    // System.out.println(sc.getCEList(AgentManager.freeCPU))
//    sc.getCEList(AgentManager.freeCPU);
//    System.out.println(sc.getNextCE(AgentManager.freeCPU));
//    System.out.println(sc.getNextCE(AgentManager.freeCPU));
//    System.out.println(sc.getNextCE(AgentManager.freeCPU));
//    System.out.println(sc.getNextCE(AgentManager.freeCPU));
//    System.out.println(sc.getNextCE(AgentManager.freeCPU));
    // System.out.println(sc.getNextCE(AgentManager.freeCPU));
    // System.out.println(sc.getNextCE(AgentManager.freeCPU));
  }

}
