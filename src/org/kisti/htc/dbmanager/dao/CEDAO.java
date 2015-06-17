package org.kisti.htc.dbmanager.dao;

import java.util.List;
import java.util.Set;

import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.CE_Limit;


public interface CEDAO {
	
	public int createCE(CE ce) throws Exception;
	
	public int readCEId(String ceName) throws Exception;
	
	public CE readCEObject(String ceName) throws Exception;
	
	public CE readCEObject(int id) throws Exception;
	
	public List<CE> readCEObjectSet(int sId, int maxRunningTimeLimit) throws Exception;
	
	public List<String> readCEName(int voId, boolean available, boolean banned) throws Exception;
	
	public List<String> readCEsIntelligent(int voId, long waitingTime,int numAgentRunning, int numAgentSubmitFailure, int waitingJob) throws Exception;
	
	public int readCEFreeCPUTotal(int serviceInfra) throws Exception;
	
	public int readCEFreeCPU(String ceName) throws Exception;

	public int readCEAliveAgent(String ceName) throws Exception;
	
	public int readCEAliveAgent(int serviceInfra) throws Exception;
	
	public int readCEZeroCnt(String ceName) throws Exception;
	
	public int readCEEqualCnt(String ceName) throws Exception;
	
	public int readCEPriority(String ceName) throws Exception;
	
	public String readCELimitClass(String ceName) throws Exception;
	
	public int readCELimitCPU(String ceName) throws Exception;
	
	public List<CE> readCEObjectList(int serviceInfra, boolean avail, boolean banned) throws Exception;
	
	public int readCETotalCPU(String ceName) throws Exception;
	
	public int readCEAvailableTimeDiff(String ceName) throws Exception;
	
	public int readCEServiceInfraId(int id) throws Exception;
	
	public void updateCEObject(CE ce) throws Exception;
	
	public void updateCEObjectList(List<CE> ceList) throws Exception;
	
	public void updateCENumAgentRunning(int ceId, int agentId) throws Exception;
	
	public void updateCERunningTime(int ceId, int agentId) throws Exception; 
	
	public void updateCENumAgentSubmitTry(String ceName) throws Exception;
	
	public void updateCENumAgentSubmitFailure(String ceName) throws Exception;
	
	public void updateCEAvailable(int ceId, boolean avail) throws Exception;
	
	public void updateCEsAvailable(int voId, boolean avail) throws Exception;
	
	public void updateCEsInitSubmitCount(int voId) throws Exception;
	
	public void updateCESubmitCountAdd(String ceName, int num) throws Exception;
	
	public void updateCESelectCountAdd(String ceName, int num) throws Exception;
	
	public void updateCEAliveAgentAdd(int ceId, int num) throws Exception;
	
	public void updateCEAliveAgentInit() throws Exception;
	
	public void updateCEZeroCnt(String ceName, int num) throws Exception;
	
	public void updateCEZeroCntAll(int num) throws Exception;
	
	public void updateCESelectCntAll(int num) throws Exception;
	
	public void updateCEZeroCntAdd(String ceName, int num) throws Exception;
	
	public void updateCEPriority(String ceName, int num) throws Exception;
	
	public void updateCEPriorityAdd(String ceName, int num) throws Exception;
	
	public void updateCELimitCPU(String ceName, int num) throws Exception;
	
	public void updateCEWaitingTime(int agentId) throws Exception;
	
	public void updateCEAvailableUpdateTime(String ceName) throws Exception;
	
}
