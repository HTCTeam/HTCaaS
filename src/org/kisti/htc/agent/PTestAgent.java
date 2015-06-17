
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.jms.JMSException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.message.DTO;
import org.kisti.htc.message.DirectConsumer;
import org.kisti.htc.message.MessageSender;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PTestAgent {

	private static final Logger logger = LoggerFactory.getLogger(PTestAgent.class);
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;


	private Database dbclient; // DBManager client

//	private Monitoring monitoring; // Monitoring client

	private UDClient udc; // UserDataManager client

	private DirectConsumer messageConsumer; // ActiveMQ client

	private int waitingTime = 50000;
	private final int WAITINGTIMELIMIT = 20000;

	private int agentId;
	private int metaJobId;
	private int jobId;
	private String host;
	private DTO jobMsg;
	private File workDir;
	private File jobLogFile;
	private File outputDir;
	private String userId;
	private String passwd;

	private static String FTPAddress;
	private static String DBManagerURL;
//	private static String MonitoringURL;
	private String adminUser = "htcaas";
	private String adminPasswd = "htcaas";
	private static int signalPeriod = 1;

	// private String JMXServiceURL;
	// private String JMXObjectName;

	/**
	 * Instantiates a new agent.
	 */
	private PTestAgent() {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			signalPeriod = Integer.parseInt(prop.getProperty("Agent.Heartbeat.Period"));

//			if(prop.getProperty("SSL.Authentication").equals("true")){
//				SSL = true;
//				DBManagerURL = DBManagerURL.replace("http", "https");
////				MonitoringURL = MonitoringURL.replace("http", "https");
//				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
//				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
//				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
//				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
//			}

			FTPAddress = prop.getProperty("FTP.Address");
//			logger.info("FTP.Address : " + FTPAddress);

//			DBManagerURL = prop.getProperty("DBManager.Address");
//			logger.info("DBManagerURL : " + DBManagerURL);

//			MonitoringURL = prop.getProperty("Monitoring.Address");
//			logger.info("MonitoringURL : " + MonitoringURL);
			
		} catch (Exception e) {
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
//		ClientProxyFactoryBean dbFactory = new ClientProxyFactoryBean();
//		dbFactory.setServiceClass(Database.class);
//		dbFactory.setAddress(DBManagerURL);
//		dbFactory.setDataBinding(new AegisDatabinding());
//		dbclient = (Database) dbFactory.create();

		// Monitoring client
//		 ClientProxyFactoryBean mFactory = new ClientProxyFactoryBean();
//		 mFactory.setServiceClass(Monitoring.class);
//		 mFactory.setAddress(MonitoringURL);
//		 mFactory.setDataBinding(new AegisDatabinding());
//		 monitoring = (Monitoring) mFactory.create();

		// UDManager client (CXF)
		udc = new UDClient();

		// ActiveMQ client
		messageConsumer = new DirectConsumer(host);
		
//		if(SSL){
//        	try {
//				setupTLS(dbclient);
////				setupTLS(monitoring);
//				setupTLS(udc);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (GeneralSecurityException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

	}
	
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
		tlsCP.setSecureSocketProtocol("SSL");   // addme
		
		httpConduit.setTlsClientParameters(tlsCP);

	}

	private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
		fac.init(trustStore);
		return fac.getTrustManagers();
	}

	private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
		KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
		fac.init(keyStore, keyPass);
		return fac.getKeyManagers();
	}

	/**
	 * Creates the directories.
	 */
	private void createDirectories() {
		workDir = new File("workspace");
		if (workDir.exists()) {
			DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
		}
		// make a new directory "workspace"
		workDir.mkdirs();

		// make a directory workspace/local/output
//		outputDir = new File("local/output");
//		outputDir.mkdirs();
	}

	// retrieve a message from InputQueue
	/**
	 * Request job.
	 * 
	 * @return true, if successful
	 */
	public boolean requestJob(String user, int waitingTime) {
		logger.info("+ Retrieving a job message from InputQueue");

		try {
			jobMsg = messageConsumer.getMessage(user, waitingTime);
			jobId = jobMsg.getJobId();
//			metaJobId = dbclient.getJobMetaJobId(jobId);  
//			dbclient.decreaseMetaJobNum(metaJobId);
//			logger.info("| JobMsg JobID : " + jobMsg.getJobId() + ", AppName: " + jobMsg.getAppName() + ", userId: " + jobMsg.getUserId());
			this.userId = jobMsg.getUserId();
//			passwd = dbclient.getUserPasswd(user);// /??????????????????????????
			passwd = "kisti4001!@#";

		} catch (Exception e) {
			logger.error("Failed to retrive a job message from InputQueue", e);
			return false;
		}

		return true;
	}

	/**
	 * Retrieve input files.
	 * 
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean getInputFiles() throws Exception {
		logger.info("+ Retrieving input files");

		UUID uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
		// udc.client 는 UserDataManager(interface) type
		// interface org.kisti.htc.udmanager.server.UserDataManager
		// public UUID login(String address, String id, String passwd);
		// public class UserDataManagerImpl implements UserDataManager

		for (String inputFile : jobMsg.getInputFiles()) {
			File file = new File(inputFile);
			boolean result = false;
//			long serverChecksum = udc.getServerChecksum(file.getName(), file.getParent());
			try {
				result = udc.getFile(uid, file.getName(), file.getParent(), workDir.getAbsolutePath(), agentId);
			} catch (Exception e) {
				throw new Exception("Input Transmission Exception");
			}

			logger.info("| " + inputFile + "(" + result + ")");

//			long localChecksum = udc.getLocalChecksum(file.getName(), workDir.getAbsolutePath());

//			int y = 1;
//			while (!result || (localChecksum != serverChecksum)) {
//				logger.info("Checksum false : " + file.getName());
//
//				if (y == 4) {
//					logger.error("| " + inputFile + "(failed)");
//
//					udc.udclient.logout(uid, agentId);
//					uploadAgentLog();
//					return false;
//				}
//				logger.info("| " + inputFile + "(" + y + " retransmitted)");
//				uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
//				result = udc.getFile(uid, file.getName(), file.getParent(), workDir.getAbsolutePath(), agentId);
//				localChecksum = udc.getLocalChecksum(file.getName(), workDir.getAbsolutePath());
//				y++;
//			}

			logger.info("Checksum true : " + file.getName());
		}

		udc.udclient.logout(uid, agentId);
		return true;
	}

	/**
	 * Install application.
	 * 
	 * @throws Exception
	 *             the exception
	 */
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
	 *             the exception
	 */
	public boolean execute() throws Exception {
		logger.info("+ Executing application");

		List<String> command = new ArrayList<String>();
		command.add("/bin/sh");
		command.add(workDir.getAbsolutePath() + File.separator + jobMsg.getExecutable());
		if(jobMsg.getAppName().equals("pTest")){
			command.add(jobMsg.getUserId());
			command.add(String.valueOf(jobMsg.getJobId()));
		}else{
			for (String arg : jobMsg.getArguments()) {
				if (arg.equals("@AGENT_TIME")) {
					// long remainingTime =
					// (dbclient.getAgentRemainingTime(agentId)) / 60;
					long remainingTime = 0;
					logger.info("| @AGENT_TIME=" + remainingTime);
					command.add("" + remainingTime);
				} else {
					command.add(arg);
				}
			}
		}

		ProcessBuilder builder = new ProcessBuilder(command);
		// Map<String, String> envs = builder.environment();
		// for (String env : jobMsg.getEnvironments()) {
		// String[] values = env.split("=");
		// if (values.length > 1) {
		// envs.put(values[0], values[1]);
		// logger.debug(values[0] + "=" + values[1]);
		// }
		// }

		builder.directory(workDir);

		Process process = builder.start();
		logger.info("| [InputStream]");
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = br.readLine()) != null) {
			logger.info("| " + line);
		}
		br.close();

		boolean error = false;

		logger.info("| [ErrorStream]");
		br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ((line = br.readLine()) != null) {
			logger.info("| " + line);
			if (line.contains("error") || line.contains("sorry"))
				error = true;
			else if (line.contains("Successful Completion")) {
				error = false;
			}
		}

		return error;

	}

	/**
	 * Validate output files.
	 * 
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean validateOutputFiles() throws Exception {
		logger.info("+ Validating output files");

		boolean validated = true;

		for (String outputFile : jobMsg.getOutputFiles()) {
			logger.debug(outputFile);

			File file = new File(workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());
			if (!file.exists()) {
				logger.error("| Output file not exist: " + file);
				validated = false;
			}
		}

		return validated;
	}

	public boolean putOutputFiles() throws Exception {
		logger.info("+ Retrieving output files");
		UUID uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);

		for (String outputFile : jobMsg.getOutputFiles()) {
			File file = new File(workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());

			boolean result = false;
			try {
				result = udc.putFile(uid, file.getAbsolutePath(), new File(outputFile).getParent(), agentId);
			} catch (Exception e) {
				throw new Exception("Output Transmission Exception");
			}

			logger.info("| " + outputFile + "(" + result + ")");
			int i = 1;
			while (!result) {
				if (i == 4) {
					logger.error("| " + outputFile + "(failed)");
					udc.udclient.logout(uid, agentId);
					uploadAgentLog();
					return false;
				}
				logger.info("| " + outputFile + "(" + i + " retransmitted)");
				uid = udc.udclient.login(FTPAddress, userId, passwd, agentId);
				result = udc.putFile(uid, file.getAbsolutePath(), new File(outputFile).getParent(), agentId);
				i++;
			}

			dbclient.addResult(jobMsg.getJobId(), jobMsg.getMetaJobId(),  outputFile);
		}

		udc.udclient.logout(uid, agentId);
		return true;
	}

	/**
	 * Upload agent log.
	 */
	public void uploadAgentLog() {
		logger.info("Uploading AgentLog : " + agentId);
		File file = new File("log/Agent.log");
		String logFileName = "agent." + agentId + ".log";
		if (file.exists()) {
			UUID uid = null; 
			try {
				uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
				// udc.putFile(file.getAbsolutePath(), "/home/shlee/agent",
				// logFileName);
				boolean result = udc.putFile(uid, file.getAbsolutePath(), "/home/" + adminUser + "/agent", logFileName, agentId);
				logger.info("| " + logFileName + "(" + result + ")");
				int i = 1;
				while (!result) {
					logger.info("| " + logFileName + "(" + i + " retransmitted)");
					uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
					result = udc.putFile(uid, file.getAbsolutePath(), "/home/" + adminUser + "/agent", logFileName, agentId);
					if (i == 3) {
						logger.error("| " + logFileName + "(failed)");
						udc.udclient.logout(uid, agentId);
						return;
					}
					i++;
				}

			} catch (Exception e) {
				logger.error("Failed to upload agent log file", e);
				udc.udclient.logout(uid, agentId);
				return;
			}

			udc.udclient.logout(uid, agentId);
		} else {
			logger.error("Log file not exist: " + file.getAbsolutePath());
		}
	}

	/**
	 * Creates the job log.
	 */
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
			return;
		} catch (Exception e2) {
			logger.error("Job Log File read/write error: {}", jobLogFile.getAbsolutePath());
		}

		// System.out.println("aaa"+sb.toString());
		dbclient.setJobLog(jobMsg.getJobId(), sb.toString()); // set job log in
																// db
	}

	/**
	 * Upload job log.
	 */
	public void uploadJobLog() {
		logger.info("Uploading JobLog : " + jobMsg.getJobId());

		String logFileName = "job." + jobMsg.getJobId() + ".log";
		jobLogFile = new File("log/" + logFileName);

		UUID uid = null; 
		try {
			uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
			// udc.putFile(jobLogFile.getAbsolutePath(), "/home/shlee/job");
			boolean result = udc.putFile(uid, jobLogFile.getAbsolutePath(), "/home/" + adminUser + "/job", logFileName, agentId);
			logger.info("| " + logFileName + "(" + result + ")");
			int i = 1;
			while (!result) {
				logger.info("| " + logFileName + "(" + i + " retransmitted)");
				uid = udc.udclient.login(FTPAddress, adminUser, adminPasswd, agentId);
				result = udc.putFile(uid, jobLogFile.getAbsolutePath(), "/home/" + adminUser + "/job", logFileName, agentId);
				if (i == 3) {
					logger.error("| " + logFileName + "(failed)");
					udc.udclient.logout(uid, agentId);
					return;
				}
				i++;
			}
		} catch (SocketTimeoutException e1) {
			logger.error("Failed to upload job log file", e1);

		} catch (Exception e) {
			logger.error("Failed to upload job log file", e);
		}
		udc.udclient.logout(uid, agentId);

		jobLogFile.delete();
	}

	/**
	 * Start a agent
	 * 
	 * @param aid
	 *            the aid
	 */
	public void start(int aid, String user) {
		// startTimestamp = new Timestamp(Calendar.getInstance().getTime()
		logger.info("+ Agent started at " + host);

		this.agentId = aid;

		try {
			// agent id(aid))로 상태를 조회하여 running 상태이면 종료
/*			if (dbclient.getAgentStatus(aid).equals("running")) {
				logger.info("Agent before-Status is running. System exits.");
				System.exit(1);
			}

			dbclient.setAgentHost(agentId, host);
			// ** agentDAO.updateAgentHost(agentId, host);
			// update Agent set host=?, lastSignal=now() where id=?

			dbclient.startAgent(agentId);
			// ** ceDAO.updateCENumAgentRunning(ceId, agentId);
			// UPDATE CE SET numAgentRunning=numAgentRunning+1,
			//    waitingTime=TIME_TO_SEC(TIMEDIFF(now(), (SELECT submittedTimestamp FROM Agent where id=?))) where id=?
			dbclient.setCEAliveAgentAdd(aid, 1);
			// ** ceDAO.updateCEAliveAgentAdd(agentDAO.readAgentCEId(agentId), num);
	        // UPDATE CE SET aliveAgent = aliveAgent+(?) where id = ?

			logger.info("| AgentID : " + agentId);
			logger.info("| Working Directory : " + workDir.getAbsolutePath());

			// 모니터링 쓰레드 (private class)
			Thread mThread = new MonitoringThread(this);
			mThread.setDaemon(true);
			mThread.start();

			// int localWaitingTime = waitingTime; */
			int failure = 0;

			try {
				while (true) {
					jobMsg = null;

/*					while (true) {
						// 해당 에이전트의 quick 값을 조회 (SELECT quit FROM Agent WHERE id=?)
						if (!dbclient.checkAgentQuit(agentId)) {
							break;
						}

						logger.info("| Admin wants for this agent to quit..., wating to quit");
						Thread.sleep(1 * 60 * 1000);
					}
*/
					// retrieve a message from InputQueue
					// 메시지가 없음
					if (!requestJob(user, waitingTime)) {
						/*if (waitingTime == 1) {
							logger.info("| No jobs in InputQueue");
							break;
						} else {
							logger.info("| No jobs in InputQueue,  queue waitingTime : " + waitingTime / 1000 + " s");

							// localWaitingTime *= 2;
							waitingTime *= 2;

							if (waitingTime > WAITINGTIMELIMIT) {
								logger.info("| Now Agent will be terminated");
								break;
							}

						}*/
						System.exit(1);

					} else { // 메시지가 있음

						// localWaitingTime = waitingTime;
//						waitingTime = 10000;

						try {

							// jobId 를 agentId 에 할당함
//							if (dbclient.setAgentCurrentJob(agentId, jobId)) {
							if(true){
 								// UPDATE Job SET CE_id=(SELECT CE_id from Agent where id=?), agent_id=? where id = ?

//								dbclient.setJobStatus(jobId, "preparing");

								// 입력 파일을 가지고 옴
								if (getInputFiles()) {

									// 어플리케이션 설치
//									installApplication();

									// 작업을 시작함
//									dbclient.startJob(jobId);

									if (execute() == true) {
										throw new Exception("Execution Failure!");
									}
									// 출력 파일을 검증
									/*if (validateOutputFiles()) {
										// 결과 파일을 처리함
										if (putOutputFiles()) {
											// 해당 작업을 끝냄
											dbclient.finishJob(jobId, agentId);
											// UPDATE Job SET status = 'done', runningTime = TIME_TO_SEC(TIMEDIFF(now(),startTimestamp)) ,lastUpdateTime = now() where id = ?

										} else {
											throw new Exception("Output Transmission Failure");
										}
									} else {
										throw new Exception("Output Validation Failure");
									}*/
								} else {
									throw new Exception("Input Transmission Failure");
								}

							} else {
								logger.info("| RemainingTime is not enough, the message is re-enqueued");

								// TEST
								dbclient.setJobStatus(jobId, "zombie");

								try {
									MessageSender upload = new MessageSender(user);
									upload.sendMessage(jobMsg);
									upload.close();
									dbclient.increaseMetaJobNum(metaJobId);
								} catch (JMSException e) {
									logger.error("| Failed to re-enqueue a job message", e);
									dbclient.setJobStatus(jobId, "zombie");
								}

								// failure = 999;
								throw new Exception("NotEnoughRemainingTime Exception");
							}

						} catch (Exception e) {
							failure++;
							logger.warn("| Failure #" + failure + ": ", e);

							dbclient.setJobStatus(jobId, "failed");
							dbclient.setJobErrormsg(jobId, e.getMessage());

							// if (failure > 2) {
							logger.info("| Now Agent will be terminated");
							throw new Exception("AgentFailure");
							// }

							// logger.info("reEnqueueJob: " + jobId);
							// int num = dbclient.getJobNumResubmit(jobId);
							// if (num < 3) {
							// dbclient.reEnqueueJob(jobId);
							// dbclient.increaseMetaJobNum(metaJobId);
							// } else {
							// logger.info("Num of reSubmitJobTry is Over");
							// }

						} finally {
//							logger.info("| Cleaning Workspace JobID : " + jobId);

//							dbclient.checkMetaJobStatusByNum(metaJobId);
//							dbclient.checkMetaJobStatusBySubJob(metaJobId);
							// workDir.mkdirs();
//							createJobLog();
//							uploadJobLog();

//							if (dbclient.getNumUserAgentRunning(user) > dbclient.getUserKeepAgentNO(user)) {
//								logger.info("RunningAgent # is " + dbclient.getNumUserAgentRunning(user));
//								logger.info("UserKeepAgent# is " + dbclient.getUserKeepAgentNO(user));
//								logger.info("Agent # is over. Done by UserKeepAgentNO.");
//								break;
//							}
						}

					}
				}
			} catch (InterruptedException e) {
			}

			// 에이전트 정상적인 종료
//			dbclient.finishAgent(agentId);
//			DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
//			logger.info("+ Agent successfully finished");

		} catch (Exception e) {
			logger.error("+ Error occurred while agent is running", e);
			dbclient.reportAgentFailure(agentId);

		} finally {
			/*uploadAgentLog();
			dbclient.setCEAliveAgentAdd(aid, -1);*/
		}

	}

	/**
	 * Test.
	 */
	public void test() {

		Thread mThread = new MonitoringThread(this);
		mThread.setDaemon(true);
		mThread.start();

		while (true) {
			logger.info("MainThread is Alive");

			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Stop.
	 */
	public void stop() {
		logger.info("+ Agent stopped by the request");

		dbclient.stopAgent(agentId);

		uploadAgentLog();

		if (jobMsg != null) {
			dbclient.setJobStatus(jobMsg.getJobId(), "canceled");
			// uploadJobLog();
		}

	}

	/**
	 * The agent main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String args[]) throws Exception {

		// agentId 와 userId 를 아규먼트로 받음
		String userId = "p258rsw";
		int agentId = 1550;

		if (args.length > 0) {
			agentId = Integer.parseInt(args[0]);
			userId = args[1];
		}

		// 새로운 Agent 객체에서 start()
		new PTestAgent().start(agentId, userId);
	}

	/**
	 * The Class MonitoringThread.
	 */
	private class MonitoringThread extends Thread {
		// private BrokerViewMBean mbean;
		// private MBeanServerConnection connection;
		// private QueueViewMBean userQueue = null;

		/** The agent. */
		private PTestAgent agent;

		/**
		 * Instantiates a new monitoring thread.
		 * 
		 * @param agent
		 *            the agent
		 */
		public MonitoringThread(PTestAgent agent) {
			this.agent = agent;

			// try {
			// JMXServiceURL url = new JMXServiceURL(JMXServiceURL);
			// JMXConnector connector = JMXConnectorFactory.connect(url);
			// connector.connect();
			// connection = connector.getMBeanServerConnection();
			// ObjectName name = new ObjectName(JMXObjectName);
			// mbean = MBeanServerInvocationHandler.newProxyInstance(connection,
			// name, BrokerViewMBean.class, true);
			//
			// logger.info("Statistics for broker " + mbean.getBrokerId() +
			// " - " + mbean.getBrokerName());
			//
			//
			// // if (userQueue == null) {
			// // throw new Exception("Queue not exist: " + userQueue);
			// // }
			//
			// } catch (Exception e) {
			// logger.error("Cannot access ActiveMQ Broker for Statistics", e);
			// System.exit(1);
			// }
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			// int i = 0;
			while (true) {
				logger.debug("MonitoringThread send a signal");

				int jobId = -1;
				try {
					if (jobMsg != null) {
						jobId = jobMsg.getJobId();
					}

					Map<String, Boolean> signal = dbclient.sendAgentSignal(agentId, jobId);
					// boolean quit = false;
					if (signal.get("agentQuit") || signal.get("jobStop")) {
						try {
							agent.stop();
						} catch (Exception e) {
							logger.error("Failed to stop Agent", e);
						} finally {
							dbclient.setCEAliveAgentAdd(agentId, -1);
							System.exit(1);
						}
					}

					try {
						Thread.sleep(signalPeriod * 60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					logger.error("MonitoringThread Failure: " + e.getMessage());
					dbclient.reportAgentFailure(agentId);
					if (jobId != -1) {
						dbclient.setJobStatus(jobId, "failed");
						dbclient.setJobErrormsg(jobId, "Monitoring Fail");
						dbclient.reEnqueueJob(jobId);
						// dbclient.increaseMetaJobNum(metaJobId);
						// dbclient.checkMetaJobStatusByNum(jobId);
					}

					uploadAgentLog();
					dbclient.setCEAliveAgentAdd(agentId, -1);
					System.exit(1);
				}
			}

		}

	}

}
