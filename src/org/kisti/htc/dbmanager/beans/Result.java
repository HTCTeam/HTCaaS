package org.kisti.htc.dbmanager.beans;

public class Result {

	private int id;
	private String LFN;
	private int metajobId;
	private int jobId;
	
/*
ysql> desc result;
+--------+--------------+------+-----+---------+----------------+
| Field  | Type         | Null | Key | Default | Extra          |
+--------+--------------+------+-----+---------+----------------+
| id     | int(11)      | NO   | PRI | NULL    | auto_increment | 
| LFN    | varchar(255) | YES  |     | NULL    |                | 
| job_id | int(11)      | YES  |     | NULL    |                | 
+--------+--------------+------+-----+---------+----------------+
*/

	public Result() {}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public int getMetajobId() {
		return metajobId;
	}
	public void setMetajobId(int metajobId) {
		this.metajobId = metajobId;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String getLFN() { return LFN; }
	public void setLFN(String LFN) { this.LFN = LFN; }
	
	@Override
	public String toString() {
		return "id:" + id + " LFN:" + LFN;	
	}
	
}

