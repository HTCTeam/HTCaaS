package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kisti.htc.dbmanager.server.Database;

import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class LocalMachine extends BackendResource {

	private Logger logger = Logger.getLogger(this.getClass());
	// final static mLogger logger = mLoggerFactory.getLogger("AM");

	public AgentManager am = AgentManager.getInstance();

	private File proxyFile;
	public static File localDir;
	public static File gangaConfig;
	private List<String> ceList;
	
	public static void main(String args[]) {

		LocalMachine lm = new LocalMachine("local");
		lm.updateCEInfo();
	}

	// constructor
	public LocalMachine(String name) {

		// set_logger_prefix("[" + this.getClass().getSimpleName() +"] ");

		this.type = "local";
		this.name = name;

		localDir = new File("conf/AgentManager/local");
		if (!localDir.exists() || !localDir.isDirectory()) {
			logger.error("localDir not exist");
			System.exit(1);
		}

		gangaConfig = new File(localDir, "gangarc.local");
		if (!gangaConfig.exists()) {
			logger.error("Ganga config file for LocalMachine not exist");
			System.exit(1);
		}

		proxyFile = new File(localDir, "local.proxy");
	}

	public String getNextCEName(int ceMetric) {

		String currentCE = null;

		ceList = AgentManager.dbClient.getCENameList(name, true, false);

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
			default:
				currentCE = ceList.get(0);
			}

			// if(getDBClient().increaseCESubmitCount(currentCE, 1)){
			// return currentCE;
			// }else{
			// return null;
			// }
		}

		return currentCE;
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
	
	@Override
	public void updateCEInfo() {

		logger.info("+ Updating Local Resource");
		String totalCPU = "0";
		String cluster = "local";
		int idleCPU = 0;

		try {
			String command = "/bin/grep -c processor /proc/cpuinfo";
			logger.debug(command);

			ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);
			Process p = builder.start();

			int exitValue = p.waitFor();

			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				totalCPU = br.readLine();
				logger.info("| Total CPU: " + totalCPU);
				br.close();
			} else {
				StringBuffer sb = new StringBuffer();
				BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				while ((totalCPU = bre.readLine()) != null) {
					sb.append(totalCPU + "\n");
				}
				bre.close();
			}

		} catch (Exception e) {
			logger.error("Failed to check total cpu", e);
		}

		List<String> list = new ArrayList<String>();

		String errorMsg = "";
		String command = "/usr/bin/mpstat -P ALL |sed '1,4d'|awk '{print $3, $12}'";
		long timeout = 5;
		try {

			logger.debug(command);
			ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", command);

			Process p = builder.start();
			int exitValue = p.waitFor();

			// exitValue = 1;
			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while (true) {
					if (ready(br, timeout)) {
						line = br.readLine();

						Pattern pattern = Pattern.compile("(\\d+)\\s+(\\d+\\.?\\d*)");
						Matcher matcher = pattern.matcher(line);
						
						if (matcher.find()) {
							String cpu = matcher.group(1);
							String idle = matcher.group(2);

							logger.debug("| CPU:" + cpu + " Idle:" + idle);
							if ((Float.parseFloat(idle)) > 50) {
								idleCPU++;
							}

						}
					} else {
						logger.info("IDLE CPU : " + idleCPU );
						break;
					}
				}
				

				list.add(cluster + " " + totalCPU + " " + idleCPU + " " + 0 + " " + 0);

				br.close();

			} else {
				StringBuffer sb = new StringBuffer();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");

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

				errorMsg = sb.toString();

				new Exception(errorMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to get ce info", e);
		}

		// update SCCE info
		AgentManager.dbClient.updateSCCEInfo("local", list);

		logger.info("| ServiceInfra: " + cluster + " " + list.size() + " CEs updated to Database");

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

	@Override
	public int getCEList(int ceMetric) {
		  if (ceMetric == AgentManager.freeCPU || ceMetric == AgentManager.roundrobin) {

		      logger.info(" getCEList  voName : " + name);
		      ceList = AgentManager.dbClient.getCENameList(name, true, false);

		    } else if (ceMetric == AgentManager.intelligent) {
		    }

		    logger.info("ceList size:" + ceList.size());

		    return ceList.size();
	}

	// / g-Lite {{{
	public void initVomsProxy() throws Exception {
		logger.info("+ Initializing Voms-Proxy");

		try {
			String command = "voms-proxy-init -cert " + localDir.getPath() + "/usercert.pem" + " -key " + localDir.getPath() + "/userkey.pem" + " -out " + proxyFile.getPath() + " -pwstdin";

			Process p = Runtime.getRuntime().exec(command);

			// input redirection
			PrintStream fout = new PrintStream(p.getOutputStream());
			BufferedReader fr = new BufferedReader(new FileReader(localDir.getPath() + "/.gridproxy"));
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
		logger.info("+ getVomsProxyTimeLeft() - check time left of voms proxy");

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

			logger.info(command);

			ProcessBuilder builder = new ProcessBuilder(command);
			Process p = builder.start();

			int exitValue = p.waitFor();

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
		} catch (Exception e) {
			logger.error("Failed to check TimeLeft of Voms-Proxy", e);
			// throw new Exception(e);
		}

		return timeLeft;
	}

	public File getProxyFile() {
		return proxyFile;
	}

	public void setProxyFile(File proxyFile) {
		this.proxyFile = proxyFile;
	}

	// / g-Lite }}}

	public Database getDBClient() {
		return AgentManager.dbClient;
	}

	public void cancelZombieJob() {

		// not implemented
	}
}
