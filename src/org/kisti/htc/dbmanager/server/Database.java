package org.kisti.htc.dbmanager.server;

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
 * The Interface DBManager.
 * 
 * @author seungwoo
 * @version 1.0
 * 
 */
public interface Database { 

	/**
	 * Insert meta job.
	 * 
	 * @param userName
	 *            the user name
	 * @param appName
	 *            the app name
	 * @param metaJobDocument
	 *            the meta job document
	 * @return the Integer
	 */
	public Integer insertMetaJob(String userName, String appName, String metaJobDocument, Integer aMaxJobTimeMin, String pName, String sName, String ces);

	/**
	 * Gets the meta job user id.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the meta job user id
	 */
	public String getMetaJobUserId(Integer metaJobId);

	/**
	 * Gets the meta job jsdl.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the meta job jsdl
	 */
	public String getMetaJobJSDL(Integer metaJobId);

	/**
	 * Gets the meta job progress.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the meta job progress
	 */
	public Map<String, Integer> getMetaJobProgress(Integer metaJobId);

	/**
	 * Gets the meta job progress all.
	 * 
	 * @return the meta job progress all
	 */
	public Map<String, Integer> getMetaJobProgressAll();

	/**
	 * Gets the meta job progress in range.
	 * 
	 * @return the meta job progress in range
	 */
	public List<Job> getMetaJobProgressinRange(String user, int start, int end);
	
	/**
	 * Gets the meta job object.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the meta job object
	 */
	public MetaJob getMetaJobObject(Integer metaJobId);

	/**
	 * Gets the meta job object list.
	 * 
	 * @param user
	 *            the user
	 * @return the meta job object list
	 */
	public List<MetaJob> getMetaJobObjectList(String user);
	
	public List<MetaJob> getMetaJobObjectListLimit(String user, int num);
	
	public String getMetaJobStatus(Integer metaJobId);
	
	public List<Integer> getMetaJobIdList(String user);
	
	public int getMetaJobListSubTotal(int startMetaId, int endMetaId);
	
	public Integer getMetaJobAJobTime(Integer metaJobId);
	
	public MetaJob getMetaJobLastRunningFromUser(String userId);

	/**
	 * Sets the meta job status.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @param status
	 *            the status
	 */
	public void setMetaJobStatus(Integer metaJobId, String status);
	
	public void setMetaJobError(Integer metaJobId, String error);
	
	/**
	 * Increase meta job num.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 */
	public void increaseMetaJobNum(Integer metaJobId);

	/**
	 * Decrease meta job num.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 */
	public boolean decreaseMetaJobNum(Integer metaJobId);

	// public void increaseMetaJobTotal(Integer metaJobId);
	/**
	 * Stop meta job.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 */
	public void stopMetaJob(Integer metaJobId);

	/**
	 * Check meta job status by num.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 */
	public void checkMetaJobStatusByNum(Integer metaJobId);
	
	public void checkMetaJobStatusBySubJob(Integer metaJobId);
	
	
	public boolean removeMetaJob(Integer metaJobId);

	/**
	 * Adds the job.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the Integer
	 */
	public Integer addJob(Integer metaJobId, Integer jobSeq);

	/**
	 * Start job.
	 * 
	 * @param jobId
	 *            the job id
	 */
	public boolean startJob(Integer jobId);

	/**
	 * Finish job.
	 * 
	 * @param jobId
	 *            the job id
	 * @param agentId
	 *            the agent id
	 */
	public boolean finishJob(Integer jobId, Integer agentId);

	/**
	 * Sets the job status.
	 * 
	 * @param jobId
	 *            the job id
	 * @param status
	 *            the status
	 */
	public boolean setJobStatus(Integer jobId, String status);

	/**
	 * Sets the job errormsg.
	 * 
	 * @param jobId
	 *            the job id
	 * @param msg
	 *            the msg
	 */
	public boolean setJobErrormsg(Integer jobId, String msg);

	/**
	 * Sets the job detail.
	 * 
	 * @param jobId
	 *            the job id
	 * @param jobDetail
	 *            the job detail
	 */
	public void setJobDetail(Integer jobId, String jobDetail);

	/**
	 * Sets the job cancel.
	 * 
	 * @param metaJobId
	 *            the new job cancel
	 */
	public void setJobCancel(Integer metaJobId);

	public boolean setJobLog(Integer jobId, String jobLog);
	
	public void setJobName(Integer jobId, String name);
	
	public boolean removeJobs(Integer metaJobId);

	/**
	 * Re enqueue job.
	 * 
	 * @param jobId
	 *            the job id
	 */
	public boolean reEnqueueJob(Integer jobId);

	/**
	 * Gets the job num resubmit.
	 * 
	 * @param jobId
	 *            the job id
	 * @return the job num resubmit
	 */
	public Integer getJobNumResubmit(Integer jobId);

	/**
	 * Gets the job object.
	 * 
	 * @param jobId
	 *            the job id
	 * @return the job object
	 */
	public Job getJobObject(Integer jobId);

	/**
	 * Gets the job object.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @param jobSeq
	 *            the job seq
	 * @return the job object
	 */
	public Job getJobObject(Integer metaJobId, Integer jobSeq);

	/**
	 * Gets the job object list.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the job object list
	 */
	public List<Job> getJobObjectList(Integer metaJobId);

	/**
	 * Gets the job meta job id.
	 * 
	 * @param jobId
	 *            the job id
	 * @return the job meta job id
	 */
	public Integer getJobMetaJobId(Integer jobId);

	/**
	 * Gets the job id list.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the job id list
	 */
	public List<Integer> getJobIdList(Integer metaJobId);
	
	public List<Integer> getJobIdListAutodockEL(Integer metaJobId,Integer energyLvLow, Integer energyLvHigh);

	public Integer getJobId(Integer metaJobId, Integer jobSeq);
	
	public String getJobLog(Integer metaJobId, Integer jobSeq);
	
	public String getJobLog(int jobLog);

	/**
	 * Gets the job id list.
	 * 
	 * @param metaJobId
	 *            the meta job id
	 * @return the job id list
	 */
	public List<Integer> getJobIdListByStatus(Integer metaJobId, String status);
	
	public String getJobStatus(Integer jobId);
	
//	public DTO requestJob(String user, Integer agentId, Integer waitingTime);

	/**
	 * Adds the agent.
	 * 
	 * @return the Integer
	 */
	public Integer addAgent();

	public Integer addAgent(String userId);

	/**
	 * Start agent.
	 * 
	 * @param agentId
	 *            the agent id
	 */
	public boolean startAgent(Integer agentId);

	/**
	 * Finish agent.
	 * 
	 * @param agentId
	 *            the agent id
	 */
	public boolean finishAgent(Integer agentId);

	/**
	 * Stop agent.
	 * 
	 * @param agentId
	 *            the agent id
	 */
	public boolean stopAgent(Integer agentId);

	/**
	 * Gets the agent status.
	 * 
	 * @param agentId
	 *            the agent id
	 * @return the agent status
	 */
	public String getAgentStatus(Integer agentId);

	/**
	 * Gets the agent host.
	 * 
	 * @param agentId
	 *            the agent id
	 * @return the agent host
	 */
	public String getAgentHost(Integer agentId);
	
	public String getAgentUserId(Integer agentId);
	
	public String getAgentCEName(Integer agentId);

	public String getAgentSubmitId(Integer agentId);
	
	public List<Integer> getAgentSubmittedZombieList(int serviceInfra);
	
	public Integer getAgentLastId();
	
	public Integer getAgentRunningNum(Integer minAgentId);
	
	public List<Integer> getAgentListFromStatus(int serviceInfra, String status, boolean flag);
	
	/**
	 * Gets the num alive agent.
	 * 
	 * @param timelimit
	 *            the timelimit
	 * @return the num alive agent
	 */
	public Integer getNumAliveAgent(Integer timelimit);

	public Integer getNumValidAgentAll();

	public Integer getNumUserAgent(Integer runningAgentHP, Integer submittedAgentHP, String userId);

	public Integer getNumUserAgentValid(String userId);
	
	public Integer getNumUserAgentStatus(String userId, String status);
	
	public Integer getNumUserAgentRunning(String userId);
	
	public Integer getNumAgent(String status);
	
	public Integer getNumAgent(String status, int serviceInfra);
	
	public Map<String, Integer> getNumUserAgentCE(String status, int userId);

	public Integer getNumUserAgentFromCE(String status, String userId, String ceName);
	
	public Set<Integer> getUserIdFromAgent(String status);
	
	
	/**
	 * Sets the agent ganga id.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param gangaId
	 *            the ganga id
	 */
	public void setAgentGangaId(Integer agentId, Integer gangaId);

	/**
	 * Sets the agent submit id.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param gliteId
	 *            the glite id
	 */
	public void setAgentSubmitId(Integer agentId, String gliteId);
	
	
	public void setAgentSubmitIdMap(Map agentIdMap, String gliteId);

	/**
	 * Sets the agent host.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param host
	 *            the host
	 */
	public boolean setAgentHost(Integer agentId, String host);

	/**
	 * Sets the agent current job.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param jobId
	 *            the job id
	 */
	public boolean setAgentCurrentJob(Integer agentId, Integer jobId, Integer metaJobId);

	/**
	 * Sets the agent pushed.
	 * 
	 * @param agentId
	 *            the new agent pushed
	 */
	public void setAgentPushed(Integer agentId);

	/**
	 * Send agent signal.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param jobId
	 *            the job id
	 * @return the map
	 */
	public Map<String, Boolean> sendAgentSignal(Integer agentId, Integer jobId);

	/**
	 * Sets the agent status.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param status
	 *            the status
	 */
	public void setAgentStatus(Integer agentId, String status);

	/**
	 * Sets the agent ce.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param ceName
	 *            the ce name
	 */
	public void setAgentCE(Integer agentId, String ceName);

	
	public void setAgentLastSignal(Integer agentId, String date);
	
	
	public void setAgentValidInit(int serviceInfra);
	/**
	 * Report agent failure.
	 * 
	 * @param agentId
	 *            the agent id
	 */
	public boolean reportAgentFailure(Integer agentId);

	/**
	 * Increase agent num jobs.
	 * 
	 * @param agentId
	 *            the agent id
	 */
	public void increaseAgentNumJobs(Integer agentId);

	/**
	 * Finish ksc agent.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param runningTime
	 *            the running time
	 */
	public void finishKSCAgent(Integer agentId, long runningTime);

	// /**
	// * Increase num agents ksc.
	// */
	// public void increaseNumAgentsKSC();
	//
	// /**
	// * Decrease num agents ksc.
	// */
	// public void decreaseNumAgentsKSC();

	// /**
	// * Need to submit agent ksc.
	// *
	// * @return true, if successful
	// */
	// public boolean needToSubmitAgentKSC();

	/**
	 * Start job ksc.
	 * 
	 * @param jobId
	 *            the job id
	 */
	public void startJobKSC(Integer jobId);

	/**
	 * Finish job ksc.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param jobId
	 *            the job id
	 * @param runningTime
	 *            the running time
	 */
	public void finishJobKSC(Integer agentId, Integer jobId, long runningTime);

	/**
	 * Report ksc agent failure.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param runningTime
	 *            the running time
	 */
	public void reportKSCAgentFailure(Integer agentId, long runningTime);

	/**
	 * Adds the application.
	 * 
	 * @param name
	 *            the name
	 * @return the Integer
	 */
	public Integer addApplication(String name);
	
	public Integer getApplicationId(String name);

	/**
	 * Adds the result.
	 * 
	 * @param jobId
	 *            the job id
	 * @param LFN
	 *            the lfn
	 * @return the Integer
	 */
	public Integer addResult(Integer jobId, Integer metaJobId, String LFN);
	
	public boolean removeResults(Integer metaJobId);

	/**
	 * Gets the results.
	 * 
	 * @param jobId
	 *            the job id
	 * @return the results
	 */
	public List<String> getResults(Integer jobId);

	/**
	 * Adds the user.
	 * 
	 * @param dn
	 *            the dn
	 * @param name
	 *            the name
	 * @param userId
	 *            the user id
	 * @param infraMetric
	 *            the infra metric
	 */
	public Integer addUser(String dn, String name, String userId, String passwd, String infraMetric);

	/**
	 * Delete user.
	 * 
	 * @param Id
	 *            the id
	 */
	public void deleteUser(Integer Id);

	/**
	 * Gets the user password.
	 * 
	 * @param userId
	 *            the user id
	 * @return the user password
	 */
	public String getUserPasswd(String userId);

	public Integer getUserKeepAgentNO(String userId);
	
	public List<ServiceInfra> getUserServiceInfra(String userId);

	public boolean setUserInfo(String userId, User user); 
	
	/**
	 * Sets the user name.
	 * 
	 * @param userId
	 *            the user id
	 * @param name
	 *            user name
     *  @return true, if successful
	 */
	public boolean setUserName(String userId, String name);

	/**
	 * Sets the user password.
	 * 
	 * @param userId
	 *            the user id
	 * @param pw
	 *            the password
	 */
	public void setUserPasswd(String userId, String pw);

	/**
	 * Sets the user dn.
	 * 
	 * @param userId
	 *            the user id
	 * @param dn
	 *            the dn info
	 */
	public void setUserDN(String userId, String dn);

	/**
	 * Sets service ID.
	 * 
	 * @param userId
	 *            the user id
	 * @param sid
	 *            the service id
	 *  @return true, if successful
	 */
	public boolean setServiceId(String userId, String sid);

	/**
	 * set KeepAgentNO
	 * 
	 * @param userId
	 *            the user id
	 * @param keepAgentNO
	 * @return true, if successful
	 */
	public boolean setUserKeepAgentNO(String userId, Integer keepAgentNO);

	/**
	 * update OTP flag
	 * 
	 * @param userId
	 * @param otp_flag 
	 * @return true, if successful
	 */
	public boolean updateOTPflag(String userId, int otp_flag);
	
	/**
	 * Check user.
	 * 
	 * @param userId
	 *            the user id
	 * @return true, if successful
	 */
	public boolean checkUser(String userId);

	/**
	 * Gets the user list.
	 * 
	 * @return the user list
	 */
	public List<User> getUserObjectList();

	/**
	 * Gets the id.
	 * 
	 * @param userId
	 *            the user id
	 * @return the id
	 */
	public Integer getId(String userId);

	// public User getUserInfo(String userId);
	/**
	 * Gets the user info.
	 * 
	 * @param userId
	 *            the user id
	 * @return the user info
	 */
	public User getUserInfo(String userId);

	/**
	 * Update wms list.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @param wmsList
	 *            the wms list
	 */
	public void updateWMSList(String serviceInfraName, List<String> wmsList);

	/**
	 * Sets the wms info.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @param wmsName
	 *            the wms name
	 * @param responseTime
	 *            the response time
	 * @param ceList
	 *            the ce list
	 */
	public void setWMSInfo(String serviceInfraName, String wmsName, long responseTime, List<String> ceList);

	/**
	 * Gets the available wms list.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @return the available wms list
	 */
	public List<String> getAvailableWMSList(String serviceInfraName);

	

	/**
	 * Next wms.
	 * 
	 * @param ceName
	 *            the ce name
	 * @return the string
	 */
	public String nextWMS(String ceName);

	/**
	 * Gets the available wms list for ce.
	 * 
	 * @param ceName
	 *            the ce name
	 * @return the available wms list for ce
	 */
	public List<String> getAvailableWMSListForCE(String ceName);
	
	/**
	 * Next ce.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @return the string
	 */
	public String nextCE(String serviceInfraName);

	/**
	 * Gets the available ce list.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @return the available ce list
	 */
	public List<CE> getAvailableCEObjectList(String serviceInfraName);

	/**
	 * Gets the Integerelligent ce list.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @param waitingTime
	 *            the waiting time
	 * @param numAgentRunning
	 *            the num agent running
	 * @param numAgentSubmitFailure
	 *            the num agent submit failure
	 * @param waitingJob
	 *            the waiting job
	 * @return the Integerelligent ce list
	 */
	public List<String> getIntelligentCEList(String serviceInfraName, Integer waitingTime, Integer numAgentRunning, Integer numAgentSubmitFailure, Integer waitingJob);

	
	public Integer getCEFreeCPUTotal(Integer serviceInfra);
	
	public Integer getCEFreeCPU(String ceName);
	
	public Integer getCEAliveAgent(String ceName);
	
	public Integer getCEAliveAgentFromAMEnv(String siSet);
	
	public Integer getCEZeroCnt(String ceName);
	
	public Integer getCEEqualCnt(String ceName);
	
	public Integer getCEPriority(String ceName);

	public String getCELimitClass(String ceName);
	
	public List<String> getCENameList(String serviceInfra, boolean available, boolean banned);
	
	public Integer getCELimitCPU(String ceName);
	
	public List<CE> getCEObjectList(int serviceInfra, boolean avail, boolean banned);
	
	public CE getCEObject(String name);
	
	public CE getCEObject(int id);
	
	public Integer getCETotalCPU(String ceName);
	
	public Integer getCEAvailableTimeDiff(String ceName);
	
	public Integer getCEServiceInfraId(int id);

	/**
	 * Sets the all ces unavailable.
	 * 
	 * @param serviceInfraName
	 *            the new all c es unavailable
	 */
	
	public void setAllCEsUnavailable(String serviceInfraName);

	public void setCEAliveAgentAdd(Integer agentId, Integer num);
	
	public void setCEAliveAgentAddFromJob(Integer jobId, Integer num);
	
	public void setCEAliveAgentInit();
	
	public void setCEZeroCnt(String ceName, Integer num);
	
	public void setCEZeroCntAll(Integer num);
	
	public void setCESelectCntAll(Integer num);
	
	public void setCEZeroCntAdd(String ceName, Integer num);
	
	public void setCEPriority(String ceName, Integer num);
	
	public void setCEPriorityAdd(String ceName, Integer num);
	
	public void setCEAvailable(String ceName, boolean available);
	
	public void setCELimitCPU(String ceName, Integer num);
	
	public void setCEWaitingTime(Integer agentId);
	
	public void setCEAvailableUpdateTime(String ceName);
	
	public void setCEInfo(List<CE> ceList);
	
	/**
	 * Update ce info.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @param ceList
	 *            the ce list
	 */
	public boolean updateCEInfo(String serviceInfraName, List<String> ceList);

	/**
	 * Update scce info.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 * @param ceList
	 *            the ce list
	 */
	public void updateSCCEInfo(String serviceInfraName, List<String> ceList);
	
	/**
	 * Inits the ce submit count.
	 * 
	 * @param serviceInfraName
	 *            the service infra name
	 */
	public void initCESubmitCount(String serviceInfraName);

	/**
	 * Increase ce submit count.
	 * 
	 * @param ceName
	 *            the ce name
	 * @return true, if successful
	 */
	public boolean increaseCESubmitCount(String ceName, Integer num);
	
	public boolean increaseCESelectCount(String ceName, Integer num);

	/**
	 * Check running zombie agent job.
	 * 
	 * @param timelimit
	 *            the timelimit
	 * @return the list
	 */
	public Integer checkRunningZombieAgentJob(Integer timelimit, int serviceInfra);

	/**
	 * Check new zombie agent.
	 * 
	 * @param timelimit
	 *            the timelimit
	 * @return the list
	 */
	public Integer checkNewZombieAgent(Integer timelimit, int serviceInfra);

	/**
	 * Check submitted zombie agent.
	 * 
	 * @param timelimit
	 *            the timelimit
	 * @return the list
	 */
	public Integer checkSubmittedZombieAgent(Integer timelimit, int serviceInfra);

	/**
	 * Check submit error agent.
	 * 
	 * @return the Integer
	 */
	public Integer checkSubmitErrorAgent();

	/**
	 * Check done agent.
	 * 
	 * @return the Integer
	 */
	public Integer checkDoneAgent();


	/**
	 * Check agent sleep.
	 * 
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean checkAgentSleep(Integer agentId);

	/**
	 * Check agent quit.
	 * 
	 * @param agentId
	 *            the agent id
	 * @return true, if successful
	 */
	public boolean checkAgentQuit(Integer agentId);

	/**
	 * Report submit error.
	 * 
	 * @param agentId
	 *            the agent id
	 * @param wmsName
	 *            the wms name
	 * @param ceName
	 *            the ce name
	 * @param errorMsg
	 *            the error msg
	 */
	public void reportSubmitError(Integer agentId, Integer metaJobId, String wmsName, String ceName, String errorMsg);
	
	public void reportSubmitErrorMap(Map agentIdMap, Integer metaJobId, String wmsName, String ceName, String errorMsg);

	/**
	 * Gets the agentmanager auto mode.
	 * 
	 * @param name
	 *            the name
	 * @return the mode
	 */
	public boolean getAMEnvAutoMode(Integer id);

	public Integer getAMEnvId(String name);

	/**
	 * Gets the AgentManager env.
	 * 
	 * @param name
	 *            the name
	 * @return the env
	 */
	public Map<String, Object> getAMEnv(Integer id);

	/**
	 * Sets all the agent manager environment configuration values.
	 * 
	 * @param name
	 *            the name
	 * @param env
	 */

	public void setAMEnv(Integer id, Map<String, Integer> env);

	public Integer insertAMEnv(String name);
	
	public List<ServiceInfra> getServiceInfraObjects();

	public boolean getServiceInfraAvail(String name);
	
	public String getServiceInfraName(int id);
	
	public int getServiceInfraId(String name);
	
	public String getServerEnvValue(String name);
	
	public String getServerEnvContent(String name);
	
	public String getNoticeContent(String div, String version);
	
	public Integer getInteger_T(Integer num);
	
	public Integer getInteger_N(Integer num);
	
	public Map<String, Integer> getAgentNumMapFromMetaJob(int metaJobId);

	public Map<String, Integer> getAgentTaskMapFromMetaJob(int metaJobId);
	
	public Set<AgentInfo> getAgentInfoSetFromMetaJob(int metaJobId);

	
}
