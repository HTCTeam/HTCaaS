package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.log4j.Logger;
import org.kisti.htc.dbmanager.beans.Constant;
import org.kisti.htc.dbmanager.server.Database;

public class GliteResource extends BackendResource {

	private Logger logger = Logger.getLogger(this.getClass());
	// final static mLogger logger = mLoggerFactory.getLogger("AM");

	// public AgentManager am = AgentManager.getInstance();

	// G-Lite CE Selection Metrics

	private String voName;
	private File proxyFile;
	private File matchJDL;
	// private int wmsSeq = 0;

	public static File gliteDir;
	public static File gangaConfig;

	private List<String> ceList;
	private List<String> wmsListForCurrentCE;

	public boolean needToRepeat = true;

	private static String DBManagerURL;
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;
	private static Database dbClient;

	public static boolean dbFlag = false;

	public GliteResource(String voName) {
		this.type = "glite";
		this.name = voName;
		this.voName = voName;

		gliteDir = new File("conf/AgentManager/glite");
		if (!gliteDir.exists() || !gliteDir.isDirectory()) {
			logger.error("gliteDir not exist");
			System.exit(1);
		}

		gangaConfig = new File(gliteDir, "gangarc.glite." + voName);
		if (!gangaConfig.exists()) {
			logger.error("Ganga config file for GliteResource not exist");
			System.exit(1);
		}

		proxyFile = new File(gliteDir, voName + ".proxy");
		matchJDL = new File(gliteDir, voName + ".match.jdl");

		wmsListForCurrentCE = new ArrayList<String>();

		if (!dbFlag) {
			try {
				Properties prop = new Properties();
				prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

				DBManagerURL = prop.getProperty("DBManager.Address");

				if (prop.getProperty("SSL.Authentication").equals("true")) {
					SSL = true;
					DBManagerURL = DBManagerURL.replace("http", "https");
					SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
					SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
					SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
					SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
				}

				System.out.println("DBManagerURL: " + DBManagerURL);

			} catch (Exception e) {
				System.out.println("Failed to load config file: " + e.getMessage());
				System.exit(1);
			}

			// 2. prepare DBManager client
			logger.info("prepare dbmanager client");
			ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
			factory.setServiceClass(Database.class);
			factory.setAddress(DBManagerURL);
			factory.setDataBinding(new AegisDatabinding());
			dbClient = (Database) factory.create();
		}

	}

	public Database getDBClient() {

		if (dbFlag) {
			return AgentManager.dbClient;
		} else {
			return this.dbClient;
		}

	}

	public int getCEList(int ceMetric) {
		if (ceMetric == AgentManager.freeCPU || ceMetric == AgentManager.roundrobin) {
			ceList = AgentManager.dbClient.getCENameList(voName, true, false);
		} else if (ceMetric == AgentManager.intelligent) {
			// try {
			// updateCEInfo();
			// } catch (Exception e) {
			// logger.error(e);
			// }
			ceList = getDBClient().getIntelligentCEList(voName, 100, 1, 5, 1000); // vo
																					// name,
																					// waiting
																					// time,
																					// numAgentRunning,
																					// numAgentSubmitFailure,
																					// waitingJob
		} else if (ceMetric == AgentManager.priority) {
			ceList = AgentManager.dbClient.getCENameList(voName, true, false);
		}

		logger.info("ceList size:" + ceList.size());

		// wmsSeq++;

		return ceList.size();
	}

	public String getHighFreeCPUCE() {

		int cpu = 0;
		String ceName = null;
		logger.info("ceList size:" + ceList.size());
		for (String ce : ceList) {
			int cpu_temp = getDBClient().getCEFreeCPU(ce);
			if (cpu < cpu_temp) {
				cpu = cpu_temp;
				ceName = ce;
				logger.info("CE FREECPU : " + ceName + " " + cpu);
			}
		}

		return ceName;

	}

	public String getHighPriorityCE() {

		int pri = -9999;
		String ceName = null;
		logger.info("ceList size:" + ceList.size());
		for (String ce : ceList) {
			int pri_temp = getDBClient().getCEPriority(ce);
			int fCPU = getDBClient().getCEFreeCPU(ce);
			if (pri <= pri_temp && fCPU > 0) {
				pri = pri_temp;
				ceName = ce;
				logger.info("ce priority : " + ceName + " " + pri);
			}
		}

		return ceName;
	}

	public String getNextCEName(int ceMetric) {

		String currentCE = null;

		if (ceMetric != AgentManager.roundrobin) {
			ceList = AgentManager.dbClient.getCENameList(voName, true, false);
		} else {
			if (ceList == null || ceList.isEmpty()) {
//				if(AgentManager.selectCount < 5){
					ceList = AgentManager.dbClient.getCENameList(voName, true, false);
//				}
			}
		}

		if (ceList == null || ceList.isEmpty()) {
			return null;
		} else {
			switch (ceMetric) {
			case AgentManager.freeCPU:
				currentCE = getHighFreeCPUCE();
				break;
			case AgentManager.priority:
				currentCE = getHighPriorityCE();
				break;
			case AgentManager.roundrobin:
				currentCE = ceList.get(0);
				getDBClient().increaseCESelectCount(currentCE, 1);
				ceList.remove(0);

				break;
			default:
				currentCE = ceList.get(0);
			}

		}

		return currentCE;
	}

	public String[] getNextWMSandCE(int ceMetric) {

		String[] pair = new String[2];
		String currentCE = null;

		if (ceList == null || ceList.isEmpty()) {
			if (ceMetric == AgentManager.roundrobin || ceMetric == AgentManager.intelligent) {
				getCEList(ceMetric);
			}
		} else {
			currentCE = ceList.get(0);
		}

		// String currentCE = ceList.get(0);
		// currentCE = "darthvader.kisti.re.kr:8443/cream-pbs-vofa";
		// pair[1] = "gridgate.cs.tcd.ie:2119/jobmanager-pbs-thirtym";
		// pair[0] = "https://wms.eela.ufrj.br:7443/glite_wms_wmproxy_server";

		// return pair;

		if (ceList.isEmpty()) {
			// No MORE CE
			if (needToRepeat) {
				logger.error("Repeat CEList");
				try {
					updateCEInfo();
				} catch (Exception e) {
					logger.error(e);
				}
				getCEList(ceMetric);
				// wmsListForCurrentCE = new ArrayList<String>();
				wmsListForCurrentCE.clear();
				getDBClient().initCESubmitCount(voName);
				return getNextWMSandCE(ceMetric);
			} else {
				return null;
			}
		}

		boolean success = getDBClient().increaseCESubmitCount(currentCE, 1);
		if (success) {

			// For CREAM CE
			if (currentCE.contains("cream")) {
				// wmsListForCurrentCE = new ArrayList<String>();
				wmsListForCurrentCE.clear();
				pair[0] = currentCE;
				pair[1] = "CREAM";

				if (ceMetric == AgentManager.roundrobin || ceMetric == AgentManager.intelligent) {
					ceList.remove(0);
					wmsListForCurrentCE.clear();
				}

				return pair;
			}

			// For Normal CE

			if (wmsListForCurrentCE.isEmpty()) {
				// Repeat WMSList
				wmsListForCurrentCE = getDBClient().getAvailableWMSListForCE(currentCE);
			}

			if (wmsListForCurrentCE == null) {
				logger.error("Unknown CE: " + currentCE);
				// remove this one and move to the next one
				ceList.remove(0);
				// wmsListForCurrentCE = new ArrayList<String>();
				wmsListForCurrentCE.clear();
				return getNextWMSandCE(ceMetric);
			}

			if (wmsListForCurrentCE.get(0).equals("EMPTY")) {
				logger.error("CE has no available WMS");
				// remove this one and move to the next one
				ceList.remove(0);
				// wmsListForCurrentCE = new ArrayList<String>();
				wmsListForCurrentCE.clear();
				return getNextWMSandCE(ceMetric);
			}

			logger.info("wmsList size:" + wmsListForCurrentCE.size());
			if (wmsListForCurrentCE.isEmpty()) {
				// CE has no WMS
				// remove this one and move to the next one
				ceList.remove(0);
				// wmsListForCurrentCE = new ArrayList<String>();
				wmsListForCurrentCE.clear();
				return getNextWMSandCE(ceMetric);
			}

			// pair[0] = wmsListForCurrentCE.remove(0);
			// pair[1] = currentCE;

			if (ceMetric == AgentManager.roundrobin || ceMetric == AgentManager.intelligent) {
				pair[0] = currentCE;
				pair[1] = wmsListForCurrentCE.remove(0);

				ceList.remove(0);
				wmsListForCurrentCE.clear();

			} else if (ceMetric == AgentManager.freeCPU) {
				pair[0] = currentCE;
				pair[1] = wmsListForCurrentCE.remove(0);

			}

			return pair;
		} else {
			// CE is full
			// Remove this one and move to the next one
			ceList.remove(0);
			// wmsListForCurrentCE = new ArrayList<String>();
			wmsListForCurrentCE.clear();
			// wmsSeq = 0;
			return getNextWMSandCE(ceMetric);
		}

	}

	private void createMatchJDL() throws Exception {
		logger.info("Creating MatchJDL file");
		PrintStream ps = new PrintStream(new FileOutputStream(matchJDL, false));
		ps.println("Type = \"Job\";\n" + "VirtualOrganisation = \"" + voName + "\";\n" + "JobType = \"Normal\";\n" + "Executable = \"/bin/sh\";\n" + "RetryCount = 0;\n");
	}

	public void initVomsProxy() throws Exception {
		logger.info("+ Initializing Voms-Proxy");

		try {
			String command = "voms-proxy-init -cert " + gliteDir.getPath() + "/usercert.pem -key " + gliteDir.getPath() + "/userkey.pem " + "-out " + proxyFile.getPath() + " -voms " + voName + " -pwstdin";

			logger.info(command);

			Process p = Runtime.getRuntime().exec(command);

			// input redirection
			PrintStream fout = new PrintStream(p.getOutputStream());
			BufferedReader fr = new BufferedReader(new FileReader(gliteDir.getPath() + "/.gridproxy"));
			String line;
			while ((line = fr.readLine()) != null) {
				fout.println(line);
			}
			fout.flush();

			int exitValue = p.waitFor();
			if (exitValue == 0) {
				logger.info("| Success");
			} else {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				throw new Exception(sb.toString());
			}
		} catch (Exception e) {
			logger.error("Failed to initialize Voms-Proxy", e);
			throw new Exception(e);
		}
	}

	public int getVomsProxyTimeLeft() throws Exception {
		logger.info("+ Checking TimeLeft of Voms-Proxy");

		int timeLeft = 0;

		if (!proxyFile.exists()) {
			logger.info("| Proxy not exist");
			return 0;
		}

		try {
			List<String> command = new ArrayList<String>();
			command.add("voms-proxy-info");
			command.add("-file");
			command.add(proxyFile.getPath());
			command.add("-timeleft");

			ProcessBuilder builder = new ProcessBuilder(command);
			Process p = builder.start();

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = br.readLine();
			timeLeft = Integer.parseInt(line);
			logger.info("| TimeLeft: " + timeLeft);
			br.close();

			StringBuffer sb = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			if (sb.toString().contains("Couldn't find a valid proxy")) {
				return 0;
			}
			// if (timeLeft == 0) {
			// throw new Exception(sb.toString());
			// }
		} catch (Exception e) {
			logger.error("Failed to check TimeLeft of Voms-Proxy", e);
			throw new Exception(e);
		}

		return timeLeft;
	}

	@Override
	public void updateCEInfo() throws Exception {
		logger.info("+ Updating CE CPU Info using 'lcg-infosites' " + voName);

		List<String> ceList = new ArrayList<String>();

		try {
			List<String> command = new ArrayList<String>();
			command.add("lcg-infosites");
			command.add("--vo");
			command.add(voName);
			command.add("ce");

			ProcessBuilder builder = new ProcessBuilder(command);
			Process p = builder.start();

			int exitValue = p.waitFor();
			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					logger.debug(line);
					Pattern pattern = Pattern.compile("(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(.*)");
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						String totalCPU = matcher.group(1);
						String freeCPU = matcher.group(2);
						String runningJob = matcher.group(4);
						String waitingJob = matcher.group(5);
						String ceName = matcher.group(6);

						logger.debug("| totalCPU:" + totalCPU + " freeCPU:" + freeCPU + " name:" + name);

						ceList.add(ceName + " " + totalCPU + " " + freeCPU + " " + runningJob + " " + waitingJob);
					}

				}
			} else {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				throw new Exception(sb.toString());
			}

		} catch (Exception e) {
			logger.error("Failed to retrieve CE list", e);
			throw new Exception(e);
		}

		boolean ret = getDBClient().updateCEInfo(voName, ceList);

		if (ret) {
			logger.info("| VO: " + voName + " " + ceList.size() + " CEs updated to Database");
		} else {
			logger.info("| VO: " + voName + " " + ceList.size() + " CEs didn't update to Database. Not registed CEs(or WMS and CE are not mached!)");
		}
	}

	private void createWMSList() throws Exception {
		logger.info("+ Retrieving WMS List using 'lcg-infosites'");
		List<String> list = new ArrayList<String>();

		try {
			List<String> command = new ArrayList<String>();
			command.add("lcg-infosites");
			command.add("--vo");
			command.add(voName);
			command.add("wms");

			ProcessBuilder builder = new ProcessBuilder(command);
			Process p = builder.start();

			int exitValue = p.waitFor();
			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("https://")) {
						String wmsName = line.trim();
						logger.debug("| " + wmsName);
						list.add(wmsName);
					}
				}
			} else {
				StringBuffer sb = new StringBuffer();
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				throw new Exception(sb.toString());
			}

		} catch (Exception e) {
			logger.error("Failed to retrieve WMS list", e);
			throw new Exception(e);
		}

		getDBClient().updateWMSList(voName, list);

		logger.info("| VO: " + voName + " " + list.size() + " WMSes founded and updated to Database");
	}

	private void checkWMSes() {
		logger.info("+ Checking WMS Status");

		getDBClient().setAllCEsUnavailable(voName);

		MatchWorkQueue matchQueue = new MatchWorkQueue(this, "matchQueue", 5);

		List<String> wmsList = getDBClient().getAvailableWMSList(voName);

		logger.info("| " + wmsList.size() + " WMSes are available");
		for (String wmsName : wmsList) {
			matchQueue.addJob(wmsName);
		}

		while (!matchQueue.doNothing()) {
			logger.info("| Still checking WMS status [" + matchQueue.currentInfo() + "]");
			try {
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
			}
		}

		matchQueue.end();

		logger.info(" | Checking WMS Status Done");
	}

	public File getProxyFile() {
		return proxyFile;
	}

	public void setProxyFile(File proxyFile) {
		this.proxyFile = proxyFile;
	}

	public File getMatchJDL() {
		return matchJDL;
	}

	public void setMatchJDL(File matchJDL) {
		this.matchJDL = matchJDL;
	}

	public String getVoName() {
		return voName;
	}

	public void setVoName(String voName) {
		this.voName = voName;
	}

	public void cancelZombieJob() {
		try {
			Integer si = getDBClient().getServiceInfraId(voName);

			List<Integer> list = getDBClient().getAgentSubmittedZombieList(si);
			List<String> command = new ArrayList<String>();

			for (Integer aid : list) {
				logger.error("Submitted Zombie Agent ID :  " + aid);

				String submitId = getDBClient().getAgentSubmitId(aid);

				command.add("glite-ce-job-purge");
				command.add("-N");
				command.add("" + submitId);

				ProcessBuilder builder = new ProcessBuilder(command);
				Map<String, String> envs = builder.environment();
				envs.put("X509_USER_PROXY", getProxyFile().getAbsolutePath());

				builder.directory();

				Process process = builder.start();

				int exitValue = process.waitFor();

				if (exitValue == 0) {
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = br.readLine();
					while ((line = br.readLine()) != null) {
						logger.info("| " + line);
					}
					logger.info("| Successfully canceled.");
					br.close();
					getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
				} else {
					logger.info("Exit Value: " + exitValue);
					logger.info("| [ErrorStream]");
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line;
					while ((line = br.readLine()) != null) {
						logger.info("| " + line);
						if (line.contains("had a status incompatible for operation")) {
							getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
							break;
						}else if(line.contains("This job has not been found on the CREAM server")){
							getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
							break;
						}
					}
					br.close();
				}
			}

		} catch (Exception e) {
			logger.error("Failed to cancel zombie-agent", e);
		}
	}

	public void purgeJobs(String status, boolean flag) {
		try {
			List<Integer> siList = new ArrayList<Integer>();
			siList.add(getDBClient().getServiceInfraId(AgentManager.BIOMED));
			siList.add(getDBClient().getServiceInfraId(AgentManager.VOFA));

			for (Integer si : siList) {
				List<Integer> list = getDBClient().getAgentListFromStatus(si, status, flag);
				List<String> command = new ArrayList<String>();

				for (Integer aid : list) {
					logger.error("Submitted Zombie Agent ID :  " + aid);

					String submitId = getDBClient().getAgentSubmitId(aid);

					command.add("glite-ce-job-purge");
					command.add("-N");
					command.add("" + submitId);

					ProcessBuilder builder = new ProcessBuilder(command);
					Map<String, String> envs = builder.environment();
					envs.put("X509_USER_PROXY", getProxyFile().getAbsolutePath());

					builder.directory();

					Process process = builder.start();
					int exitValue = process.waitFor();

					if (exitValue == 0) {
						BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
						String line = br.readLine();
						while ((line = br.readLine()) != null) {
							logger.info("| " + line);
						}
						logger.info("| Successfully canceled.");
						br.close();
						getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
					} else {
						logger.info("Exit Value: " + exitValue);
						logger.info("| [ErrorStream]");
						BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
						String line;
						while ((line = br.readLine()) != null) {
							logger.info("| " + line);
							if (line.contains("had a status incompatible for operation")) {
								getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
								break;
							}else if(line.contains("This job has not been found on the CREAM server")){
								getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
								break;
							}
						}
						br.close();
					}
					process.destroy();
					command.clear();
				}
			}

		} catch (Exception e) {
			logger.error("Failed to cancel zombie-agent", e);
		}
	}

	public static void main(String[] args) throws Exception {

		GliteResource gr = new GliteResource("vo.france-asia.org");
		// GliteResource gr = new GliteResource("biomed");

		// GliteResource gr = new GliteResource(args[0]);

		if (gr.getVomsProxyTimeLeft() < 3600 * 2) {
			gr.initVomsProxy();
		}

		gr.purgeJobs(Constant.AGENT_STATUS_SUB, false);
		gr.purgeJobs(Constant.AGENT_STATUS_RUNZOM, true);
		// gr.createWMSList();
		// gr.createMatchJDL();
		// gr.checkWMSes();
		// gr.updateCEInfo();
		// gr.cancelZombieJob();
	}

}
