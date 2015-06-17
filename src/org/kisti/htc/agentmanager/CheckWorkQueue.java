package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.UUID;

import org.kisti.htc.message.DTO;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class CheckWorkQueue extends WorkQueue {

	final Logger logger = LoggerFactory.getLogger(CheckWorkQueue.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AM");

	private AgentManager as;
	private UDClient udc; // UserDataManager client

	private String ftpAddress;
	private int port = 22;

	private String adminUser = "htcaas";
	private String adminPasswd = "htcaas";

	private SshClient sc = new SshClient();

	// constructor
	public CheckWorkQueue(AgentManager as, String queueName, int nThreads) {

		super(queueName, nThreads);

		this.as = as;

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(AgentManager.configPath));

			ftpAddress = prop.getProperty("FTP.Address");

		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}

		// logger = Logger.getLogger(CheckWorkQueue.class);
		startWorkers();

		// UDManager client (CXF)
		udc = new UDClient();
	}

	// 호스트명을 구함
	private boolean checkHost(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (info.isHostNull()) {
				// /prowk01/사용자/agnetWorkspace##/scagent/agent.status/host
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/agent.status/host", ss, true);
				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.debug(ret.getStdOutput());
					String hostname = ret.getStdOutput();
					AgentManager.dbClient.setAgentHost(aid, hostname);
					info.setHost(hostname);
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		}

		return true;
	}

	// 에이전트가 preparing 상태인지 체크
	private boolean checkJobPreparing(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		int jid = info.getJobMsg().getJobId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isJob_preparing()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/job.status/preparing", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Job " + jid + " preparing " + ret.getStdOutput());
					info.setJob_preparing(true);
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			setJobLog(userId, jid, aid);
			return false;
		}

		return true;
	}

	// 에이전트가 running 상태인지 체크
	private boolean checkJobRunning(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		int jid = info.getJobMsg().getJobId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isJob_running()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/job.status/running", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Job " + jid + " running " + ret.getStdOutput());
					info.setJob_running(true);

					AgentManager.dbClient.startJobKSC(jid);
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			setJobLog(userId, jid, aid);
			return false;
		}

		return true;
	}

	// 작업이 완료되었는지 체크
	private boolean checkJobDone(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		int jid = info.getJobMsg().getJobId();
		String userId = info.getJobMsg().getUserId();
		int mid = info.getJobMsg().getMetaJobId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isJob_done()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/job.status/done", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Job " + jid + " done " + ret.getStdOutput());
					info.setJob_done(true);

					long runningTime = Long.parseLong(ret.getStdOutput().trim());
					AgentManager.dbClient.finishJobKSC(aid, jid, runningTime);

					// retrieve results
					for (String outputFile : info.getJobMsg().getOutputFiles()) {
						sc.ScpFrom("/pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/workspace/" + new File(outputFile).getName(),
								as.workDir.getAbsolutePath(), ss, true);
					}

					logger.info("+ Validating output files");
					UUID uuid = udc.udclient.login(ftpAddress, userId, AgentManager.dbClient.getUserPasswd(userId), aid);
					try {

						boolean validated = true;

						for (String outputFile : info.getJobMsg().getOutputFiles()) {
							logger.debug(outputFile);

							File file = new File(as.workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());
							if (!file.exists()) {
								logger.error("| Output file not exist: " + outputFile);
								validated = false;
							}
						}

						if (!validated) {
							AgentManager.dbClient.setJobStatus(jid, "Output Validation Failure");
							logger.info("| Output Validation Failure");
						} else {
							// upload results
							for (String outputFile : info.getJobMsg().getOutputFiles()) {
								File file = new File(as.workDir.getAbsolutePath() + File.separator + new File(outputFile).getName());
								udc.putFile(uuid, file.getAbsolutePath(), new File(outputFile).getParent(), aid);
								AgentManager.dbClient.addResult(jid, mid, outputFile);
								file.delete();
							}
						}

					} finally {
						udc.udclient.logout(uuid, aid);
						uploadJobLog(userId, aid, jid);
						setJobLog(userId, jid, aid);
					}
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		}
		
		return true;
	}

	// 새로운 작업을 요청
	private boolean requestNewJob(AgentMonitoringInfo info, int agentId) {
		
		logger.info("Preparing to get a new job...");

		String userId = info.getJobMsg().getUserId();
		DeleteFileAndDirUtil.deleteFilesAndDirs("/pwork01/" + userId + LLJob.AGENT_WORKSPACE + agentId + "/scagent/job.status/");

		info.setJob_preparing(false);
		info.setJob_running(false);
		info.setJob_done(false);
		
		boolean result = true;
		
		DTO dto = LLJob.requestSubJob(userId);

		if(dto == null){
			return false;
		}
		
		AgentManager.dbClient.setAgentCurrentJob(agentId, dto.getJobId(), dto.getMetaJobId());

		int port = 22;
		SshClient sc = new SshClient();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);

			for (String inputFile : dto.getInputFiles()) {
				sc.ScpTo(new File(inputFile).getAbsolutePath(), "/pwork01/" + userId + LLJob.AGENT_WORKSPACE + agentId + "/scagent/workspace", ss,
						false);
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
				return false;
			}

			// jobmsg 파일을 scp 복사한다.
			logger.info("Copying jobmsg :" + agentId);
			sc.ScpTo("tmp/jobmsg-" + agentId, "/pwork01/" + userId + LLJob.AGENT_WORKSPACE + agentId + "/scagent/workspace", ss, false);

		} catch (Exception e) {
			logger.error("Failed to scp inputfile or jobmsg: " + e.getMessage());
			return false;
		}
		AgentManager.dbClient.setAgentPushed(agentId);

		AgentManager.checkQueue.addJob(new AgentMonitoringInfo(agentId, dto));

		AgentManager.dbClient.setJobStatus(dto.getJobId(), "preparing");

		return result;
	}

	// 작업이 실패했는지 체크함
	private boolean checkJobFailed(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		int jid = info.getJobMsg().getJobId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isJob_failed()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/job.status/failed", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Job " + jid + " failed " + ret.getStdOutput());
					info.setJob_failed(true);

					String errMsg = ret.getStdOutput();

					AgentManager.dbClient.setJobStatus(jid, "failed");
					AgentManager.dbClient.setJobErrormsg(jid, errMsg);
					setJobLog(userId, jid, aid);
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		} finally {
			uploadJobLog(userId, aid, jid);
			setJobLog(userId, jid, aid);
		}
		
		
		return true;
	}
	
	// 작업이 취소 되었는지 체크함
	private boolean checkJobCanceled(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		int jid = info.getJobMsg().getJobId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isJob_failed()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/job.status/canceled", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Job " + jid + " failed " + ret.getStdOutput());
					info.setJob_canceled(true);

					AgentManager.dbClient.setJobStatus(jid, "canceled");
					setJobLog(userId, jid, aid);
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			uploadJobLog(userId, aid, jid);
			setJobLog(userId, jid, aid);
			return false;
		}
		
		
		return true;
	}
	

	// 
	private boolean checkAgentRunning(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isAgent_running()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/agent.status/running", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Agent " + aid + " running " + ret.getStdOutput());
					info.setAgent_running(true);

					AgentManager.dbClient.startAgent(aid);
					
					AgentManager.dbClient.setCEAliveAgentAdd(aid, 1);
					int runningAgent = AgentManager.dbClient.getNumUserAgentStatus(userId, "running");
					int keepAgent = AgentManager.dbClient.getUserKeepAgentNO(userId);
							
					if ( runningAgent > keepAgent ) {
						logger.info("RunningAgent # is " + runningAgent);
						logger.info("UserKeepAgent# is " + keepAgent);
						logger.info("Agent # is over. Done by UserKeepAgentNO.");
						
						Session ss2 = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
						sc.Exec("touch /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/quit", ss2, true);
					}else if(AgentManager.dbClient.checkAgentQuit(aid)){
						Session ss2 = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
						sc.Exec("touch /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/quit", ss2, true);
					}
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			uploadSCAgentLog(userId, aid, info.getJobMsg().getJobId());
			return false;
		}
		
		

		return true;
	}

	private boolean checkAgentDone(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isAgent_done()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/agent.status/done", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Agent " + aid + " done " + ret.getStdOutput());
					info.setAgent_done(true);

					long runningTime = Long.parseLong(ret.getStdOutput().trim());
					AgentManager.dbClient.finishKSCAgent(aid, runningTime);
					uploadSCAgentLog(userId, aid, info.getJobMsg().getJobId());
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		} finally {
			AgentManager.dbClient.setCEAliveAgentAdd(aid, -1);
		}

		return true;
	}

	private boolean checkAgentFailed(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isAgent_failed()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/agent.status/failed", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Agent " + aid + " failed " + ret.getStdOutput());
					info.setAgent_failed(true);

					long runningTime = Long.parseLong(ret.getStdOutput().trim());
					AgentManager.dbClient.reportKSCAgentFailure(aid, runningTime);
					uploadSCAgentLog(userId, aid, info.getJobMsg().getJobId());
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		} finally {
			AgentManager.dbClient.setCEAliveAgentAdd(aid, -1);
		}

		return true;
	}
	
	private boolean checkAgentStopped(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isAgent_stopped()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/agent.status/stopped", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Agent " + aid + " stopped " + ret.getStdOutput());
					info.setAgent_stopped(true);

					uploadSCAgentLog(userId, aid, info.getJobMsg().getJobId());
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		} finally {
			AgentManager.dbClient.setCEAliveAgentAdd(aid, -1);
		}

		return true;
	}

	private boolean checkHeartbeat(AgentMonitoringInfo info) {
		int aid = info.getAgentId();
		String userId = info.getJobMsg().getUserId();

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, userId, AgentManager.dbClient.getUserPasswd(userId), port);
			if (!info.isAgent_failed()) {
				SshExecReturn ret = sc.Exec("cat /pwork01/" + userId + LLJob.AGENT_WORKSPACE + aid + "/scagent/heartbeat", ss, true);
				logger.debug("ExitValue: " + ret.getExitValue());

				if (ret.getExitValue() != 0) {
					logger.error(ret.getStdError());
					return false;
				} else {
					logger.info("Agent " + aid + " ,Heartbeat " + ret.getStdOutput());

					AgentManager.dbClient.setAgentLastSignal(aid, ret.getStdOutput());
				}
			}
		} catch (Exception e) {
			logger.error("| " + e.getMessage());
			return false;
		} finally {
		}

		return true;
	}

	private void setJobLog(String userId, int jid, int aid) {
		// read job log and upload it in db
		StringBuilder sb = null;
		try {
			File logFile = new File(as.workDir.getAbsoluteFile() + File.separator + "job." + jid + ".log");
			BufferedReader br = new BufferedReader(new FileReader(logFile.getAbsolutePath()));
			String temp;
			sb = new StringBuilder();
			while ((temp = br.readLine()) != null) {
				sb.append(temp + "\n");
			}
			br.close();

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			logger.error("Job log File not found: {}", e1.getMessage());
		} catch (Exception e2) {
			logger.error("| " + e2.getMessage());
		}
		AgentManager.dbClient.setJobLog(jid, sb.toString()); // set job log in
																// db
	}

	private void uploadJobLog(String uid, int aid, int jid) {
		logger.info("Creating and Uploading JobLog : " + aid);

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, uid, AgentManager.dbClient.getUserPasswd(uid), port);
			sc.ScpFrom("/pwork01/" + uid + LLJob.AGENT_WORKSPACE + aid + "/scagent/log/job." + jid + ".log", as.workDir.getAbsolutePath(), ss, true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("failed to copying job log in workspace", e1);
		}

		File file = new File(as.workDir.getAbsoluteFile() + File.separator + "job." + jid + ".log");
		String logFileName = "job." + jid + ".log";

		if (file.exists()) {
			UUID uuid = null;
			try {
				uuid = udc.udclient.login(ftpAddress, adminUser, AgentManager.dbClient.getUserPasswd(adminUser), aid);
				boolean result = udc.putFile(uuid, file.getAbsolutePath(), "/home/" + adminUser + "/filelog/job", logFileName, aid);
				logger.info("| " + logFileName + "(" + result + ")");
				int i = 1;
				while (!result) {
					logger.info("| " + logFileName + "(" + i + " retransmitted)");
					result = udc.putFile(uuid, file.getAbsolutePath(), "/home/" + adminUser + "/filelog/job", logFileName, aid);
					if (i == 3) {
						logger.error("| " + logFileName + "(failed)");
						udc.udclient.logout(uuid, aid);
						return;
					}
					i++;
				}
			} catch (SocketTimeoutException e1) {
				logger.error("Failed to upload job log file", e1);
				return ;

			} catch (Exception e) {
				logger.error("Failed to upload job log file", e);
			} finally {
				udc.udclient.logout(uuid, aid);
			}
		} else {
			logger.error("Job Log file not exist: " + file.getAbsolutePath());
		}
	}

	private void uploadSCAgentLog(String uid, int aid, int jid) {
		logger.info("Creating and Uploading SCAgentLog : " + aid);

		try {
			Session ss = sc.getSession(LLResource.PLSILOGINNODE, uid, AgentManager.dbClient.getUserPasswd(uid), port);
			sc.ScpFrom("/pwork01/" + uid + LLJob.AGENT_WORKSPACE + aid + "/scagent/log/SCAgent." + aid + ".log", as.workDir.getAbsolutePath(), ss,
					true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			logger.error("failed to copying scagent log in workspace", e1);
		}

		File file = new File(as.workDir.getAbsoluteFile() + File.separator + "SCAgent." + aid + ".log");
		String logFileName = "SCAgent." + aid + ".log";

		if (file.exists()) {
			UUID uuid = null; 
			try {
				uuid = udc.udclient.login(ftpAddress, adminUser, AgentManager.dbClient.getUserPasswd(adminUser), aid);
				boolean result = udc.putFile(uuid, file.getAbsolutePath(), "/home/" + adminUser + "/filelog/agent", logFileName, aid);
				logger.info("| " + logFileName + "(" + result + ")");
				int i = 1;
				while (!result) {
					logger.info("| " + logFileName + "(" + i + " retransmitted)");
					result = udc.putFile(uuid, file.getAbsolutePath(), "/home/" + adminUser + "/filelog/agent", logFileName, aid);
					if (i == 3) {
						logger.error("| " + logFileName + "(failed)");
						udc.udclient.logout(uuid, aid);
						return;
					}
					i++;
				}
			} catch (SocketTimeoutException e1) {
				logger.error("Failed to upload agent log file", e1);
				return ;
			} catch (Exception e) {
				logger.error("Failed to upload agent log file", e);
			} finally {
				udc.udclient.logout(uuid, aid);
			}
		} else {
			logger.error("SCAgent Log file not exist: " + file.getAbsolutePath());
		}
	}

	@Override
	public void doWork(String workerName, Object object) {

		AgentMonitoringInfo info = (AgentMonitoringInfo) object;
		int aid = info.getAgentId();
		int jid = info.getJobMsg().getJobId();

		boolean finished = false;

		logger.info("| " + queueName + " Thread_" + workerName + " Checking Agent: " + aid + ", Job: " + jid);
		try {

			if (checkHost(info)) {
				if (checkJobPreparing(info)) {
					if (checkJobRunning(info)) {
						if (checkJobDone(info)) {
							requestNewJob(info, aid);
//							finished = true;
						} else if (checkJobFailed(info)) {
							requestNewJob(info, aid);
//							finished = true;
						} else if (checkJobCanceled(info)){
							requestNewJob(info, aid);
//							finished = true;
						}
					}
				}

				if (checkAgentRunning(info)) {
					if (checkAgentDone(info)) {
						finished = true;
					} else if (checkAgentFailed(info)) {
						finished = true;
					} else if (checkAgentStopped(info)){
						finished = true;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		checkHeartbeat(info);

		if (!finished) {
			try {
				Thread.sleep(10 * 1000); // 10 seconds
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("| Enqueue again! Agent:" + aid + ", Job: " + jid);

			this.addJob(info);
		} else {
			if (checkHost(info)) {
				if (checkJobPreparing(info)) {
					if (checkJobRunning(info)) {
						if (checkJobDone(info)) {
						} else if (checkJobFailed(info)) {
							// //failed Job re-enqueue
						}
					}
				}
			}

			logger.info("| Finished Agent: " + aid + ", Job: " + jid);
		}
	}
	
	public static void main(String arg[]){
		
		CheckWorkQueue aa = new CheckWorkQueue(AgentManager.getInstance(), "test", 1);
		
	}

}
