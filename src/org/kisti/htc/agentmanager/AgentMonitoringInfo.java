package org.kisti.htc.agentmanager;

import org.kisti.htc.message.DTO;

public class AgentMonitoringInfo {
        
	public static final int JOB_NULL = 0;
	public static final int JOB_PREPARING = 1;
	public static final int JOB_RUNNING = 2;
	public static final int JOB_DONE = 3;
	public static final int JOB_FAILED = 4;
	public static final int JOB_CANCELED = 5;
	
	public static final int AGENT_NULL = 0;
	public static final int AGENT_RUNNING = 1;
	public static final int AGENT_DONE = 2;
	public static final int AGENT_FAILED = 3;
	public static final int AGENT_STOPPED = 4;
	
	
	private boolean job_preparing;
	private boolean job_running;
	private boolean job_done;
	private boolean job_failed;
	private boolean job_canceled;
	
	private boolean agent_running;
	private boolean agent_done;
	private boolean agent_failed;
	private boolean agent_stopped;
	
	private String host;
	private int agentId;
	private DTO jobMsg;
	
	private int agentStatus;
	private int jobStatus;
		
	public AgentMonitoringInfo(int agentId, DTO jobMsg) {
		this.agentId = agentId;
		this.jobMsg = jobMsg;
	}
	
	public int getAgentId() {
		return agentId;
	}
	
	public DTO getJobMsg() {
		return jobMsg;
	}
	
	public String getHost() {
		return host;
	}
		
	public void setHost(String host) {
		this.host = host;
	}
	
	public boolean isHostNull() {
		return (host == null);
	}
	
	public int getAgentStatus() {
		return this.agentStatus;
	}
	
	public void setAgentStatus(int status) {
		this.agentStatus = status;
	}
	
	public int getJobStatus() {
		return this.jobStatus;
	}
	
	public void setJobStatus(int status) {
		this.jobStatus = status;
	}
	
	public boolean isJob_preparing() {
		return job_preparing;
	}

	public void setJob_preparing(boolean jobPreparing) {
		job_preparing = jobPreparing;
	}

	public boolean isJob_running() {
		return job_running;
	}

	public void setJob_running(boolean jobRunning) {
		job_running = jobRunning;
	}

	public boolean isJob_done() {
		return job_done;
	}

	public void setJob_done(boolean jobDone) {
		job_done = jobDone;
	}

	public boolean isJob_failed() {
		return job_failed;
	}

	public void setJob_failed(boolean jobFailed) {
		job_failed = jobFailed;
	}
	
	public boolean isAgent_running() {
		return agent_running;
	}

	public void setAgent_running(boolean agentRunning) {
		agent_running = agentRunning;
	}

	public boolean isAgent_done() {
		return agent_done;
	}

	public void setAgent_done(boolean agentDone) {
		agent_done = agentDone;
	}

	public boolean isAgent_failed() {
		return agent_failed;
	}

	public void setAgent_failed(boolean agentFailed) {
		agent_failed = agentFailed;
	}

	public boolean isJob_canceled() {
		return job_canceled;
	}

	public void setJob_canceled(boolean job_canceled) {
		this.job_canceled = job_canceled;
	}

	public boolean isAgent_stopped() {
		return agent_stopped;
	}

	public void setAgent_stopped(boolean agent_stopped) {
		this.agent_stopped = agent_stopped;
	}
	
	
	
}