package org.kisti.htc.dbmanager.dao;

import java.util.List;

import org.kisti.htc.dbmanager.beans.ServiceInfra;

public interface ServiceInfraDAO {
	
	public int createServiceInfra(String name, int serviceCode, int priority, boolean available) throws Exception;
	
	public List<ServiceInfra> readServiceInfraObjects() throws Exception;
	
	public int readServiceInfraId(String name) throws Exception;
	
	public ServiceInfra readServiceInfra(String name) throws Exception;
	
	public ServiceInfra readServiceInfra(int id) throws Exception;
	
	public boolean readServiceInfraAvail(String name) throws Exception;
	
}
