package org.kisti.htc.agent;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.message.DTO;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SamplingAgent {

	private static final Logger logger = LoggerFactory.getLogger(SamplingAgent.class);

	private Database dbclient; // DBManager client
	private UDClient udc; // UserDataManager client
	// private UDClient udc_agent;

	private int agentId;
	private String host;
	private DTO jobMsg;

	private int waitingTime;
	private File workDir;
	private File outputDir;
	private static String FTPAddress;
	private static String DBManagerURL;

	private String adminUser = "htcaas";
	private String adminPasswd = "htcaas";

	private static int signalPeriod = 1;

	private SamplingAgent() {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Agent.conf"));
			signalPeriod = Integer.parseInt(prop.getProperty("Agent.Heartbeat.Period"));

			FTPAddress = prop.getProperty("FTP.Address");
			logger.info("FTP.Address : {}", FTPAddress);

			DBManagerURL = prop.getProperty("DBManager.Address");
			logger.info("DBManagerURL : {}", DBManagerURL);

		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}

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

		udc = new UDClient();

	}

	private void createDirectories() {
		workDir = new File("workspace");
		if (workDir.exists()) {
			DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
		}
		workDir.mkdirs();

		outputDir = new File("local/output");
		outputDir.mkdirs();
	}

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
					result = udc.putFile(uid, file.getAbsolutePath(), "/home/" + adminUser + "/agent", logFileName, agentId);
					if (i == 3) {
						logger.error("| " + logFileName + "(failed)");
						udc.udclient.logout(uid, agentId);
						return;
					}
					i++;
				}
			} catch (SocketTimeoutException e1) {
				logger.error("Failed to upload agent log file", e1);
				return;
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

	public void start(int aid) {
		logger.info("+ SamplingAgent started at " + host);

		// int num = 1;
		// logger.info("checking Agent 'submitted' Status...");
		// while(true){
		// try {
		// if(dbclient.getAgentStatus(aid).equals("submitted") ||
		// dbclient.getAgentStatus(aid).equals("submitted-zombie")){
		// break;
		// }
		// Thread.sleep(6000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// uploadLog();
		// System.exit(1);
		// }
		//
		// if(num==10) System.exit(1);
		// num++;
		// }

		this.agentId = aid;

		try {
			dbclient.setAgentHost(agentId, host);
			dbclient.startAgent(agentId);

			logger.info("| AgentID : " + agentId);
			logger.info("| Working Directory : " + workDir.getAbsolutePath());

			Thread mThread = new MonitoringThread(this);
			mThread.setDaemon(true);
			mThread.start();

			try {
				jobMsg = null;
				while (true) {
					if (!dbclient.checkAgentSleep(agentId))
						break;

					logger.info("| Admin wants for this agent to sleep..., sleepfing for 1 min");
					Thread.sleep(1 * 60 * 1000);

				}

				while (true) {
					if (!dbclient.checkAgentQuit(agentId))
						break;

					logger.info("| Admin wants for this agent to quit..., wating to quit");
					Thread.sleep(1 * 60 * 1000);

				}

				if (waitingTime == 0) {
					waitingTime = 5;
				}

				logger.info("| Sampling agent process.... " + waitingTime + " sec");
				Thread.sleep(waitingTime * 1 * 1000);

				logger.info("| Now SamplingAgent will be terminated");

			} catch (InterruptedException e) {
			}

			dbclient.finishAgent(agentId);

			logger.info("+ SamplingAgent successfully finished");

			// logger.info("| Cleaning Workspace");
			// DeleteFileAndDirUtil.deleteFilesAndDirs(workDir
			// .getAbsolutePath());

		} catch (Exception e) {
			logger.error("+ Error occurred while samplingagent is running", e);

			dbclient.reportAgentFailure(agentId);
			// /insert
		} finally {
			uploadAgentLog();
		}

	}

	public void stop() {
		logger.info("+ SamplingAgent stopped by the request");

		dbclient.stopAgent(agentId);

		uploadAgentLog();

		if (jobMsg != null) {
			dbclient.setJobStatus(jobMsg.getJobId(), "stopped");
			// uploadJobLog();
		}

	}

	public static void main(String args[]) throws Exception {

		int agentId = -1;
		if (args.length > 0) {
			agentId = Integer.parseInt(args[0]);
		}

		new SamplingAgent().start(agentId);

	}

	private class MonitoringThread extends Thread {

		private SamplingAgent agent;

		public MonitoringThread(SamplingAgent agent) {
			this.agent = agent;
		}

		@Override
		public void run() {

			while (true) {
				logger.debug("MonitoringThread send a signal");

				try {
					int jobId = -1;
					if (jobMsg != null) {
						jobId = jobMsg.getJobId();
					}

					Map<String, Boolean> signal = dbclient.sendAgentSignal(agentId, jobId);
					// boolean quit = false;
					if (signal.get("agentQuit")) {
						try {
							agent.stop();
						} catch (Exception e) {
							logger.error("Failed to stop Agent", e);
						} finally {
							System.exit(1);
						}
					}

					try {
						Thread.sleep(signalPeriod * 60 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					logger.error("MonitoringThread Failure: " + e.getMessage());
					System.exit(1);
				}
			}

		}

	}

}
