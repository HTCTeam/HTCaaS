/***********************************************************************
 * Copyright (C) 2012-2013, HTCaaS Team, KISTI Supercomputing Center,
 * Korea Institute of Science and Technology Information, Korea.
 * http://htcaas.kisti.re.kr/
 ***********************************************************************/

package org.kisti.htc.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.constant.AgentConstant;
import org.kisti.htc.constant.JobConstant;
import org.kisti.htc.constant.MetaJobConstant;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.message.DTO;
import org.kisti.htc.message.DirectConsumer;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Agent {

  private static final Logger logger = LoggerFactory.getLogger(Agent.class);

  private static final String AGENT_STATUS_RUN = AgentConstant.AGENT_STATUS_RUN;

  private static final String JOB_STATUS_RUN = JobConstant.JOB_STATUS_RUN;
  private static final String JOB_STATUS_DONE = JobConstant.JOB_STATUS_DONE;
  private static final String JOB_STATUS_CANCEL = JobConstant.JOB_STATUS_CANCEL;
  private static final String JOB_STATUS_FAIL = JobConstant.JOB_STATUS_FAIL;

  private static final String METAJOB_STATUS_SPLIT = MetaJobConstant.METAJOB_STATUS_SPLIT;
  
  private static String HTCaaS_AGENT_LOG_PATH = "/home/htcaas/agent/";
  private static String HTCaaS_JOB_LOG_PATH = "/home/htcaas/job/";
  private static int OUTPUT_RETRY_NUM = 5;
  private static int OUTPUT_RETRY_DELAY_TIME = 10000;

  private static String SSLClientPath;
  private static String SSLClientPassword;
  private static String SSLCAPath;
  private static String SSLCAPassword;
  private static boolean SSL = false;
  private static boolean jobLogFlag = false;
  
 //---------------------------------------------------------------
 // 환경설정값                       : 타입    : 기본값
 // Agent.Listening.Priority         : boolean : false
 // true 일 경우
 //   Agent.Listening.Metric1        : int     : 10000
 //   Agent.ListeningMultiple.Metric : int     : 2
 //   Agent.ListeningTime.Limit      : int     : 20000
 // flase 일 경우
 //   Agent.Listening.Metric2        : int     : 500
 //   Agent.WaitingTime              : int     : 1000
 //   Agent.WaitingAddition.Metric   : int     : 1000
 //   Agent.WaitingTime.Limit        : int     : 15000
 // ---------------------------------------------------------------

  private static boolean Listening_Priority = false;
  private static int Listening_Metric1 = 10000;
  private static int ListeningMultiple_Metric = 2;
  private static int ListeningTime_Limit = 20000;
  private static int Listening_Metric2 = 500;
  private static int WaitingTime = 1000;
  private static int WaitingAddition_Metric = 1000;
  private static int WaitingTime_Limit = 15000;
  private static int mFailure = 0;

  private static int failure = 0;
  private static int initialListeningValue = 0;
  private static int initialWaitingValue = 0;
  private static String STORAGE=null;
  
  private Database dbclient; // DBManager client

  private UDClient udc; // UserDataManager client

  private DirectConsumer messageConsumer; // ActiveMQ client

  private static int agentId;
  
  // host : 에이전트가 실행중인 호스트명
  private static String host;
  
  private static String userId;
  private static String passwd;
  private static volatile boolean endSignal = false;

  // 작업 아이디와 메타작업 아이디
  private int metaJobId;
  private int jobId;
  
  private DTO jobMsg;
  private File workDir;
  private File jobLogFile;

  private String FTPAddress;
  private String DBManagerURL;
  private String adminUser = "htcaas";
  private String adminPasswd = "htcaas";
  private static int signalPeriod = 1;

  /**
   * Instantiates a new agent.
   */
  // Agent::Agent
  private Agent() {

    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
//       prop.load(new FileInputStream("conf/HTCaaS_Agent.conf"));
      signalPeriod = Integer.parseInt(prop.getProperty("Agent.Heartbeat.Period"));

      if (prop.getProperty("SSL.Authentication").equals("true")) {
        SSL = true;
        DBManagerURL = DBManagerURL.replace("http", "https");
        // MonitoringURL = MonitoringURL.replace("http", "https");
        SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
        SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
        SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
        SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
      }

      FTPAddress = prop.getProperty("FTP.Address");
      logger.info("FTP.Address : " + FTPAddress);

      DBManagerURL = prop.getProperty("DBManager.Address");
      logger.info("DBManagerURL : " + DBManagerURL);
      
      STORAGE = prop.getProperty("HTCaaS_Storage");
      logger.info("HTCaaS Storage : " + STORAGE);

      Listening_Priority = Boolean.parseBoolean(prop.getProperty("Agent.Listening.Priority"));
      logger.info("Agent Listening Priority : " + Listening_Priority);
      
      HTCaaS_AGENT_LOG_PATH = prop.getProperty("HTCaaS_AGENT_LOG_PATH");
      logger.info("HTCaaS_AGENT_LOG_PATH : " + HTCaaS_AGENT_LOG_PATH);
      
      HTCaaS_JOB_LOG_PATH = prop.getProperty("HTCaaS_JOB_LOG_PATH");
      logger.info("HTCaaS_JOB_LOG_PATH : " + HTCaaS_JOB_LOG_PATH);
      
      OUTPUT_RETRY_NUM = Integer.parseInt(prop.getProperty("Output_Retry_Num"));
      logger.info("OUTPUT_RETRY_NUM : " + OUTPUT_RETRY_NUM);
      
      OUTPUT_RETRY_DELAY_TIME = Integer.parseInt(prop.getProperty("Output_Retry_Delay_Time"));
      logger.info("OUTPUT_RETRY_DELAY_TIME : " + OUTPUT_RETRY_DELAY_TIME);
      

      if (Listening_Priority) {
        Listening_Metric1 = Integer.parseInt(prop.getProperty("Agent.Listening.Metric1"));
        logger.info("Agent Listening Metric : " + Listening_Metric1);

        ListeningMultiple_Metric = Integer.parseInt(prop.getProperty("Agent.ListeningMultiple.Metric"));
        logger.info("Agent ListeningMultiple Metric : " + ListeningMultiple_Metric);

        ListeningTime_Limit = Integer.parseInt(prop.getProperty("Agent.ListeningTime.Limit"));
        logger.info("Agent ListeningTime Limit : " + ListeningTime_Limit);
      } else {
        Listening_Metric2 = Integer.parseInt(prop.getProperty("Agent.Listening.Metric2"));
        logger.info("Agent Listening Metric2 : " + Listening_Metric2);

        WaitingTime = Integer.parseInt(prop.getProperty("Agent.WaitingTime"));
        logger.info("Agent WaitingTime : " + WaitingTime);

        WaitingAddition_Metric = Integer.parseInt(prop.getProperty("Agent.WaitingAddition.Metric"));
        logger.info("Agent WaitingAddition_Metric : " + WaitingAddition_Metric);

        WaitingTime_Limit = Integer.parseInt(prop.getProperty("Agent.WaitingTime.Limit"));
        logger.info("Agent WaitingTime Limit : " + WaitingTime_Limit);
      }

    } catch (Exception e) {
    	e.printStackTrace();
      logger.error("Failed to load config file: " + e.getMessage());
    }

    // 에이전트가 실행중인 호스트명을 구함
    try {
      host = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      host = "UnknownHost";
    }
    
    createDirectories();

    // DBManager client
    ClientProxyFactoryBean dbFactory = new ClientProxyFactoryBean();
    dbFactory.setServiceClass(Database.class);
    dbFactory.setAddress(DBManagerURL);
    dbFactory.setDataBinding(new AegisDatabinding());
    dbclient = (Database) dbFactory.create();

    HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(dbclient).getConduit();
    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
    httpClientPolicy.setConnectionTimeout(180000);
    httpClientPolicy.setReceiveTimeout(0);
    httpConduit.setClient(httpClientPolicy);

    // UDManager client (CXF)
    udc = new UDClient();

    // ActiveMQ client
    messageConsumer = new DirectConsumer(host);

    if (SSL) {
      try {
        setupTLS(dbclient);
        // setupTLS(monitoring);
        setupTLS(udc);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (GeneralSecurityException e) {
        e.printStackTrace();
      }
    }

    ////add add
    
//    logger.info("setAgentHost");
//    if (!dbclient.setAgentHost(agentId, host)) {
//    }
//
//    logger.info("startAgent");
//    if (!dbclient.startAgent(agentId)) {
//    }

    logger.info("| AgentID : " + agentId);
    logger.info("| Working Directory : " + workDir.getAbsolutePath());

  }

/// SSL {{{
  // Agent::setupTLS
  private static void setupTLS(Object port) throws FileNotFoundException, IOException, GeneralSecurityException {

    HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

    TLSClientParameters tlsCP = new TLSClientParameters();
    KeyStore keyStore = KeyStore.getInstance("JKS");
    String keyStoreLoc = SSLClientPath;
    keyStore.load(new FileInputStream(keyStoreLoc), SSLClientPassword.toCharArray());
    KeyManager[] myKeyManagers = getKeyManagers(keyStore, SSLClientPassword);
    tlsCP.setKeyManagers(myKeyManagers);

    KeyStore trustStore = KeyStore.getInstance("JKS");
    String trustStoreLoc = SSLCAPath;
    trustStore.load(new FileInputStream(trustStoreLoc), SSLCAPassword.toCharArray());
    TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
    tlsCP.setTrustManagers(myTrustStoreKeyManagers);

    // The following is not recommended and would not be done in a
    // prodcution environment,
    // this is just for illustrative purpose
    tlsCP.setDisableCNCheck(true);
    tlsCP.setSecureSocketProtocol("SSL"); // addme

    httpConduit.setTlsClientParameters(tlsCP);

  }

  // Agent::getTrustManagers
  private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
    fac.init(trustStore);
    return fac.getTrustManagers();
  }

  // Agent::getKeyManagers
  private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
    KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
    fac.init(keyStore, keyPass);
    return fac.getKeyManagers();
  }
/// SSL }}}
  
  // Creates the directories.
  // workspace 디렉터리를 생성
  // Agent::createDirectories
  private void createDirectories() {
    workDir = new File("workspace");
    if (workDir.exists()) {
      DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
    }
    // make a new directory "workspace"
    workDir.mkdirs();

    // make a directory workspace/local/output
    // outputDir = new File("local/output");
    // outputDir.mkdirs();
  }

  // retrieve a message from InputQueue
  /**
   * Request job.
   * 
   * @return true, if successful
   */
  // Agent::requestJob
  public boolean requestJob(String user, int waitingTime) throws Exception {
    logger.info("+ Retrieving a job message from InputQueue");
    boolean result = false;

    try {
      // 사용자별로 큐가 따로 만들어져 있으므로 사용자 아이디를 전달
      jobMsg = messageConsumer.getMessage(user, waitingTime);
      logger.info("| JobMsg JobID : " + jobMsg.getJobId() + ", MetaJobID : " + jobMsg.getMetaJobId() + ", AppName: " + jobMsg.getAppName() + ", userId: " + jobMsg.getUserId());

      logger.info("+ Agent started at " + host);
      
      jobId = jobMsg.getJobId();
      metaJobId = jobMsg.getMetaJobId();
      userId = jobMsg.getUserId();

      try {

        // if(!dbclient.decreaseMetaJobNum(metaJobId)){
        // throw new Exception("Failed to setJobStatus : preparing");
        // }

        // 해당작업이 이미 done 이나 run 상태이면
        logger.info("getJobStatus");
        String status = dbclient.getJobStatus(jobId);
        if (status.equals(JOB_STATUS_DONE)) {
          logger.warn("Job is Done. Skip the job");
          return result;
        } else if (status.equals(JOB_STATUS_RUN)) {
          logger.warn("Job is running. Skip the job");
          return result;
        }

        result = true;
      } catch (Exception e) {
        logger.error("Failed to retrive a job message from InputQueue", e);
        e.printStackTrace();
        throw new Exception("Failed to request job");
      }

    } catch (Exception e) {
    }

    return result;
  }

  /**
   * Retrieve input files.
   * 
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  // Agent::getInputFiles
  public boolean getInputFiles() {
    if(STORAGE.equals("FTP")){
      return getInputFiles_FTP();
    }else if(STORAGE.equals("Web")){
      return getInputFiles_Web();
    }else{
      return false;
    }
  }
  
  // Agent::getInputFiles_FTP
  public boolean getInputFiles_FTP() {
    logger.info("+ Retrieving input files FTP");
    boolean result = false;
    UUID uid = null;
    try {
      uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
      int i = 1;
      while (uid == null && i < 6) {
        logger.error("UDClient login failed. retry : " + i);
        uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
        i++;
      }

      if (uid == null) {
        new Exception("UDClient login failed");
      }

      // udc.client 는 UserDataManager(interface) type
      // interface org.kisti.htc.udmanager.server.UserDataManager
      // public UUID login(String address, String id, String passwd);
      // public class UserDataManagerImpl implements UserDataManager

      for (String inputFile : jobMsg.getInputFiles()) {
        File file = new File(inputFile);
        boolean checkResult = false;
        long serverChecksum = udc.getServerChecksum(file.getName(), file.getParent());

        checkResult = udc.getFile(uid, file.getName(), file.getParent(), workDir.getAbsolutePath(), agentId);

        logger.info("| " + inputFile + "(" + checkResult + ")");

        long localChecksum = udc.getLocalChecksum(file.getName(), workDir.getAbsolutePath());

        int y = 1;
        while (!checkResult || (localChecksum != serverChecksum)) {
          logger.info("Checksum false : " + file.getName());

          if (y == 4) {
            logger.error("| " + inputFile + "(failed)");

            uploadAgentLog();
            return result;
          }
          udc.udclient.logout(uid, agentId);
          uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
          logger.info("| " + inputFile + "(" + y + " retransmitted)");
          checkResult = udc.getFile(uid, file.getName(), file.getParent(), workDir.getAbsolutePath(), agentId);
          localChecksum = udc.getLocalChecksum(file.getName(), workDir.getAbsolutePath());
          y++;
        }

        logger.info("Checksum true : " + file.getName());
      }
      result = true;

    } catch (SocketTimeoutException e1) {
      logger.error("SocketTiemoutException");
      e1.printStackTrace();
      result = false;
    } catch (Exception e2) {
      logger.error("Unknown Exception");
      e2.printStackTrace();
      result = false;
    } finally {
      if (uid != null) {
        udc.udclient.logout(uid, agentId);
      }
    }
    return result;
  }

  // Agent::getInputFiles_Web
  public boolean getInputFiles_Web() {

    logger.info("+ Retrieving input files Web");
    boolean result = false;

      // udc.client 는 UserDataManager(interface) type
      // interface org.kisti.htc.udmanager.server.UserDataManager
      // public UUID login(String address, String id, String passwd);
      // public class UserDataManagerImpl implements UserDataManager
    try{
      for (String inputFile : jobMsg.getInputFiles()) {
        File file = new File(inputFile);
        boolean checkResult = false;
        long serverChecksum = udc.getServerChecksum(file.getName(), file.getParent());

        checkResult = udc.getFile_Web(file.getName(), file.getParent(), workDir.getAbsolutePath(), agentId);

        logger.info("| " + inputFile + "(" + checkResult + ")");

        long localChecksum = udc.getLocalChecksum(file.getName(), workDir.getAbsolutePath());

        int y = 1;
        while (!checkResult || (localChecksum != serverChecksum)) {
          logger.info("Checksum false : " + file.getName());

          if (y == 4) {
            logger.error("| " + inputFile + "(failed)");

            uploadAgentLog();
            return result;
          }
          logger.info("| " + inputFile + "(" + y + " retransmitted)");
          checkResult = udc.getFile_Web(file.getName(), file.getParent(), workDir.getAbsolutePath(), agentId);
          localChecksum = udc.getLocalChecksum(file.getName(), workDir.getAbsolutePath());
          y++;
        }

        logger.info("Checksum true : " + file.getName());
      }
      result = true;

    } catch (Exception e2) {
      logger.error("Unknown Exception");
      e2.printStackTrace();
      result = false;
    } 
    return result;
  }
  
  private boolean changeFileExecutableMod(String filePath) {

		logger.info("ChangeFilExecutableMod : " + filePath);

		try {
			List<String> command = new ArrayList<String>();

			command.add("chmod");
			command.add("+x");
			command.add(filePath);

			logger.info("" + command);

			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(new File("."));

			Process p = builder.start();
			int exitValue = p.waitFor();

			if (exitValue == 0) {
				logger.info("| chmod success");

			} else {
				StringBuffer sb = new StringBuffer();

				BufferedReader brE = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (brE.readLine() == null) {
					BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = brI.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
					brI.close();
				} else {
					String line;
					while ((line = brE.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
				}

				brE.close();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");
				logger.error("| " + sb.toString());

				throw new Exception("chmod Error");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());

			return false;
		}
		return true;
	}
  
  /**
   * Install application.
   * 
   * @throws Exception
   *           the exception
   */
  // Agent::installApplication
  public void installApplication() throws Exception {
    logger.info("+ Installing application");

    String file = workDir.getAbsolutePath() + File.separator + "install.sh";
    if (new File(file).exists()) {
      List<String> command = new ArrayList<String>();
      command.add("/bin/sh");
      command.add(workDir.getAbsolutePath() + File.separator + "install.sh");

      ProcessBuilder builder = new ProcessBuilder(command);
      builder.directory(workDir);

      Process process = builder.start();
      logger.info("| [OutputStream]");
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = br.readLine()) != null) {
        logger.info("| " + line);
      }
      br.close();

      logger.info("| [ErrorStream]");
      br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      while ((line = br.readLine()) != null) {
        logger.info("| " + line);
      }
    }
  }

  
  /**
   * Execute the job
   * 
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  // Agent::execute
  public boolean execute() throws Exception {
    logger.info("+ Executing application");
    boolean result = false;

    List<String> command = new ArrayList<String>();
    
    if(jobMsg.getInputFiles() != null && !jobMsg.getInputFiles().isEmpty()){
    	for(String input : jobMsg.getInputFiles()){
			if(input.contains(jobMsg.getExecutable())){
				changeFileExecutableMod(workDir.getAbsolutePath() + File.separator + jobMsg.getExecutable());
				if(jobMsg.getExecutable().endsWith("sh")){
			    	command.add("/bin/sh");
			    	command.add(workDir.getAbsolutePath() + File.separator + jobMsg.getExecutable());
				}else{
					command.add(workDir.getAbsolutePath() + File.separator + jobMsg.getExecutable());
				}
				break;
			}
		}
    	
    }
    
    if(command.isEmpty()){
    	command.add("" + jobMsg.getExecutable());
    }
    
    
    if (jobMsg.getAppName().equals("pTest")) {
      command.add(jobMsg.getUserId());
      command.add(String.valueOf(jobMsg.getJobId()));
    } else {
      for (String arg : jobMsg.getArguments()) {
        command.add(arg);
      }
    }

    logger.info("Command :" + command.toString());
    
    ProcessBuilder builder = new ProcessBuilder(command);

    builder.directory(workDir);

    Process process = builder.start();
    logger.info("| [InputStream]");
    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    String line;
    while ((line = br.readLine()) != null) {
      logger.info("| " + line);
    }
    br.close();

    logger.info("| [ErrorStream]");
    br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    if (jobMsg.getAppName().equals("autodock3")) {
      while ((line = br.readLine()) != null) {

        logger.info("| " + line);
        if (line.contains("error") || line.contains("sorry"))
          result = false;
        else if (line.contains("Successful Completion")) {
          result = true;
        }
      }

      result = true;
    } else {
      while ((line = br.readLine()) != null) {
        logger.info("| " + line);
      }
      result = true;
    } // end if-else

    return result;

  }

  // Validate output files.
  // return true, if successful
  // @throws Exception
  // Agent::validateOutputFiles
  public boolean validateOutputFiles() throws Exception {
    logger.info("+ Validating output files");

    boolean validated = false;

    for (String outputFile : jobMsg.getOutputFiles()) {
      logger.debug(outputFile);

      File file = new File(workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());
      if (!file.exists()) {
        logger.error("| Output file not exist: " + file);
        return validated;
      }

    }
    validated = true;

    return validated;
  }
  
  // Agent::putOutputFiles
  public boolean putOutputFiles() {
    if(STORAGE.equals("FTP")){
      return putOutputFiles_FTP();
    }else if(STORAGE.equals("Web")){
      return putOutputFiles_Web();
    }else{
      return false;
    }
  }

  // Agent::putOutputFiles_FTP
  public boolean putOutputFiles_FTP() {
    logger.info("+ Retrieving output files FTP");
    UUID uid = null;
    boolean result = false;
    try {

      for (String outputFile : jobMsg.getOutputFiles()) {
        File file = new File(workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());

        uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);

        int i = 1;
        while (uid == null && i < 6) {
          logger.error("UDClient login failed. retry : " + i);
          uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
          i++;
        }

        if (uid == null) {
          new Exception("Failed to get udclient uuid : null");
        }
        

        result = udc.putFile(uid, file.getAbsolutePath(), new File(outputFile).getParent(), agentId);

        logger.info("| " + outputFile + "(" + result + ")");
        int j = 1;
        while (!result) {
          if (j == 4) {
            logger.error("| " + outputFile + "(failed)");
            uploadAgentLog();
            return result;
          }
          udc.udclient.logout(uid, agentId);
          uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
          logger.info("| " + outputFile + "(" + j + " retransmitted)");
          result = udc.putFile(uid, file.getAbsolutePath(), new File(outputFile).getParent(), agentId);
          j++;
        }
        
        logger.info("addResult");
        dbclient.addResult(jobMsg.getJobId(), jobMsg.getMetaJobId(), outputFile);
      } // end for

      result = true;
    } catch (SocketTimeoutException e1) {
      logger.error("SocketTiemoutException");
      e1.printStackTrace();
      result = false;
    } catch (Exception e2) {
      logger.error("Unknown Exception");
      e2.printStackTrace();
      result = false;
    } finally {
      if (uid != null) {
        udc.udclient.logout(uid, agentId);
      }
    }
    return result;
  }
  
  // Agent::putOutputFiles_Web
  public boolean putOutputFiles_Web() {
    logger.info("+ Retrieving output files Web");
    boolean result = false;
    try {

      for (String outputFile : jobMsg.getOutputFiles()) {
        File file = new File(workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());

        result = udc.putFile_Web(file.getAbsolutePath(), new File(outputFile).getParent(), agentId, userId);

        logger.info("| " + outputFile + "(" + result + ")");
        int j = 1;
        while (!result) {
          if (j == 4) {
            logger.error("| " + outputFile + "(failed)");
            uploadAgentLog();
            return result;
          }
          logger.info("| " + outputFile + "(" + j + " retransmitted)");
          result = udc.putFile_Web(file.getAbsolutePath(), new File(outputFile).getParent(), agentId, userId);
          j++;
        }

        logger.info("addResult");
        dbclient.addResult(jobMsg.getJobId(), jobMsg.getMetaJobId(), outputFile);
      } // end for

      result = true;
    } catch (Exception e2) {
      logger.error("Unknown Exception");
      e2.printStackTrace();
      result = false;
    }
    
    return result;
  }

  // Upload agent log.
  // Agent::uploadAgentLog
  public void uploadAgentLog() {
    if(STORAGE.equals("FTP")){
      uploadAgentLog_FTP();
    }else if(STORAGE.equals("Web")){
      uploadAgentLog_Web();
    }else{
      return ;
    }
  }
  
  // Agent::uploadAgentLog_FTP
  public void uploadAgentLog_FTP() {
    logger.info("Uploading AgentLog : " + agentId);
    File file = new File("log/Agent.log");
    String logFileName = "agent." + agentId + ".log";
    UUID uid = null;
    try {
      if (file.exists()) {
        uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
        // udc.putFile(file.getAbsolutePath(), "/home/shlee/agent",
        // logFileName);
        int i = 1;
        while (uid == null && i < 6) {
          logger.error("UDClient login failed. retry : " + i);
          uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
          i++;
        }

        if (uid == null) {
          new Exception("UDClient login failed");
        }

        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.KOREA);
        Date currentTime = new Date();
        String mTime = mSimpleDateFormat.format(currentTime);

        boolean result = udc.putFile(uid, file.getAbsolutePath(), HTCaaS_AGENT_LOG_PATH + mTime, logFileName, agentId);
        logger.info("| " + logFileName + "(" + result + ")");
        int j = 1;
        while (!result) {
          udc.udclient.logout(uid, agentId);
          uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
          logger.info("| " + logFileName + "(" + j + " retransmitted)");
          result = udc.putFile(uid, file.getAbsolutePath(), HTCaaS_AGENT_LOG_PATH + mTime, logFileName, agentId);
          if (j == 3) {
            logger.error("| " + logFileName + "(failed)");
            return;
          }
          j++;
        }
      } else {
        logger.error("Log file not exist: " + file.getAbsolutePath());
      }
    } catch (SocketTimeoutException e1) {
      logger.error("SocketTimeoutException : Failed to upload agent log file", e1);
      e1.printStackTrace();
      return;
    } catch (Exception e2) {
      logger.error("Unknown Exception : Failed to upload agent log file", e2);
      e2.printStackTrace();
      return;
    } finally {
      if (uid != null) {
        udc.udclient.logout(uid, agentId);
      }
    }
  }
  
  // Agent::uploadAgentLog_Web
  public void uploadAgentLog_Web() {
    logger.info("Uploading AgentLog : " + agentId);
    File file = new File("log/Agent.log");
    String logFileName = "agent." + agentId + ".log";
    try {
      if (file.exists()) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.KOREA);
        Date currentTime = new Date();
        String mTime = mSimpleDateFormat.format(currentTime);

        boolean result = udc.putFile_Web(file.getAbsolutePath(), HTCaaS_AGENT_LOG_PATH + mTime, logFileName, agentId);
        logger.info("| " + logFileName + "(" + result + ")");
        int j = 1;
        while (!result) {
          logger.info("| " + logFileName + "(" + j + " retransmitted)");
          result = udc.putFile_Web(file.getAbsolutePath(), HTCaaS_AGENT_LOG_PATH + mTime, logFileName, agentId);
          if (j == 3) {
            logger.error("| " + logFileName + "(failed)");
            return;
          }
          j++;
        }
      } else {
        logger.error("Log file not exist: " + file.getAbsolutePath());
      }
    } catch (Exception e2) {
      logger.error("Unknown Exception : Failed to upload agent log file", e2);
      e2.printStackTrace();
      return;
    } 
  }
  
  // Creates the job log.
  // Agent::createJobLog
  public void createJobLog() {

    // DTO jobMsg = new DTO();
    // jobMsg.setJobId(1);

    logger.info("Creating JobLog : " + jobMsg.getJobId());
    jobLogFile = new File("log/Job.log");
    StringBuilder sb = new StringBuilder();

    try {

      // file read
      String temp;
      BufferedReader br = new BufferedReader(new FileReader(jobLogFile.getAbsolutePath()));
      while ((temp = br.readLine()) != null) {
        // System.out.println(temp);

        if (temp.contains("JobMsg JobID : " + jobMsg.getJobId())) {
          sb.setLength(0);
          sb.append(temp + "\n");
          while ((temp = br.readLine()) != null) {
            sb.append(temp + "\n");
            if (temp.contains("Cleaning Workspace JobID : " + jobMsg.getJobId())) {
              break;
            }
          }
        }
      }
      br.close();

      // file write
      FileWriter fw = new FileWriter("log/" + "job." + jobMsg.getJobId() + ".log");
      BufferedWriter bw = new BufferedWriter(fw);

      bw.write(sb.toString());
      bw.close();
      fw.close();

    } catch (FileNotFoundException e1) {
      logger.error("Job Log File not exit: {}", jobLogFile.getAbsolutePath());
      e1.printStackTrace();
      return;
    } catch (Exception e2) {
      logger.error("Job Log File read/write error: {}", jobLogFile.getAbsolutePath());
      e2.printStackTrace();
    }

    // try{
    // dbclient.setJobLog(jobMsg.getJobId(), sb.toString()); // set job log in
    // db
    // }catch(Exception e){
    // logger.error("Failed to set Job log : " + e.toString());
    // e.printStackTrace();
    // }
  }

  /**
   * Upload job log.
   */
  // Agent::uploadJobLog
  public void uploadJobLog() {
    if(STORAGE.equals("FTP")){
      uploadJobLog_FTP();
    }else if(STORAGE.equals("Web")){
      uploadJobLog_Web();
    }else{
      return ;
    }
  }
  
  // Agent::uploadJobLog_FTP
  public void uploadJobLog_FTP() {
    logger.info("Uploading JobLog : " + jobMsg.getJobId());

    String logFileName = "job." + jobMsg.getJobId() + ".log";
    jobLogFile = new File("log/" + logFileName);
    UUID uid = null;

    try {
      uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);

      int i = 1;
      while (uid == null && i < 6) {
        logger.error("UDClient login failed. retry : " + i);
        uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
        i++;
      }

      if (uid == null) {
        new Exception("UDClient login failed");
      }

      SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.KOREA);
      Date currentTime = new Date();
      String mTime = mSimpleDateFormat.format(currentTime);

      boolean result = udc.putFile(uid, jobLogFile.getAbsolutePath(), HTCaaS_JOB_LOG_PATH + mTime, logFileName, agentId);
      logger.info("| " + logFileName + "(" + result + ")");
      int j = 1;
      while (!result) {
        udc.udclient.logout(uid, agentId);
        uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
        logger.info("| " + logFileName + "(" + j + " retransmitted)");
        result = udc.putFile(uid, jobLogFile.getAbsolutePath(), HTCaaS_JOB_LOG_PATH + mTime, logFileName, agentId);
        if (j == 3) {
          logger.error("| " + logFileName + "(failed)");
          return;
        }
        j++;
      }

      if(!jobLogFlag){
        logger.info("setJobLog");
        jobLogFlag = dbclient.setJobLog(jobMsg.getJobId(), HTCaaS_JOB_LOG_PATH + mTime + "/" + logFileName); // set job log in db
      }
    } catch (SocketTimeoutException e1) {
      logger.error("SocketTimeoutException : Failed to upload job log file", e1);
      e1.printStackTrace();
    } catch (Exception e2) {
      logger.error("Unknown Exception : Failed to upload job log file", e2);
      e2.printStackTrace();
    } finally {
      udc.udclient.logout(uid, agentId);
      jobLogFile.delete();
    }

  }
  
  // Agent::uploadJobLog_Web
  public void uploadJobLog_Web() {
    logger.info("Uploading JobLog : " + jobMsg.getJobId());

    String logFileName = "job." + jobMsg.getJobId() + ".log";
    jobLogFile = new File("log/" + logFileName);

    try {
      SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.KOREA);
      Date currentTime = new Date();
      String mTime = mSimpleDateFormat.format(currentTime);

      boolean result = udc.putFile_Web(jobLogFile.getAbsolutePath(), HTCaaS_JOB_LOG_PATH + mTime, logFileName, agentId);
      logger.info("| " + logFileName + "(" + result + ")");
      int j = 1;
      while (!result) {
        logger.info("| " + logFileName + "(" + j + " retransmitted)");
        result = udc.putFile_Web(jobLogFile.getAbsolutePath(), HTCaaS_JOB_LOG_PATH + mTime, logFileName, agentId);
        if (j == 3) {
          logger.error("| " + logFileName + "(failed)");
          return;
        }
        j++;
      }

      logger.info("setJobLog");
      if(!jobLogFlag){
        jobLogFlag = dbclient.setJobLog(jobMsg.getJobId(), HTCaaS_JOB_LOG_PATH + mTime + "/" + logFileName); // set job log in db
      }
    } catch (Exception e2) {
      logger.error("Unknown Exception : Failed to upload job log file", e2);
      e2.printStackTrace();
    } finally {
      jobLogFile.delete();
    }

  }

  // WorkerThread::run() 에서 호출됨
  // Agent::executeJobs
  public boolean executeJobs(String user) {
    // 메시지가 있음

    boolean result = false;
    // localWaitingTime = waitingTime;
    Listening_Metric1 = initialListeningValue;

    try {
      // jobId 를 agentId 에 할당함
      logger.info("setAgentCurrentJob");
      if (dbclient.setAgentCurrentJob(agentId, jobId, metaJobId)) {

        if (passwd == null) {
          logger.info("getUserPasswd");
          passwd = dbclient.getUserPasswd(user);
        }
        // 입력 파일을 가지고 옴
        if (getInputFiles()) {

          // 어플리케이션 설치
          installApplication();

          // 작업을 시작함
          logger.info("startJob");
          if (!dbclient.startJob(jobId)) {
            throw new Exception("Failed to start job");
          }

          if (!execute()) {
            throw new Exception("Failed to excecute job");
          }
          // 출력 파일을 검증
          if (validateOutputFiles()) {
            // 결과 파일을 처리함
            boolean output = putOutputFiles();
            
            while (!output) {
              failure++;
              logger.info("PutOutputFiles failure #:" + failure );
              
              
              if(failure <= OUTPUT_RETRY_NUM){
                Thread.sleep(OUTPUT_RETRY_DELAY_TIME);
                output = putOutputFiles();
              }else{
                logger.info("PutOutputFiles failure # is over");
                break;
              }
            } 
            
            failure = 0;
            
            if(output){
                // 해당 작업을 끝냄
              logger.info("finishJob");
              if (!dbclient.finishJob(jobId, agentId)) {
                throw new Exception("Failed to finish Job");
              }
            } else {
              throw new OutputException("Output Transmission Failure");
            } // end if(output)
          } else {
            throw new OutputException("Output Validation Failure");
          }
        } else {
          throw new InputException("Input Transmission Failure");
        }

      } else {
        logger.info("| RemainingTime is not enough, the message is re-enqueued");
        failure = 999;
        logger.info("reEnqueueJob");
        if (!dbclient.reEnqueueJob(jobId)) {
          logger.error("Failed to reEnqueue");
          throw new Exception("Failed to reEnqueue");
        }
      }

    } catch (InputException e) {
      failure++;
      logger.warn("| InputException Failure #" + failure + ": ", e);

      try {
        logger.info("setJobStatus fail");
        if (!dbclient.setJobStatus(jobId, JOB_STATUS_FAIL)) {
          throw new Exception("Failed to set JobStatus");
        }
        logger.info("setJobErrormsg");
        if (!dbclient.setJobErrormsg(jobId, e.getMessage())) {
          throw new Exception("Failed to set JobErrormsg");
        }
      } catch (Exception e1) {
        e1.printStackTrace();
        logger.error("ExecuteJobs DBClient Inner Excetion1. Exit system ");
        endSignal = true;
        System.exit(1);
      }
    } catch (OutputException e) {
      failure++;
      logger.warn("| OutputException Failure #" + failure + ": ", e);

      try {
        logger.info("setJobStatus fail");
        if (!dbclient.setJobStatus(jobId, JOB_STATUS_FAIL)) {
          throw new Exception("Failed to set JobStatus");
        }
        logger.info("setJobErrormsg");
        if (!dbclient.setJobErrormsg(jobId, e.getMessage())) {
          throw new Exception("Failed to set JobErrormsg");
        }
      } catch (Exception e1) {
        e1.printStackTrace();
        logger.error("ExecuteJobs DBClient Inner Excetion2. Exit system ");
        endSignal = true;
        System.exit(1);
      }
    } catch (Exception e) {
      failure++;
      logger.warn("| Exception Failure #" + failure + ": ", e);

      try {
        logger.info("setJobStatus fail");
        if (!dbclient.setJobStatus(jobId, JOB_STATUS_FAIL)) {
          throw new Exception("Failed to set JobStatus");
        }
        logger.info("setJobErrormsg");
        if (!dbclient.setJobErrormsg(jobId, e.getMessage())) {
          throw new Exception("Failed to set JobErrormsg");
        }
      } catch (Exception e1) {
        e1.printStackTrace();
        logger.error("ExecuteJobs DBClient Inner Excetion3. Exit system ");
        endSignal = true;
        System.exit(1);
      }
    } finally {
      logger.info("| Cleaning Workspace JobID : " + jobId);
      logger.info("| Wokrdir : " + workDir.getAbsolutePath());
      try{
    	  if(workDir.exists()){
    		  DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
    	  }
      }catch(Exception e){
        logger.error("Failed to remove workdir :" + e.toString());
        e.printStackTrace();
      }
      WaitingTime = 1000;
      workDir.mkdirs();
      
      createJobLog();
      uploadJobLog();

      try {
        logger.info("getMetaJobStatus");
        if (dbclient.getMetaJobStatus(metaJobId).equals(METAJOB_STATUS_SPLIT)) {
          logger.info("checkMetaJobStatusBySubJob");
          dbclient.checkMetaJobStatusBySubJob(metaJobId);
        }

        logger.info("getUserKeepAgentNO");
        int keepAgent = dbclient.getUserKeepAgentNO(user);
        logger.info("getNumUserAgentStatus");
        int runAgent = dbclient.getNumUserAgentStatus(user, AGENT_STATUS_RUN);
        if (runAgent > keepAgent) {
          logger.info("RunningAgent # is " + runAgent);
          logger.info("UserKeepAgent# is " + keepAgent);
          logger.info("Agent # is over. Done by UserKeepAgentNO.");
          return result;
        }

        logger.info("setAgentCurrentJob");
        dbclient.setAgentCurrentJob(agentId, null, -1);
        
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("ExecuteJobs DBClient Inner Excetion4. Exit system ");
        endSignal = true;
        System.exit(1);
      }

      if (failure > 3) {
        logger.info("| Now Agent will be terminated, Failure :" + failure);
        result = false;
      } else {
        result = true;
      }
    }

    return result;
  }

  // Start a agent
  // Agent::start
  @SuppressWarnings("finally")
  public void start() {
    logger.info("+ Agent started at " + host);

    // ** ceDAO.updateCEAliveAgentAdd(agentDAO.readAgentCEId(agentId),num);
    // UPDATE CE SET aliveAgent = aliveAgent+(?) where id = ?

    try{
      logger.info("setCEAliveAgentAdd");
      dbclient.setCEAliveAgentAdd(agentId, 1);
    }catch(Exception e){
      logger.error("Failed to setCEAliveAgentAdd : " + e.toString());
      e.printStackTrace();
      logger.error("System exit");
      System.exit(1);
    }

    Thread wThread = new WorkerThread();
    wThread.setDaemon(true);
    wThread.start();
    int logTime = 0;

    while (true) {
      try {
        logger.info("MonitoringThread send a signal");

        int jobId = -1;
        if (jobMsg != null) {
          jobId = jobMsg.getJobId();
        }

        if (endSignal) {
          logger.error("endSignal exit");
          System.exit(0);
        }

        if (!wThread.isAlive()) {
          logger.error("WorkerThread was dead. System exits");
          System.exit(0);
        }

        Map<String, Boolean> signal = dbclient.sendAgentSignal(agentId, jobId);

        if (signal.get(AgentConstant.AGENT_QUIT) || signal.get(JobConstant.JOB_STOP)) {
          try {
            logger.info("Stop result : " + stop());
          } catch (Exception e) {
            logger.error("Failed to stop Agent", e.toString());
          } finally {
            throw new StopException("Agent Stop Signal");
          }
        }

        mFailure = 0;
        
      } catch (StopException e1) {
        endSignal = true;
        logger.error("StopException :{}", e1.toString());
        e1.printStackTrace();
        logger.info("setCEAliveAgentAdd -1");
        dbclient.setCEAliveAgentAdd(agentId, -1);
        try{
        	uploadAgentLog();
        }catch(Exception e3){
        	logger.error("Failed to upload AgentLog :" + e3.toString());
        }
        logger.error("System exit");
        System.exit(1);
      } catch (Exception e2) {
        mFailure++;
        logger.error("MonitoringThread Failure # : " + mFailure + ", " + e2.toString());
        e2.printStackTrace();

        if (mFailure > 6) {
          endSignal = true;
          try {
            if (jobId != -1) {
              logger.info("setJobStatus fail");
              dbclient.setJobStatus(jobId, JOB_STATUS_FAIL);
              logger.info("setJobErrormsg");
              dbclient.setJobErrormsg(jobId, "Monitoring Fail");
              logger.info("setCEAliveAgentAdd -1");
              dbclient.setCEAliveAgentAdd(agentId, -1);
              logger.info("reEnqueueJob");
              dbclient.reEnqueueJob(jobId);
            }
            logger.info("reportAgentFailure");
            dbclient.reportAgentFailure(agentId);
            uploadAgentLog();
            logger.error("System exit");
            System.exit(1);
          } catch (Exception e) {
            e.printStackTrace();
            logger.error("Monitoring DBClient Inner Excetion1. Exit system ");
            try{
            	uploadAgentLog();
            }catch(Exception e3){
            	logger.error("Failed to upload AgentLog :" + e3.toString());
            }
            logger.error("System exit");
            System.exit(1);
          }

        }// end if(mFailure)
      }// end catch(Exception e2)
      
      try {
        Thread.sleep(signalPeriod * 60 * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      logTime += signalPeriod;

      if (logTime == 10) {
        if(jobMsg != null){
          createJobLog();
          uploadJobLog();
        }
        logTime = 0;
      }

    }
  }

  // Agent::stop
  public boolean stop() {
    logger.info("+ Agent stopped by the request");

    if (jobMsg != null) {
      logger.info("setJobStatus cancel");
      dbclient.setJobStatus(jobMsg.getJobId(), JOB_STATUS_CANCEL);
      // uploadJobLog();
    }

    logger.info("stopAgent");
    return dbclient.stopAgent(agentId);

  }

  /**
   * The agent main method.
   * 
   * @param args
   *          the arguments
   * @throws Exception
   *           the exception
   */
  // Agent::main
  public static void main(String args[]) throws Exception {

    if (args.length > 0) {
      agentId = Integer.parseInt(args[0]);
      userId = args[1];
    }

    // 새로운 Agent 객체에서 start()
    Agent agent = new Agent();
    agent.start();
    
    //    int tn = 200 ;
    //    ExecutorService es = Executors.newFixedThreadPool(tn);
    //    System.out.println("[Submitting tasks...]");
    //       for (int i = 1; i <= tn; i++) {
    //         logger.info("start i  : " + i);
    //         es.execute(agent.new UDThread(i));
    //         logger.info("finish i  : " + i);
    //         try {
    //           Thread.sleep(1);
    //         } catch (InterruptedException e) {
    //           // TODO Auto-generated catch block
    //           e.printStackTrace();
    //         }
    //       }
    //       System.out.println("[Finish submitting!]");
    //
    //       es.shutdown();
    //       System.out.println("[Shutdown]");
    //        db.jobclient.getInteger(10);
  }
  
  /// class WorkerThread {{{
  private class WorkerThread extends Thread {

    // Agent::WorkerThread::WorkerThread
    public WorkerThread() {
    }
    
    // Agent::WorkerThread::run()
    @SuppressWarnings("finally")
    @Override
    public void run() {

      try {
        // agent id(aid))로 상태를 조회하여 running 상태이면
        // 종료
        logger.info("getAgentStatus run");
        if (dbclient.getAgentStatus(agentId).equals(AGENT_STATUS_RUN)) {
          endSignal = true;
          logger.error("Agent before-Status is running. System exits.");
          throw new Exception("Failed to run the job");
        }

        // ** agentDAO.updateAgentHost(agentId, host);
        // update Agent set host=?, lastSignal=now() where id=?
        logger.info("setAgentHost");
        if (!dbclient.setAgentHost(agentId, host)) {
          throw new Exception("Failed to set AgentHost");
        }
        
        logger.info("checkAgentQuit");
        if (dbclient.checkAgentQuit(agentId)) {
          logger.info("| Admin wants for this agent to quit..., wating to quit");
          Thread.sleep(1 * 600 * 1000);

        } else if (endSignal) {
          logger.info("| EndSignal. Waiting to exit system");
          Thread.sleep(1 * 600 * 1000);
        }

        logger.info("startAgent");
        if (!dbclient.startAgent(agentId)) {
          throw new Exception("Failed to startAgent");
        }
        // ** ceDAO.updateCENumAgentRunning(ceId, agentId);
        // UPDATE CE SET numAgentRunning=numAgentRunning+1,
        // waitingTime=TIME_TO_SEC(TIMEDIFF(now(), (SELECT
        // submittedTimestamp FROM Agent where id=?))) where id=?

        logger.info("| AgentID : " + agentId);
        logger.info("| Working Directory : " + workDir.getAbsolutePath());

        // 모니터링 쓰레드 (private class)

        // int localWaitingTime = waitingTime;

        try {
          while (true) {

            jobMsg = null;
            jobLogFlag = false;
            metaJobId = -1;
            jobId = -1;

            // while (true) {
            logger.info("checkAgentQuit inner");
            if (dbclient.checkAgentQuit(agentId)) {
              logger.info("| Admin wants for this agent to quit..., wating to quit");
              Thread.sleep(1 * 600 * 1000);
              break;

            } else if (endSignal) {
              logger.info("| EndSignal. Waiting to exit system");
              Thread.sleep(1 * 600 * 1000);
              break;
            }
            // }

            // retrieve a message from InputQueue
            // 메시지가 없음

            if (Listening_Priority) {
              if (!requestJob(userId, Listening_Metric1)) {
                logger.info("| No jobs in InputQueue,  Current Queue ListeningTime : " + Listening_Metric1 / 1000 + " s");

                initialListeningValue = Listening_Metric1;
                Listening_Metric1 *= ListeningMultiple_Metric;

                if (Listening_Metric1 > ListeningTime_Limit) {
                  logger.info("| Now Agent will be terminated");
                  break;
                }
              } else {
                if (!executeJobs(userId)) {
                  break;
                }
              }
            } else {
              if (!requestJob(userId, Listening_Metric2)) {
                logger.info("| No jobs in InputQueue,  Current Queue WaitingTime : " + WaitingTime / 1000 + " s");
                
              if (WaitingTime > WaitingTime_Limit) {
                  logger.info("| Now Agent will be terminated");
                  break;
                }
		
                initialWaitingValue = WaitingTime;

                Thread.sleep(WaitingTime);

                WaitingTime += WaitingAddition_Metric;

              } else {
                if (!executeJobs(userId)) {
                  break;
                }
              }
            }

          } // end while
        } catch (InterruptedException e) {
        }

        // 에이전트 정상적인 종료
        logger.info("Finishing Agent");
        if (!dbclient.finishAgent(agentId)) {
          logger.error("Failed to finishAgent");
          throw new Exception("Failed to finishAgent");
        }
        
        logger.info("+ Agent successfully finished");

      } catch (Exception e) {
        logger.error("+ Error occurred while agent is running : " + e.toString());
        e.printStackTrace();
        try {
          logger.info("reportAgentFailure");
          if (!dbclient.reportAgentFailure(agentId)) {
            throw new Exception("Failed to report AgentFailure");
          }
        } catch (Exception e1) {
          e1.printStackTrace();
          logger.error("WorkerThread run DBClient Inner Excetion1. Exit system ");
          endSignal = true;
          logger.error("System exits");
          System.exit(1);
        }

      } finally {
        try{
          uploadAgentLog();
        }catch(Exception e){
          logger.error("Failed to uploadAgentLog :" + e.toString());
          e.printStackTrace();
        }
        
        try{
          logger.info("setCEAliveAgentAdd -1");
          dbclient.setCEAliveAgentAdd(agentId, -1);
        }catch(Exception e){
          logger.error("Failed to setCEAliveAgentAdd :" + e.toString());
          e.printStackTrace();
        }
        endSignal = true;
        logger.warn("System exit");
        System.exit(0);
      }

    }

  }
  /// class WorkerThread }}}
}
