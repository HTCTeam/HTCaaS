package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kisti.htc.dbmanager.server.Database;

import com.amazonaws.services.ec2.model.Instance;

import util.mLogger;
import util.mLoggerFactory;

public class CloudResource extends BackendResource {

	private Logger logger = Logger.getLogger(this.getClass());
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
    
	public AgentManager am = AgentManager.getInstance();
	
	private File proxyFile;
	public static File cloudDir;
	public static File gangaConfig;
	
	private AmazonCloud ac;
//	private List<Instance> instanceList;
		
	public CloudResource(String name) {
		this.type = "cloud";
		this.name = name;

		this.ac = new AmazonCloud();
		ac.setInsType("t1.micro");
		ac.setKeyPair("seungwoo");
		
		cloudDir = new File("conf/AgentManager/cloud");
		if (!cloudDir.exists() || !cloudDir.isDirectory()) {
			logger.error("cloudDir not exist");
			System.exit(1);
		}
		
		gangaConfig = new File(cloudDir, "gangarc.cloud");
		if (!gangaConfig.exists()) {
			logger.error("Ganga config file for CloudResource not exist");
			System.exit(1);
		}
		
		proxyFile = new File(cloudDir, "cloud.proxy");
	}
	
	@Override
	public void updateCEInfo(){
		
	}
	
	@Override
	public int getCEList(int CE_SELECTION_METRIC){
		
		return -1;
	}
	
	public void showRunningInstances() {
		List<Instance> list = ac.getRunningInstanceInfo();
		System.out.println(list);
	}
	
	public String createInstance() {		
		List<Instance> iList = ac.createAMInstances("ami-903c8991", "seungwoo", 1);
		ac.getRunningState(iList);
		
		List<Instance> list = null;
		list = ac.getRefreshInstanceInfo(iList);
		
		logger.info("Waiting for the Service to start...");
		try {
			Thread.sleep(2 * 60 * 1000);
		} catch (InterruptedException e) {}
		
		return ac.getPublicDnsName(list).get(0);
	}
		
	public void terminateAllInstances() {
		ac.terminateAllInstance();
	}
	
	public void initVomsProxy() throws Exception {
		logger.info("+ Initializing Voms-Proxy");
		
		try {			
			String command = "voms-proxy-init -cert " + cloudDir.getPath() + "/usercert.pem -key " + cloudDir.getPath() + "/userkey.pem "
				+ "-out " + proxyFile.getPath() + " -pwstdin";

	        Process p = Runtime.getRuntime().exec(command);	        
	        
	        // input redirection
	        PrintStream fout = new PrintStream(p.getOutputStream());
			BufferedReader fr = new BufferedReader(new FileReader(cloudDir.getPath() + "/.gridproxy"));
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
		} catch(Exception e) {
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
//		    if (timeLeft == 0) {
//		        throw new Exception(sb.toString());
//			}
		} catch(Exception e) {
			logger.error("Failed to check TimeLeft of Voms-Proxy", e);
			throw new Exception(e);
		}
		
		return timeLeft;
	}
	
	public File getProxyFile() {
		return proxyFile;
	}

	public void setProxyFile(File proxyFile) {
		this.proxyFile = proxyFile;
	}
	
	public Database getDBClient() {
		return AgentManager.dbClient;
	}
	
	public void cancelZombieJob(){
    
    // not implemented
  }
}
