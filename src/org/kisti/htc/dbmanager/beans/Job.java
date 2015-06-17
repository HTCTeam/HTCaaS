package org.kisti.htc.dbmanager.beans;

import java.util.Date;
import java.util.List;

public class Job {

	private int id;
	private int seq;
	private String name;
	private String errormsg;
	private String jobDetail;
	private Date lastUpdate;
	private int numResubmit;
	private long runningTime;
	private Date startTimestamp;
	private String status;
	private Integer CEId;
	private int metaJob;
	private Integer agentId;
	private boolean stop;
	private String log;
	private List<String> results;

/*
mysql> desc job;   
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | int(11)      | NO   | PRI | NULL    | auto_increment | 
| seq            | int(11)      | YES  |     | NULL    |                | 
| errormsg       | varchar(255) | YES  |     | NULL    |                | 
| jobDetail      | longtext     | YES  |     | NULL    |                | 
| lastUpdateTime | datetime     | YES  |     | NULL    |                | 
| numResubmit    | int(11)      | NO   |     | 0       |                | 
| runningTime    | bigint(20)   | NO   |     | 0       |                | 
| startTimestamp | datetime     | YES  |     | NULL    |                | 
| status         | varchar(255) | YES  |     | NULL    |                | 
| CE_id          | int(11)      | YES  | MUL | NULL    |                | 
| metajob_id     | int(11)      | YES  | MUL | NULL    |                | 
| agent_id       | int(11)      | YES  | MUL | NULL    |                | 
| stop           | bit(1)       | NO   |     | b'0'    |                | 
| log            | varchar(255) | YES  |     | NULL    |                | 
+----------------+--------------+------+-----+---------+----------------+
*/

	public Job() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMetaJob() {
		return metaJob;
	}

	public void setMetaJob(int metaJob) {
		this.metaJob = metaJob;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrormsg() {
		return errormsg;
	}

	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}

	public List<String> getResults() {
		return results;
	}

	public void setResults(List<String> results) {
		this.results = results;
	}

	public String getJobDetail() {
		return jobDetail;
	}

	public void setJobDetail(String jobDetail) {
		this.jobDetail = jobDetail;
	}

	public Date getLastUpdateTime() {
		return lastUpdate;
	}

	public void setLastUpdateTime(Date time) {
		this.lastUpdate = time;
	}

	public int getNumResubmit() {
		return numResubmit;
	}

	public void setNumResubmit(int numResubmit) {
		this.numResubmit = numResubmit;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	public Integer getCEId() {
		return CEId;
	}

	public void setCEId(Integer CEId) {
		this.CEId = CEId;
	}
	
	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
	
	

	@Override
	public String toString() {
		return "=================================================================="
				+"\nID:" + id + "\nSeq:" + seq + "\nname:" + name + "\nStatus:" + status + "\nError Message:"
				+ errormsg + "\nLast Update Time:" + lastUpdate + "\nJob Details:" 
				+ jobDetail + "\nNumber of Resubmit:" + numResubmit
				+ "\nStart Time:" + startTimestamp + "\nRunning Time(s):"
				+ runningTime
//				+ "\nJSDL:" + JSDL
				+"\n==================================================================" ;
	}

}
