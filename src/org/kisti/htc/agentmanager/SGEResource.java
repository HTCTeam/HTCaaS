package org.kisti.htc.agentmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kisti.htc.dbmanager.server.Database;

import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class SGEResource extends BackendResource {

	private Logger logger = Logger.getLogger(this.getClass());
	// public AgentManager am = AgentManager.getInstance();
	// public static File clusterDir;

	// default setting
	static String CLUSTERNAME = "nodemaster01.sookmyung.ac.kr";
	static String CLUSTERID = "root";
	static String CLUSTERPASSWD = "tnrauddueo()()%$%$";
	static int CLUSTERPORT = 22;
	static String CLUSTERQUEUE = "all.q";

	private List<String> ceList;

	// constructor
	public SGEResource(String voName) {

		logger.info("Cluster Resource initialization");
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(AgentManager.configPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CLUSTERNAME = prop.getProperty("CLUSTER_NAME");
		CLUSTERID = prop.getProperty("CLUSTER_ID");
		CLUSTERPASSWD = prop.getProperty("CLUSTER_PASSWD");
		CLUSTERPORT = Integer.parseInt(prop.getProperty("CLUSTER_PORT"));
		CLUSTERQUEUE = prop.getProperty("CLUSTER_QUEUE");

		this.type = "cluster"; // delete or not
		this.name = voName;

		// clusterDir = new File("conf/AgentManager/cluster");
		// if (!clusterDir.exists() || !clusterDir.isDirectory()) {
		// logger.error("clusterDir not exist");
		// System.exit(1);
		// }

	} // end of constructor

	@Override
	public void updateCEInfo() throws Exception {

	    logger.info("+ Updating SGE Resource Info using 'ssh'");

	    List<String> ceList = new ArrayList<String>();

	    SshExecReturn result1 = null;
		
		SshClient sc = new SshClient();

		try {
			Session ss = sc.getSession(SGEResource.CLUSTERNAME, SGEResource.CLUSTERID, SGEResource.CLUSTERPASSWD, SGEResource.CLUSTERPORT);

			
			result1 = sc.Exec("qstat -g c", ss, true);
			if (result1.getExitValue() == 0) {
		
				if (!result1.getStdOutput().isEmpty() && result1.getStdError().isEmpty()) {
						String out = result1.getStdOutput();
						
						logger.debug(out);
						
						String cluster = null;
						String availableCPU = null;
						String numRunningJobs = null;
						String numRunningCores = null;
						String waitingJobs = "0";
						String totalCPU = null;
						String nodes = null;
						
						String[] out2 = out.split("\n");
						for(String line : out2){
							if(line.contains(CLUSTERQUEUE)){
								String patternStr = "(\\d+)";
								Pattern pattern = Pattern.compile(patternStr);
								Matcher matcher = pattern.matcher(line);

								cluster = SGEResource.CLUSTERNAME;
								int i = 0;
								while(matcher.find()){
//									logger.debug(matcher.group(1));
									switch (i) {
										case 2 :  numRunningCores = Integer.toString(Integer.parseInt(matcher.group(0)));
											break;
										case 4 : availableCPU = matcher.group(0); 
											break;
										case 5 : totalCPU = matcher.group(0); 
											break;												
									}

//									System.out.println(matcher.group(0));
									i+=1; 
									if(i>5)break;
								}
								
								availableCPU = Integer.toString(Integer.parseInt(totalCPU) - Integer.parseInt(numRunningCores));

							}else if(line.contains("Nodes Active")){
								String patternStr = "(\\d+)";
								Pattern pattern = Pattern.compile(patternStr);
								Matcher matcher = pattern.matcher(line);
								
								int i = 0;
								while(matcher.find()){
									logger.debug(matcher.group(0));
									switch (i) {
										case 1 :  nodes = matcher.group(0);
											break;
									}
									
									i++;
									if(i==2) break;
								}
								
//								String tmp[]  = line.trim().split(" ");
//								int i =0;
//								for(String li : tmp){
//									
//									System.out.println(i + ": " + li);
//									i++;
//								}
//					            nodes = tmp[4];
							}
						}
						
						logger.info("| cluster:" + cluster + " nodes:" + nodes + " availableCPU:" + availableCPU + " totalCPU:" + totalCPU);
						
						System.out.println(cluster + " " + totalCPU + " " + availableCPU + " " + numRunningCores + " " + waitingJobs);
						ceList.add(cluster + " " + totalCPU + " " + availableCPU + " " + numRunningCores + " " + waitingJobs);
						
				} else {
					throw new SubmitException(result1.getStdError());
				}
				
			} else {
			  throw new SSHException(result1.getStdError());
			}

		} catch (SSHException e1) {
			logger.error("Cluster Monitoring Error:1. Failed to update ceinfo", e1);

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
	    AgentManager.dbClient.updateSCCEInfo("sge", ceList);
//		AgentManager.dbClient.updateCEInfo("pbs", ceList);

	    logger.info("| ServiceInfra: " + name + " " + ceList.size() + " Cluster CEs updated to Database");
		
	  }

	public int getCEList(int ceMetric) {

		if (ceMetric == AgentManager.freeCPU || ceMetric == AgentManager.roundrobin) {

			ceList = AgentManager.dbClient.getCENameList(name, true, false);
			// _debug("ceList = ", ceList);

			// ceList = AgentManager.dbClient.getAvailableCEObjectList(voName);

		} else if (ceMetric == AgentManager.intelligent) {
			// ceList = AgentManager.dbClient.getIntelligentCEList(voName, 100,
			// 0, 5, 1000);
		}

		logger.info("ceList size:" + ceList.size());

		return ceList.size();
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

	public Database getDBClient() {
		return AgentManager.dbClient;
	}

	public String getVoName() {
		return name;
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
	
	public void cancelZombieJob(){
    
    // not implemented
  }

	public static void main(String args[]) {
		SGEResource cr = new SGEResource("sge");
		// cr.getCEList(AgentManager.CE_SELECTION_METRIC);
		try {
			cr.updateCEInfo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
