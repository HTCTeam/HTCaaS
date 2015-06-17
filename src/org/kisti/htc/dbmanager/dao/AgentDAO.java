package org.kisti.htc.dbmanager.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kisti.htc.dbmanager.beans.AgentInfo;

public interface AgentDAO {
	
	public int createAgent() throws Exception;
	public int createAgent(String userId) throws Exception;
	
	public String readAgentStatus(int agentId) throws Exception;
	public String readAgentHost(int agentId) throws Exception;
	public int readAgentCEId(int agentId) throws Exception;
	public boolean readAgentSleep(int agentId) throws Exception;
	public boolean readAgentQuit(int agentId) throws Exception;
	public List<Integer> readAgentJobListByStatus(String status, boolean flag) throws Exception;
	public List<Integer> readAgentList(String status, boolean flag, int serviceInfra) throws Exception;
	public List<Integer> readAgentList(String status, boolean flag, int serviceInfra, int timelimit) throws Exception;
	public int readAgentCurrentJob(int agentId) throws Exception;
	public int readAgentNumStatus(String status) throws Exception;
	public int readAgentNumStatus(String status, int serviceInfra) throws Exception;
	public int readAgentNumAlive(int timelimit) throws Exception;
	public int readAgentNumValidAll() throws Exception;
	@Deprecated
	public int readUserAgentNum(int runningAgent, int submittedAgent, String userId) throws Exception;
	public int readUserAgentNum(String userId) throws Exception;
	public int readUserAgentNumStatus(int userId, String status) throws Exception;
	public Map<String, Integer> readUserAgentNumCE(String status, int userId) throws Exception;
	public Integer readUserAgentNumFromCE(String status, int userId, String ceName) throws Exception;
	public Set<Integer> readAgentUserId(String status) throws Exception;
	
	public Timestamp readAgentRunningTimestamp(int agentId) throws Exception;
	public int readAgentNumJobs(int agentId) throws Exception;
	public int readAgentRunningTime(int agentId) throws Exception;
	public String readAgentUserId(int agentId) throws Exception;
	public String readAgentCEName(int agentId) throws Exception;
	public String readAgentSubmitId(int agentId) throws Exception;
	public int readAgentLastId() throws Exception;
	public int readAgentRunningNum(int minAgentId) throws Exception;
	
	public Set<AgentInfo> readAgentInfoSetFromMetaJob(int metaJobId) throws Exception;
	public Map<String, Integer> readAgentNumMapFromMetaJob(int metaJobId) throws Exception;
	public Map<String, Integer> readAgentTaskMapFromMetaJob(int metaJobId) throws Exception;
	
	
	public void updateAgentNumJobs(int agentId) throws Exception;
	public void updateAgentGangaId(int agentId, int gangaId) throws Exception;
	public void updateAgentSubmitId(int agentId, String submitId) throws Exception;
	public void updateAgentHost(int agentId, String host) throws Exception;
	public void updateAgentRunningStatus(int agentId) throws Exception;
	public void updateAgentCurrentJob(int agentId, Integer jobId) throws Exception;
	public void updateAgentRunningTime(int agentId)	throws Exception;
	public void updateAgentStatus(int agentId, String status) throws Exception;
	
	public void updateAgentSubmittedTimestamp(int agentId) throws Exception;
	public void updateAgentFinish(int agentId) throws Exception;
	public void updateKSCAgentFinish(int agentId, long runningTime) throws Exception;
	public void updateAgentStop(int agentId) throws Exception;
	public void updateAgentFail(int agentId) throws Exception;
	public void updateKSCAgentFail(int agentId, long runningTime) throws Exception;
	public void updateAgentCE(int agentId, String ceName) throws Exception;
	public int updateAgentStatusZombie(String status, String zombie, int timelimit, int serviceInfra) throws Exception;
	public int updateAgentFlag(int agentId, boolean flag) throws Exception;
	public void updateAgentLastSignal(int agentId, String date) throws Exception;
	public void updateAgentValidInit(int serviceInfra) throws Exception;
	
}
