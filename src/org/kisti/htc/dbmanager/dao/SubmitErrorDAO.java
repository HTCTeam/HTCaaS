package org.kisti.htc.dbmanager.dao;

public interface SubmitErrorDAO {
	
	public void insertSubmitError(Integer metaJobId, String ceName, String wmsName, String errorMsg) throws Exception;

}
