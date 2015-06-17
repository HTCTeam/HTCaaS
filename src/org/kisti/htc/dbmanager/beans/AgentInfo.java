package org.kisti.htc.dbmanager.beans;

import java.util.Date;
import java.util.List;

public class AgentInfo {

	private int id;
	private String host;
	private int numJobs;
	private String status;
	private Date submittedTimestamp;
	private Date startTimestamp;
	private Date endTimestamp;
	private long runningTime;

	public AgentInfo() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getNumJobs() {
		return numJobs;
	}

	public void setNumJobs(int numJobs) {
		this.numJobs = numJobs;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getSubmittedTimestamp() {
		return submittedTimestamp;
	}

	public void setSubmittedTimestamp(Date submittedTimestamp) {
		this.submittedTimestamp = submittedTimestamp;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getEndTimestamp() {
		return endTimestamp;
	}

	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	@Override
	public String toString() {
		return "=================================================================="
				+"\nID:" + id + "\nhost:" + host + "\nnumJobs:" + numJobs + "\nStatus:" + status
				+ "\nsubmittedTimeStamp:" + submittedTimestamp 
				+ "\nrunningTimeStamp:" + startTimestamp
				+ "\nendTimeStamp:" + endTimestamp 
				+ "\nRunning Time(s):" + runningTime
				+"\n==================================================================" ;
	}

}
