package org.kisti.htc.dbmanager.beans;

import java.io.Serializable;
import java.util.Date;

public class MetaJob implements Serializable {

	private static final long serialVersionUID = 1L;

	public MetaJob() {
	}

	private int id;
	private String JSDL;
	private String status;
	private String user;
	private String app;
	private Date startTimestamp;
	private Date lastUpdateTime;
	private int num;
	private int total;
	private String projectName;
	private String scriptName;
	private String error;
	private String ce;
	
/*
mysql> desc metajob;
+----------------+--------------+------+-----+---------+----------------+
| Field          | Type         | Null | Key | Default | Extra          |
+----------------+--------------+------+-----+---------+----------------+
| id             | int(11)      | NO   | PRI | NULL    | auto_increment | 
| JSDL           | longtext     | YES  |     | NULL    |                | 
| status         | varchar(255) | NO   |     |         |                | 
| user_id        | int(11)      | YES  | MUL | NULL    |                | 
| app_id         | int(11)      | YES  | MUL | NULL    |                | 
| startTimestamp | datetime     | YES  |     | NULL    |                | 
| endTimestamp   | datetime     | YES  |     | NULL    |                | 
| num            | int(11)      | YES  |     | 0       |                | 
| total          | int(11)      | YES  |     | 0       |                | 
+----------------+--------------+------+-----+---------+----------------+
*/

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getJSDL() {
		return JSDL;
	}

	public void setJSDL(String JSDL) {
		this.JSDL = JSDL;
	}

	public Date getStartTimestamp() {
		return startTimestamp;
	}

	public void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	

	public String getCe() {
		return ce;
	}

	public void setCe(String ce) {
		this.ce = ce;
	}

	public String toString() {
		return "=================================================================="
				+"\nID:" + id + "\nStatus:" + status + "\nUser:"
				+ user + "\nApplication:" + app + "\nRemaining Jobs:" + num + "\nTotal Jobs:" + total
				+ "\nStartTimestamp:" + startTimestamp + "\nlastUpdateTime:"
				+ lastUpdateTime
				+ "\nError:" + error
//				+ "\nJSDL:" + JSDL
				+"\n==================================================================" ;
	}
	
	public String toStringJSDL() {
		if(id!=0){
			return "=================================================================="
					+"\nID:" + id + "\nStatus:" + status + "\nUser:"
					+ user + "\nApplication:" + app + "\nRemaining Jobs:" + num + "\nTotal Jobs:" + total
					+ "\nStartTimestamp:" + startTimestamp + "\nlastUpdateTime:"
					+ lastUpdateTime
					+ "\nError:" + error	
					+ "\nJSDL:" + JSDL
					+"\n==================================================================" ;
		} else {
			return "Fault MetaJob ID!";
		}
	}

}
