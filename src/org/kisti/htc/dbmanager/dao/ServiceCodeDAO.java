package org.kisti.htc.dbmanager.dao;

public interface ServiceCodeDAO {
	
	public int createServiceCode(String name) throws Exception;
	
	public int readServiceCodeId(String name) throws Exception;
	
}
