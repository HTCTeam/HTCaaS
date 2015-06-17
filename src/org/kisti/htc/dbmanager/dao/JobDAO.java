package org.kisti.htc.dbmanager.dao;

import java.util.List;
import java.util.Map;

import org.kisti.htc.dbmanager.beans.Job;


// JobDAOImpl impelment JobDAO
public interface JobDAO {
	
	public int createJob(int metaJobId, int jobSeq) throws Exception;
	
	public int readJobMetaJobId(int jobId) throws Exception;
	
	public String readJobStatus(int jobId) throws Exception;
	
	public String readJobDetail(int jobId) throws Exception;
	
	public int readJobNumResubmit(int jobId) throws Exception;
	
	public Map<String, Integer> readJobTotalStatusInfo() throws Exception; 
	
	public List<Job> readJobObjectList(int metaJobId) throws Exception;
	
	public List<Job> readJobPartialStatusInfo (String userId, int start, int end) throws Exception;
	
	public Job readJobObject(int jobId) throws Exception;
	
	public Job readJobObject(int metaJobId, int jobSeq) throws Exception;
	
	public List<Integer> readJobIdList(int metaJobId) throws Exception;
	
	public List<Integer> readJobIdListAutodockEL(int metaJobId, int energyLvLow, int energyLvHigh) throws Exception;
	
	public boolean readJobStop(int jobId) throws Exception;
	
	public List<Integer> readJobIdByStatus(int metaJobId, String Status) throws Exception;
	
	public int readJobId(int metaJobId, int jobSeq) throws Exception;
	
	public String readJobLog(int metaJobId, int jobSeq) throws Exception;
	
	public String readJobLog(int jobId) throws Exception;
	
	public int readPendingJobNum(int metaJobId) throws Exception;
	
	public int readJobCEId(int jobId) throws Exception;
	
	public void updateJobStart(int jobId) throws Exception;

	public void updateJobFinish(int jobId) throws Exception;
	
	public void updateJobStatus(int jobId, String status) throws Exception;
	
	public void updateJobErrormsg(int jobId, String msg) throws Exception;
	
	public void updateJobDetail(int jobId, String jobDetail) throws Exception;
	
	public void updateJobCEnAgentId(int jobId, int agentId) throws Exception;
	
	public void updateJobRunningTime(int jobId) throws Exception;
	
	public void updateJobObject(int jobId, Job job) throws Exception;
	
	public void updateJobStop(int jobId) throws Exception;
	
	public void updateJobCancel(int metaJobId) throws Exception;
	
	public boolean updateJobLog(int jobId, String jobLog) throws Exception;
	
	public void updateJobName(int jobId, String name) throws Exception;
	
	public void updateJobNumResubmitAdd(int jobId, int num) throws Exception;
	
	public boolean deleteJobs(int metaJobId) throws Exception;
}
