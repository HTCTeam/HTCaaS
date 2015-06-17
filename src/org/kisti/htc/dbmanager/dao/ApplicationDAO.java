package org.kisti.htc.dbmanager.dao;

public interface ApplicationDAO {
	
	public int createApplication(String name) throws Exception;
	
	public int readAppId(String name) throws Exception;

}
