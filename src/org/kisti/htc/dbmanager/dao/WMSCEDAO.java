package org.kisti.htc.dbmanager.dao;

import java.util.Set;

public interface WMSCEDAO {
	
	public void createWMSCE(int wmsId, int ceId) throws Exception;
	
	public Set<Integer> readWMSes(int ceId) throws Exception;
	
	public int readWMSCE(int wmsId, int ceId) throws Exception;
	
	public int readSubmitErrorNum(String wmsName, String ceName) throws Exception;
	
	public void updateSubmitErrorNum(String wmsName, String ceName) throws Exception;
	
	public void deleteWMSCE(int wmsId) throws Exception;
	
	public void deleteWMSCE(int wmsId, int ceId) throws Exception;

}
