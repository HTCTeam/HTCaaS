package org.kisti.htc.message;

import java.io.Serializable;

public class MetaDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private int metaJobId = -1;
	private String userId = null;
	private int numSubJob = -1;
	private String app = null;
	private String ce = null;
	
	public int getMetaJobId() {
		return metaJobId;
	}
	public void setMetaJobId(int metaJobId) {
		this.metaJobId = metaJobId;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public int getNumSubJob() {
		return numSubJob;
	}
	public void setNumSubJob(int numSubJob) {
		this.numSubJob = numSubJob;
	}
	
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	
	public String getCe() {
		return ce;
	}
	public void setCe(String ce) {
		this.ce = ce;
	}
	@Override
	public String toString() {
	
		StringBuffer sb = new StringBuffer();		
		sb.append("[MetaJobID] " + metaJobId + "\n");
		sb.append("[UserId] " + userId + "\n");
		sb.append("[NumSubJob] " + numSubJob + "\n");
		sb.append("[Application] " + app + "\n");
		sb.append("[CEs] " + ce + "\n");
		
		return sb.toString();
	}
	
}
