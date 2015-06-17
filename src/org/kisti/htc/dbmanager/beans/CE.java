package org.kisti.htc.dbmanager.beans;

import java.util.Date;
import java.util.Set;


public class CE {

/*
mysql> desc ce;
+-----------------------+--------------+------+-----+---------+----------------+
| Field                 | Type         | Null | Key | Default | Extra          |
+-----------------------+--------------+------+-----+---------+----------------+
| id                    | int(11)      | NO   | PRI | NULL    | auto_increment | 
| available             | bit(1)       | NO   |     | NULL    |                | 
| banned                | bit(1)       | NO   |     | NULL    |                | 
| lastUpdateTime        | datetime     | YES  |     | NULL    |                | 
| maxRunningTime        | bigint(20)   | NO   |     | 0       |                | 
| name                  | varchar(255) | YES  | MUL | NULL    |                | 
| runningTime           | bigint(20)   | YES  |     | NULL    |                | 
| waitingTime           | bigint(20)   | YES  |     | NULL    |                | 
| service_Infra_id      | int(11)      | YES  | MUL | NULL    |                | 
| freeCPU               | int(11)      | NO   |     | 0       |                | 
| totalCPU              | int(11)      | NO   |     | 0       |                | 
| SYSTEM_LIMIT_NO       | int(11)      | YES  |     | NULL    |                | 
| numAgentRunning       | int(11)      | NO   |     | 0       |                | 
| numAgentSubmitFailure | int(11)      | NO   |     | 0       |                | 
| numAgentSubmitTry     | int(11)      | NO   |     | 0       |                | 
| submitCount           | int(11)      | NO   |     | 0       |                | 
| runningJob            | int(11)      | YES  |     | NULL    |                | 
| waitingJob            | int(11)      | YES  |     | NULL    |                | 
+-----------------------+--------------+------+-----+---------+----------------+
*/
	private int id;
	private boolean available;
	private boolean banned;
	private int priority;
	private Date lastUpdateTime;
	private long maxRunningTime;
	private String name;
	private long runningTime;
	private long waitingTime;
	private int serviceInfraId;
	private int freeCPU;
	private int totalCPU;
	private int node;

	
	private int numAgentRunning;
	private int numAgentSubmitFailure;
	private int numAgentSubmitTry;
	private int submitCount;
	private int selectCount;
	private int runningJob;
	private int waitingJob;

	private Set<WMS> WMSes;
	
	private int SYSTEM_LIMIT_NO;
	
	
	public CE() {}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Date getLastUpdateTime() { return lastUpdateTime; }
	public void setLastUpdateTime(Date time) { this.lastUpdateTime = time; }
	
	public long getWaitingTime() { return waitingTime; }
	public void setWaitingTime(long waitingTime) { this.waitingTime = waitingTime; }

	public long getRunningTime() { return runningTime; }
	public void setRunningTime(long runningTime) { this.runningTime = runningTime; }

	public long getMaxRunningTime() { return maxRunningTime; }
	public void setMaxRunningTime(long maxRunningTime) { this.maxRunningTime = maxRunningTime; }
	
	public boolean isAvailable() { return available; }
	public void setAvailable(boolean available) { this.available = available; }

	public boolean isBanned() {	return banned; }
	public void setBanned(boolean banned) { this.banned = banned; }
	
	public int getNumAgentSubmitTry() { return numAgentSubmitTry; }
	public void setNumAgentSubmitTry(int numAgentSubmitTry) { this.numAgentSubmitTry = numAgentSubmitTry; }
	
	public int getNumAgentSubmitFailure() { return numAgentSubmitFailure; }
	public void setNumAgentSubmitFailure(int numAgentSubmitFailure) { this.numAgentSubmitFailure = numAgentSubmitFailure; }
	
	public int getNumAgentRunning() { return numAgentRunning; }
	public void setNumAgentRunning(int numAgentRunning) { this.numAgentRunning = numAgentRunning; }
	
	public Set<WMS> getWMSes() { return WMSes; }
	public void setWMSes(Set<WMS> WMSes) { this.WMSes = WMSes; }
	
		
	public int getServiceInfraId() { return serviceInfraId; }
	public void setServiceInfraId(int serviceInfraId) { this.serviceInfraId = serviceInfraId; }
	
	public int getTotalCPU() { return totalCPU; }
	public void setTotalCPU(int totalCPU) { this.totalCPU = totalCPU; }
	
	public int getFreeCPU() { return freeCPU; }
	public void setFreeCPU(int freeCPU) { this.freeCPU = freeCPU; }
	
	public int getSubmitCount() { return submitCount; }
	public void setSubmitCount(int submitCount) { this.submitCount = submitCount; }
	
	public int getSelectCount() { return selectCount; }
	public void setSelectCount(int selectCount) { this.selectCount = selectCount; }
	
	public int getRunningJob() { return runningJob; }
	public void setRunningJob(int runningJob) { this.runningJob = runningJob; }
	
	public int getWaitingJob() { return waitingJob; }
	public void setWaitingJob(int waitingJob) { this.waitingJob = waitingJob; }
	
	public int getSYSTEM_LIMIT_NO() {
		return SYSTEM_LIMIT_NO;
	}
	public void setSYSTEM_LIMIT_NO(int sYSTEM_LIMIT_NO) {
		SYSTEM_LIMIT_NO = sYSTEM_LIMIT_NO;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public int getNode() {
    return node;
  }

  public void setNode(int node) {
    this.node = node;
  }

  @Override
	public String toString() {
		return "Name:" + name + ", Node : " + node + ", FreeCPU:" + freeCPU + ", TotalCPU: " + totalCPU; 
	}

}
