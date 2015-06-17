package org.kisti.htc.dbmanager.beans;

public class ServiceInfra {

	private int id;
	private String name;
	private String serviceCode;
	private int priority;
	private boolean available;
	private int runningAgentHP;
	private int submittedAgentHP;
	private int newAgentHP;
	
	public ServiceInfra() {}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
		
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	public int getRunningAgentHP() {
		return runningAgentHP;
	}

	public void setRunningAgentHP(int runningAgentHP) {
		this.runningAgentHP = runningAgentHP;
	}

	public int getSubmittedAgentHP() {
		return submittedAgentHP;
	}

	public void setSubmittedAgentHP(int submittedAgentHP) {
		this.submittedAgentHP = submittedAgentHP;
	}

	public int getNewAgentHP() {
		return newAgentHP;
	}

	public void setNewAgentHP(int newAgentHP) {
		this.newAgentHP = newAgentHP;
	}

	@Override
	public String toString() {
		return "id:" + id + " Name:" + name + " ServiceCode:" + serviceCode + " Priority:" + priority + " Available:" + available + " runningAgentHP: " + runningAgentHP; 
	}

}
