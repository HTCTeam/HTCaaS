package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class CloudJob {

	protected Logger logger = Logger.getLogger(this.getClass());
//  final static mLogger logger = mLoggerFactory.getLogger("AM");

	private CloudResource cr;
	private File submitScript;
	private int agentId;
	private String dns;

	private String type;
	
	public CloudJob(CloudResource cr, String type) {
		this.cr = cr;
		this.type = type;
	}
		
	private void generateSubmitScript() {
		
		StringBuffer content = new StringBuffer();		
		content.append("j=Job()\n");
		content.append("j.application=Executable(exe=File('"+ AgentManager.scriptDir.getAbsolutePath() +"/runAgent.sh'),args=['" + agentId + "'])\n");
		content.append("j.backend=RemoteLocal()\n");
		content.append("j.backend.host = \"" + dns + ":22\"\n");
		content.append("j.backend.username = \"root\"\n");
		content.append("j.backend.ssh_key = \"" + CloudResource.cloudDir.getAbsolutePath() + "/seungwoo.pem"+"\"\n");
		content.append("j.submit()\n");
		content.append("print j.id\n");
		
		submitScript = new File(AgentManager.tempDir, UUID.randomUUID() + ".py");
		try {         
			PrintStream ps = new PrintStream(submitScript);
			ps.println(content);
			ps.close();
		}
		catch (Exception e) {
			logger.error("Failed to Generate Ganga Submit Script: " + e.getMessage());
		}
	}
	
	public boolean submit() {
		dns = cr.createInstance();
				
		if (type.equals("SSH")) {
			return submitDirectly();
		} else if (type.equals("Ganga")) {
			return submitUsingGanga();
		} else {
			logger.error("Unknown Submission Type: " + type);
			return false;
		}
	}
	
	public boolean submitDirectly() {
		
		agentId = cr.getDBClient().addAgent();
    	logger.info("| New Agent added, AgentID : " + agentId);
    	
    	cr.getDBClient().setAgentCE(agentId, "AmazonEC2");
    	
//    	System.exit(1);
    	
    	logger.info("| Submitting application to AmazonEC2(" + dns + ")");
    	
    	SshClient sc = new SshClient();
    	
//    	int tryNum = 0;
//    	
//    	while(true) {
			try {
				Session ss = sc.getSessionPEM(dns, CloudResource.cloudDir.getAbsolutePath() + "/seungwoo.pem");
				sc.ScpTo(AgentManager.scriptDir.getAbsolutePath() +"/runAgentEC2.sh", "/root", ss, false);
				sc.Exec("chmod +x /root/runAgentEC2.sh", ss, false);
				sc.Exec("qsub /root/runAgentEC2.sh -v AID=" + agentId, ss, false);
				cr.getDBClient().setAgentSubmitId(agentId, dns);
				
				logger.info("SUCCESS");
//				break;
			} catch (Exception e) {
				logger.error("| " + e.getMessage());
				return false;
				
//				if (e.getMessage().contains("No route to host") || e.getMessage().contains("Connection refused")) {
//					tryNum++;
//					if (tryNum >= 10) {
//						return false; 
//					}
//					logger.info("| Try Again : " + tryNum);
//					
//					try {
//						Thread.sleep(10 * 1000);
//					} catch (InterruptedException ee) {
//						ee.printStackTrace();
//					}
//				} else {
//					return false;
//				}
			}
//    	}
    	
		return true;
	}
	
	public boolean submitUsingGanga() {

		agentId = cr.getDBClient().addAgent();
    	logger.info("| New Agent added, AgentID : " + agentId);
    	
    	cr.getDBClient().setAgentCE(agentId, "AmazonEC2");
    	
		generateSubmitScript();
		
		try {
	        List<String> command = new ArrayList<String>();
	        command.add("ganga");
	        command.add("--config=" + CloudResource.gangaConfig.getAbsolutePath());
	        command.add("--very-quiet");
	        command.add(submitScript.getName());
	        	        	        
	        ProcessBuilder builder = new ProcessBuilder(command);   
	        Map<String, String> envs = builder.environment();
	        envs.put("X509_USER_PROXY", cr.getProxyFile().getAbsolutePath());
	        	        
	        builder.directory(AgentManager.tempDir);
		        
			Process process = builder.start();
			int exitValue = process.waitFor();
			
			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line = br.readLine();
				int gangaID = Integer.parseInt(line);
				logger.info("| Successfully submitted, gangaID: " + gangaID);
	    		cr.getDBClient().setAgentGangaId(agentId, gangaID);
		        br.close();
			} else {
				logger.info("Exit Value: " + exitValue);
		        logger.info("| [ErrorStream]");
		        BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		        String line;
		        while ((line = br.readLine()) != null) {
		        	logger.info("| " + line);
		        }
		        br.close();
	        }
		} catch (Exception e) {
			logger.error("Failed to submit a new agent", e);
			return false;
		}
		
		submitScript.delete();
				
		return true;
	}
	
	public static void main(String arg[]){
		CloudResource cr = new CloudResource("amazon");

//		cr.terminateAllInstances();
//		cr.showRunningInstances();
//		System.exit(1);
				
//		String dns = "ec2-176-32-65-214.ap-northeast-1.compute.amazonaws.com";
		CloudJob cj = new CloudJob(cr, "SSH");
		cj.submit();
	}
	
}
