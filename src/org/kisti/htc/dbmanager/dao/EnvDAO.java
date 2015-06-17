package org.kisti.htc.dbmanager.dao;

import java.util.Map;

public interface EnvDAO {
	
	public int createAMEnv(String name) throws Exception;

	public boolean readAMEnvAutoMode(int id) throws Exception;
	
	public int readAMEnvId(String name) throws Exception;
	
	public Map<String, Object> readAMEnv(int id) throws Exception;
	
	public void updateAMEnv(int id, Map<String, Integer> env) throws Exception;
	

}
