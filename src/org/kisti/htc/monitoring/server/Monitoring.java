package org.kisti.htc.monitoring.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kisti.htc.dbmanager.beans.AgentInfo;
import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.Job;
import org.kisti.htc.dbmanager.beans.MetaJob;
import org.kisti.htc.dbmanager.beans.ServiceInfra;
import org.kisti.htc.dbmanager.beans.User;

/**
 * The Interface Monitoring.
 * @author seungwoo
 * @version 1.0
 * 
 */
public interface Monitoring {

  /**
   * Gets the meta job user id.
   *
   * @param metaJobId the meta job id
   * @return the meta job user id
   */
  public String getMetaJobUserId(int metaJobId);
  
  /**
   * Gets the meta job jsdl.
   *
   * @param metaJobId the meta job id
   * @return the meta job jsdl
   */
  public String getMetaJobJSDL(int metaJobId);
  
  /**
   * Gets the meta job progress.
   *
   * @param metaJobId the meta job id
   * @return the specific meta job progress. (done, 10) 
   */
  public Map<String, Integer> getMetaJobProgress(int metaJobId);
  
  /**
   * Gets the meta job progress all regardless of the user .
   *
   * @return all the meta job progress. (done, 100)
   */
  public Map<String, Integer> getMetaJobProgressAll();
  
  /**
   * Gets the meta job object.
   *
   * @param metaJobId the meta job id
   */
  public MetaJob getMetaJobObject(int metaJobId);
  
  /**
   * Gets the meta job object list of the user.
   *
   * @param user the user
   * @return the meta job object list
   */
  public List<MetaJob> getMetaJobObjectList(String user);
  
  /**
   * Gets the meta job progress in specific range for user
   *
   * @param user the user
   * @param start start meta job id
   * @param end end meta job id
   * @return the meta job object list
   */
  public List<Job> getMetaJobProgressinRange(String user, int start, int end);
  
  /**
   * Gets the meta job object list of the user.(Limit)
   *
   * @param user the user
   * @return the meta job object list
   */
  public List<MetaJob> getMetaJobObjectListLimit(String user, int num);
  
  public List<Integer> getMetaJobIdList(String user);
  
  public int getMetaJobListSubTotal(int startMetaId, int endMetaId);
  
  /**
   * Gets the job object.
   *
   * @param jobId the job id
   * @return the job object
   */
  public Job getJobObject(int jobId);
  
  /**
   * Gets the job object.
   *
   * @param metaJobId the meta job id
   * @param jobSeq the job seq
   * @return the job object
   */
  public Job getJobObject(int metaJobId, int jobSeq);
  
  /**
   * Gets the job object list.
   *
   * @param metaJobId the meta job id
   * @return the job object list
   */
  public List<Job> getJobObjectList(int metaJobId);
  
  /**
   * Gets the job id list.
   *
   * @param metaJobId the meta job id
   * @return the job id list
   */
  public List<Integer> getJobIdList(int metaJobId);
  
  public List<Integer> getJobIdListByStatus(int metaJobId, String status);
  
  /**
   * Gets the job meta job id.
   *
   * @param jobId the job id
   * @return the job meta job id
   */
  public int getJobMetaJobId(int jobId);
  
  
  public int getJobId(int metaJobId, int jobSeq);
  
  public String getJobLog(int metaJobId, int jobSeq);
  
  public String getJobLog(int jobId);
  
  /**
   * Gets the failed job id list.
   *
   * @param metaJobId the meta job id
   * @return the failed job id list
   */
  public List<Integer> getFailedJobIdList(int metaJobId);
  
  public List<Integer> getJobIdListAutodockEL(int metaJobId,int energyLvLow, int energyLvHigh);
  /**
   * Gets the agent status.
   *
   * @param agentId the agent id
   * @return the agent status
   */
  public String getAgentStatus(int agentId);
  
  /**
   * Gets the agent host info.
   *
   * @param agentId the agent id
   * @return the agent host info
   */
  public String getAgentHost(int agentId);
  
  /**
   * Gets the number of alive agent.
   *
   * @param timelimit period of valid time
   * @return the number of alive agent
   */
  public int getNumAliveAgent(int timelimit);

//  /**
//   * Need to submit agent ksc.
//   *
//   * @return true, if successful
//   */
//  public boolean needToSubmitAgentKSC();
  
  /**
   * Gets the results.
   *
   * @param jobId the job id
   * @return the results. the server location of the job results
   */
  public List<String> getResults(int jobId);
  
  /**
   * Gets the available wms list.
   *
   * @param voName the vo name
   * @return the available wms list
   */
  public List<String> getAvailableWMSList(String voName); 
  
  /**
   * Next wms.
   *
   * @param ceName the ce name
   * @return the string
   */
  public String nextWMS(String ceName);
  
  /**
   * Next ce.
   *
   * @param voName the vo name
   * @return the string
   */
  public String nextCE(String voName);
  
  /**
   * Gets the available ce list.
   *
   * @param voName the vo name. Supercom vo is 'PLSI' or '4TH'. Grid vo is 'biomed' or 'vo.france-asia.org'.
   * @return the available ce list
   */
  public List<String> getAvailableCEList(String voName);
  
  /**
   * Gets the intelligent ce list.
   *
   * @param voName the vo name
   * @param waitingTime the waiting time
   * @param numAgentRunning the num agent running
   * @param numAgentSubmitFailure the num agent submit failure
   * @param waitingJob the waiting job
   * @return the intelligent ce list
   */
  public List<String> getIntelligentCEList(String voName,int waitingTime,int numAgentRunning, int numAgentSubmitFailure, int waitingJob);
  
  /**
   * Gets the available wms list for ce.
   *
   * @param ceName the ce name
   * @return the available wms list for ce
   */
  public List<String> getAvailableWMSListForCE(String ceName);
  
  /**
   * Check agent sleep.
   *
   * @param agentId the agent id
   * @return true, if successful
   */
  public boolean checkAgentSleep(int agentId);
  
  /**
   * Check agent quit.
   *
   * @param agentId the agent id
   * @return true, if successful
   */
  public boolean checkAgentQuit(int agentId);
  
  public String getServerEnvValue(String name);
  
  public String getServerEnvContent(String name);
  
  public List<CE> getCEObjectList(int serviceInfra, boolean avail, boolean banned);
  
  public int getNumUserAgentRunning(String userId);
  
  public String getNoticeContent(String div, String version);
  
  public Map<String, Integer> getAgentNumMapFromMetaJob(int metaJob);
  
  public Map<String, Integer> getAgentTaskMapFromMetaJob(int metaJob);
  
  public Set<AgentInfo> getAgentInfoSetFromMetaJob(int metaJob);
  
  public List<CE> getPLSICEInfo() throws Exception;
  
  public Map<String, Integer> getNumUserAgentCE(String agetStatus, int userId);
  
  public User getUserInfo(String userId);
  
  public List<ServiceInfra> getServiceInfraObjects();
  
  public String getServiceInfraName(int id);
  
  public int getServiceInfraId(String name);
}
