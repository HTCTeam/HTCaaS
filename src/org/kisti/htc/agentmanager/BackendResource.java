package org.kisti.htc.agentmanager;


abstract class BackendResource {
	
	String type;
	String name;
	int jobNum;
	int maxJobNum;
	int priority;
	String servicecode;
	int id;
	
	boolean enabledForAM = false;
	boolean enabledForDB = false;
	
	
	// constructor
	public BackendResource() {
    //set_logger_prefix("[BackendResource] ");  // debug message prefix
	}
	
	// constructor
	public BackendResource(String name) {
		this();
		this.name = name;
	}
	
	abstract void updateCEInfo() throws Exception;
	
	abstract int getCEList(int CE_SELECTION_METRIC);
	
	abstract void cancelZombieJob();
	
	// type
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	// name
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	// jobNum
	public int getJobNum() {
		return jobNum;
	}
	public void setJobNum(int jobNum) {
		this.jobNum = jobNum;
	}
	
	// maxJobNum
	public int getMaxJobNum() {
		return maxJobNum;
	}
	public void setMaxJobNum(int maxJobNum) {
		this.maxJobNum = maxJobNum;
	}
	
	// priority
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean available() {
		return (jobNum < maxJobNum);
	}

	public String getServicecode() {
		return servicecode;
	}
	
	public void setServicecode(String servicecode) {
		this.servicecode = servicecode;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isEnabledForAM() {
		return enabledForAM;
	}
	
	public void setEnabledForAM(boolean enabledForAM) {
		this.enabledForAM = enabledForAM;
	}
	
	public boolean isEnabledForDB() {
		return enabledForDB;
	}
	
	public void setEnabledForDB(boolean enabledForDB) {
		this.enabledForDB = enabledForDB;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.name.equals(((BackendResource)o).getName());
	}
	

	@Override
	public String toString() {
		return "[" + name + "] type=" + type + " ID=" + id + " priority=" + priority + " maxJobNum=" + maxJobNum + " jobNum=" + jobNum + " enabledForAM=" + enabledForAM + " enabledForDB=" + enabledForDB ; 
	}
	

}
