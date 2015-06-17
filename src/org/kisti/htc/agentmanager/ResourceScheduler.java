package org.kisti.htc.agentmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;

public class ResourceScheduler {

	final Logger logger = LoggerFactory.getLogger(ResourceScheduler.class);
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
	
	private List<BackendResource> originalList;
	private int totalMaxJobNum;
	
	
	public ResourceScheduler() {
		originalList = new ArrayList<BackendResource>();
	}
	
	public void addResource(BackendResource br) {
		originalList.add(br);
		totalMaxJobNum += br.getMaxJobNum();
	}
	
	public BackendResource chooseOne(String name) {
		logger.info("chooseOne : " + name);
		logger.info("Original Resource List :" + originalList.size());
		
		BackendResource ret = null;

		if(name.equals(AgentManager.LOCAL)){
			for (BackendResource br : originalList) {
				if(br instanceof LocalMachine){
					ret = br;
					logger.info("choose Local");
					break;
				}
			}
		}else if(name.equals(AgentManager.BIOMED)){
			for (BackendResource br : originalList) {
				if(br instanceof GliteResource && br.getName().equals(AgentManager.BIOMED)){
					ret = br;
					logger.info("choose BIOMED");
					break;
				}
			}
		}else if(name.equals(AgentManager.VOFA)){
			for (BackendResource br : originalList) {
				if(br instanceof GliteResource && br.getName().equals(AgentManager.VOFA)){
					ret = br;
					logger.info("choose VOFA");
					break;
				}
			}
		}else if(name.equals(AgentManager.PLSI)){
			for (BackendResource br : originalList) {
				if(br instanceof LLResource){
					ret = br;
					logger.info("choose LoadLeveler");
					break;
				}
			}
		}else if(name.equals(AgentManager.AMAZONEC2)){
			for (BackendResource br : originalList) {
				if(br instanceof CloudResource){
					ret = br;
					logger.info("choose AMAZONEC2");
					break;
				}
			}
		}else if(name.equals(AgentManager.PBS)){
			for (BackendResource br : originalList) {
				if(br instanceof ClusterResource){
					ret = br;
					logger.info("choose PBS");
					break;
				}
			}
		}else if(name.equals(AgentManager.CONDOR)){
			for (BackendResource br : originalList) {
				if(br instanceof CondorResource){
					ret = br;
					logger.info("choose Condor");
					break;
				}
			}	
		}else{
			logger.info("Not find resource. choose random resource :");
			for (BackendResource br : originalList) {
				logger.warn(br.getName());
				ret = br;
				
			}
//			int idx = new Random().nextInt(totalMaxJobNum) + 1;
//			int total = 0;
//			
//			for (BackendResource br : originalList) {
//				total += br.getMaxJobNum();
//				if (idx <= total) {
//					
//					logger.info("| [" + idx + "/" + totalMaxJobNum + "] " + br.getName());
//					if (!br.available()) {
//						logger.error("| full");
//						break;
//					}
//					br.setJobNum(br.getJobNum() + 1);
//					
//					ret = br;
//					break;
//				}
//			}
		}
		
		return ret;
		
	}
	
	public void initResourceList(){
		originalList.clear();
		totalMaxJobNum = 0;
	}
	
	
	public List<BackendResource> getResourceList() {
		return originalList;
	}
	
	public static void main(String[] args) throws Exception {
		ResourceScheduler rsc = new ResourceScheduler();
		
		GliteResource gm = new GliteResource(AgentManager.BIOMED);
		gm.setMaxJobNum(10);	
		rsc.addResource(gm);
//		
//		SuperComputer sc = new SuperComputer("TACHYON2");
//		sc.setMaxJobNum(5);
//		rsc.addResource(sc);
//		
//		LocalMachine lm = new LocalMachine("local");
//		lm.setMaxJobNum(5);
//		rsc.addResource(lm);
		
		for(int i=0;i<1;i++){
			BackendResource a = rsc.chooseOne(AgentManager.BIOMED);
			System.out.println(a.toString());
		}
		
	}

}
