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

import util.mLogger;
import util.mLoggerFactory;

public class LocalJob {

	protected Logger logger = Logger.getLogger(this.getClass());
//  final static mLogger logger = mLoggerFactory.getLogger("AM");

	private LocalMachine lm;
	private File submitScript;
	private int agentId;
	private String userName;
	private String ceName;

	public static final int everyNjobs = 0;
	public static final int avgEnqueueTime = 1;
	public static final int maxEnqueueTime = 2;
	public static final int addNagents = 3;
	public static final int keepQagents = 4;
	public static final int samplingAgents = 5;
	public static final int testAllCombination = 6;
	
	public LocalJob(LocalMachine lm, String userName, String ceName) {
		this.lm = lm;
		this.userName = userName;
		this.ceName = ceName; 
	}
	
	public LocalJob(LocalMachine lm) {
		this.lm = lm;
	}
		
	private void generateSubmitScript() {
		
		StringBuffer content = new StringBuffer();		
		content.append("j=Job()\n");
		if(AgentManager.AGENT_SCALING_METRIC == samplingAgents){
			content.append("j.application=Executable(exe=File('"+ AgentManager.scriptDir.getAbsolutePath() +"/runSamplingAgent.sh'),args=['" + agentId + "'])\n");
		} else {
			content.append("j.application=Executable(exe=File('"+ AgentManager.scriptDir.getAbsolutePath() +"/runAgent.sh'),args=['" + agentId + "','"+userName+"'])\n");
		}
		
		content.append("j.backend=Local()\n");
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

		agentId = lm.getDBClient().addAgent(userName);
    	logger.info("| New Agent added, AgentID : " + agentId);
    	
    	lm.getDBClient().setAgentCE(agentId, ceName);
//		generateSubmitScript();
		
    	RunningDaemon th = new RunningDaemon();
    	th.start();
    	
    	logger.info("| Successfully submitted.");
		
		
		return true;
	}
	
	private class RunningDaemon extends Thread {

		@Override
		public void run() {
			
			File wd = null;
			
			try {
//				System.out.println(System.getProperty("user.dir"));
		        List<String> command = new ArrayList<String>();
//		        command.add("ganga");
//		        command.add("--config=" + LocalMachine.gangaConfig.getAbsolutePath());
//		        command.add("--very-quiet");
//		        command.add(submitScript.getName());
//		        command.add("--test");
//		        command.add("ls");
		        
		        command.add(AgentManager.scriptDir.getAbsolutePath() + "/runAgent.sh");
		        command.add(""+agentId);
		        command.add(userName);
		        
		        ProcessBuilder builder = new ProcessBuilder(command);
		        Map<String, String> envs = builder.environment();
		        envs.put("X509_USER_PROXY", lm.getProxyFile().getAbsolutePath());
		        
		        wd = new File(AgentManager.tempDir.getAbsoluteFile() + "/" + userName + "/" + agentId);
		        wd.mkdirs();
		        builder.directory(wd);
			        
				Process process = builder.start();
				
				int exitValue = process.waitFor();
				
				if (exitValue == 0) {
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = br.readLine();
//					int gangaID = Integer.parseInt(line);
					while ((line = br.readLine()) != null) {
			        	logger.debug("| " + line);
			        }
					logger.info("| Successfully completed.");
//		    		lm.getDBClient().setAgentGangaId(agentId, 0);
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
				logger.error("Failed to run a new agent", e);
			} finally {
				if(wd != null){
					DeleteFileAndDirUtil.deleteFilesAndDirs(wd.getAbsolutePath());
				}
			}
		}
		
	}
	
	public static void main(String args[]){
		
		LocalJob local = new LocalJob(new LocalMachine("test"), "seungwoo", "localhost");
		
		local.submit();
		
	}
		
	
}
