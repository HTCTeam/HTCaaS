package org.kisti.htc.scagent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kisti.htc.message.DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SCAgent {

	final Logger logger = LoggerFactory.getLogger(SCAgent.class);

	private int agentId;
	private String host;
	private Date startTimestamp;
	private Date endTimestamp;
	private DTO jobMsg;
	private File workDir;
	private File agentStatusDir;
	private File jobStatusDir;
	private int jobId;
	private File jobLogFile;
	private int waitingTime = 10;  //second

	private static int signalPeriod = 1;

	private SCAgent() {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			signalPeriod = Integer.parseInt(prop.getProperty("SCAgent.Heartbeat.Period"));
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
		}

		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			host = "UnknownHost";
		}

		// set workDir as current directory
		// workDir = new File(System.getProperty("user.dir"));
		agentStatusDir = new File("agent.status");
		agentStatusDir.mkdirs();
		jobStatusDir = new File("job.status");
		jobStatusDir.mkdirs();

		createDirectories();
	}

	private void createDirectories() {
		workDir = new File("workspace");

		// make a new directory "workspace"
		workDir.mkdirs();
	}

	// retrieve a message from File
	public boolean reguestJob() {
		logger.info("+ Retrieving a job message from File");

		jobMsg = new DTO();

		List<String> arguments = new ArrayList<String>();
		List<String> inputFiles = new ArrayList<String>();
		List<String> outputFiles = new ArrayList<String>();

		try {

			FileInputStream is = new FileInputStream(workDir + File.separator + "jobmsg-"+agentId);
			DataInputStream in = new DataInputStream(is);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				String regExpr = "\\[(.+)\\]:?(.+)";
				Pattern pattern = Pattern.compile(regExpr);
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String tag = matcher.group(1).trim();
					String value = matcher.group(2).trim();

					if (tag.equals("MetaJobID"))
						jobMsg.setMetaJobId(Integer.parseInt(value));
					else if (tag.equals("JobID"))
						jobMsg.setJobId(Integer.parseInt(value));
					else if (tag.equals("UserID"))
						jobMsg.setUserId(value);
					else if (tag.equals("AppName"))
						jobMsg.setAppName(value);
					else if (tag.equals("Executable"))
						jobMsg.setExecutable(value);
					else if (tag.equals("Arguments"))
						arguments.add(value);
					else if (tag.equals("InputFiles"))
						inputFiles.add(value);
					else if (tag.equals("OutputFiles"))
						outputFiles.add(value);
					else {
						throw new Exception("Unknown Tag in Job Message: " + tag);
					}
				}
			}
			in.close();

			jobMsg.setArguments(arguments);
			jobMsg.setInputFiles(inputFiles);
			jobMsg.setOutputFiles(outputFiles);

			logger.info("| JobMsg JobID : " + jobMsg.getJobId() + ", AppName: " + jobMsg.getAppName() + ", userId: " + jobMsg.getUserId());

		} catch (Exception e) {
			logger.error("Failed to retrive a job message from File", e);
			return false;
		}

		return true;
	}

	public void validateInputFiles() throws Exception {
		logger.info("+ Validating input files");

		for (String inputFile : jobMsg.getInputFiles()) {
			File file = new File(workDir + File.separator + inputFile);
			if (!file.exists()) {
				logger.error("| " + inputFile + " not exist");
				throw new Exception("Input File Validation Failure");
			} else {
				logger.info("| " + inputFile);
			}
		}
	}

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

	public void execute() throws Exception {
		logger.info("+ Executing application");

		List<String> command = new ArrayList<String>();
		command.add("/bin/sh");
		command.add(workDir.getAbsolutePath() + File.separator + jobMsg.getExecutable());
		for (String arg : jobMsg.getArguments()) {
			command.add(arg);
		}
		logger.debug(command.toString());

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

		logger.info("| [ErrorStream]");
		br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		while ((line = br.readLine()) != null) {
			logger.info("| " + line);
		}

	}

	public void validateOutputFiles() throws Exception {
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

		if (!validated) {
			reportAgentStatus("failed", "Output Validation Failure");
			throw new Exception("Output Validation Failure");
		}

	}

	public void start(int aid) {
		startTimestamp = new Date();

		logger.info("+ Agent started at " + host);

		reportAgentStatus("host", host);

		this.agentId = aid;

		try {
			reportAgentStatus("running", "");

			logger.info("| AgentID : " + agentId);
			logger.info("| Working Directory : " + workDir.getAbsolutePath());

			Thread mThread = new MonitoringThread(this);
			mThread.setDaemon(true);
			mThread.start();

			int failure = 0;

			try {

				while (true) {
					jobMsg = null;
					jobId = -1;
					
					while (true) {
						if (!new File(workDir+File.separator+"quit").exists())
							break;

						logger.info("| Admin wants for this agent to quit..., wating to quit");
						Thread.sleep(1 * 60 * 1000);
					}

					if (!reguestJob()) {
						logger.info("| No jobs in InputQueue,  waitingTime : " + waitingTime + " s");

						
						try {
							Thread.sleep(waitingTime * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						if (waitingTime > 40) {
							logger.info("| Now Agent will be terminated");
							break;
						}

						waitingTime *= 2;
					} else {
						waitingTime = 20;
						try {
							jobId = jobMsg.getJobId();
							reportJobStatus("preparing", "");

							validateInputFiles();

							installApplication();

							Date jobStartTimestamp = new Date();
							reportJobStatus("running", "" + jobStartTimestamp);

							execute();
							validateOutputFiles();

							Date jobEndTimestamp = new Date();
							long runningTime = (jobEndTimestamp.getTime() - jobStartTimestamp.getTime()) / 1000;
							reportJobStatus("done", "" + runningTime);

							new File(workDir.getAbsoluteFile() + File.separator + "jobmsg-"+agentId).delete();
						} catch (Exception e) {
							failure++;
							logger.warn("| Failure #" + failure + ": ", e);
							if (failure >= 1) {
								logger.info("| Now Agent will be terminated");
								throw e;
							}
						} finally {
							logger.info("| Cleaning Workspace JobID : " + jobId);
//							DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath()+"/job.status");
							createJobLog();
							
						}

					}

					// run only one job
//					DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
//					break;

				}
			} catch (InterruptedException e) {
			}

			endTimestamp = new Date();
			long runningTime = (endTimestamp.getTime() - startTimestamp.getTime()) / 1000;
			reportAgentStatus("done", "" + runningTime);

			logger.info("+ Agent successfully finished");
		} catch (Exception e) {
			logger.error("+ Error occurred while agent is running", e);

			reportJobStatus("failed", e.getMessage());

			endTimestamp = new Date();
			long runningTime = (endTimestamp.getTime() - startTimestamp.getTime()) / 1000;
			reportAgentStatus("failed", "" + runningTime);
		} finally {
			createSCAgentLog();
		}

	}

	public void createJobLog() {

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
			// TODO Auto-generated catch block
			logger.error("Job Log File not exit: {}", jobLogFile.getAbsolutePath());
			return;
		} catch (Exception e2) {
			logger.error("Job Log File read/write error: {}", jobLogFile.getAbsolutePath());
		}
	}

	public void createSCAgentLog() {

		logger.info("Creating SCAgentLog : " + agentId);
		File agentLogFile = new File("log/SCAgent.log");
		StringBuilder sb = new StringBuilder();

		try {

			// file read
			String temp;
			BufferedReader br = new BufferedReader(new FileReader(agentLogFile.getAbsolutePath()));
			while ((temp = br.readLine()) != null) {
				sb.append(temp + "\n");
			}
			br.close();

			// file write
			FileWriter fw = new FileWriter("log/" + "SCAgent." + agentId + ".log");
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(sb.toString());
			bw.close();
			fw.close();

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			logger.error("SCAgent Log File not exit: {}", agentLogFile.getAbsolutePath());
			return;
		} catch (Exception e2) {
			logger.error("SCAgent Log File read/write error: {}", agentLogFile.getAbsolutePath());
		}
	}

	private void reportJobStatus(String status, String content) {
		try {
			File file = new File(jobStatusDir, status);
			PrintStream ps = new PrintStream(file);
			ps.println(content);
			ps.close();
		} catch (Exception e) {
			logger.error("Failed to write Job Status: " + e.getMessage());
		}
	}

	private void reportAgentStatus(String status, String content) {
		try {
			File file = new File(agentStatusDir, status);
			PrintStream ps = new PrintStream(file);
			ps.println(content);
			ps.close();
		} catch (Exception e) {
			logger.error("Failed to write Agent Status: " + e.getMessage());
		}
	}
	
	

	public static void main(String args[]) throws Exception {

		int agentId = -1;
		if (args.length > 0) {
			agentId = Integer.parseInt(args[0]);
		}

		new SCAgent().start(agentId);

	}
	
	/**
	 * Stop.
	 */
	public void stop() {
		logger.info("+ Agent stopped by the request");

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		reportAgentStatus("stopped", format.format(new Date()));
		
		
		if(jobMsg != null){
			reportJobStatus("canceled", format.format(new Date()));
		}
		
		
		createSCAgentLog();

	}

	private class MonitoringThread extends Thread {

		private SCAgent scAgent;
		
		public MonitoringThread(SCAgent scAgent) {
			this.scAgent = scAgent;
		}

		@Override
		public void run() {
			while (true) {
				logger.info("MonitoringThread send a signal");

				try {
					int jobId = -1;
					if (jobMsg != null) {
						jobId = jobMsg.getJobId();
					}

					try {
						PrintStream ps = new PrintStream(new File("heartbeat"));
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						ps.println(format.format(new Date()));
						ps.close();
					} catch (Exception e) {
						logger.error("Failed to write Heartbeat: " + e.getMessage());
					}
					
					// boolean quit = false;
					if (new File("quit").exists()) {
						try {
							scAgent.stop();
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
					logger.error("MonitoringThread Failure", e.getMessage());
				}
			}

		}

	}

}
