package org.kisti.htc.message;

import java.io.Serializable;
import java.util.List;

// TODO: rename 'DTO' to 'JobDTO' 클래스 이름을 변경하는게 좋겠음  ActiveMQ 를 통해 전송되는 자료 클래스

public class DTO implements Serializable {

	private int metaJobId = -1;
	private int jobId = -1;
	private String userId;
	private String appName;

	private String executable;
	private List<String> arguments;
	private List<String> inputFiles;
	private List<String> outputFiles;
	
	
	public int getMetaJobId() {
		return metaJobId;
	}
	public void setMetaJobId(int metaJobId) {
		this.metaJobId = metaJobId;
	}
	public int getJobId() {
		return jobId;
	}
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
//	public String getProjectName() {
//		return projectName;
//	}
//	public void setProjectName(String projectName) {
//		this.projectName = projectName;
//	}
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getExecutable() {
		return executable;
	}
	public void setExecutable(String executable) {
		this.executable = executable;
	}
	
	public List<String> getArguments() {
		return arguments;
	}
	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
	
//	public List<String> getEnvironments() {
//		return environments;
//	}
//	public void setEnvironments(List<String> environments) {
//		this.environments = environments;
//	}
	
	public List<String> getInputFiles() {
		return inputFiles;
	}
	public void setInputFiles(List<String> inputFiles) {
		this.inputFiles = inputFiles;
	}
	
	public List<String> getOutputFiles() {
		return outputFiles;
	}
	public void setOutputFiles(List<String> outputFiles) {
		this.outputFiles = outputFiles;
	}
	
	@Override
	public String toString() {
	
		StringBuffer sb = new StringBuffer();
		
		sb.append("[MetaJobID] " + metaJobId + "\n");
		sb.append("[JobID] " + jobId + "\n");
		sb.append("[UserID] " + userId + "\n");
//		sb.append("[ProjectName] " + projectName + "\n");
		sb.append("[AppName] " + appName + "\n");
		sb.append("[Executable] " + executable + "\n");	
		
		for (String arg : arguments) {
			sb.append("[Arguments]: " + arg + "\n");
		}
		
//		for (String env : environments) {
//			sb.append("[Environment]: " + env + "\n");
//		}

		for (String input : inputFiles) {
			sb.append("[InputFiles]: " + input + "\n");
		}
		
		for (String output : outputFiles) {
			sb.append("[OutputFiles]: " + output + "\n");
		}
		
		return sb.toString();
	}
	
}
