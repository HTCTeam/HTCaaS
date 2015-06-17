package org.kisti.htc.dbmanager.dao;

public interface ServerEnvDAO {
	
	public String readValue(String name) throws Exception;
	
	public String readContent(String name) throws Exception;
	
}
