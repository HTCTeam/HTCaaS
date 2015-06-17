package org.kisti.htc.dbmanager.dao;

import java.util.List;

public interface ResultDAO {

	public int createResult(int jobId, int metaJobId, String LFN) throws Exception;
	
	public List<String> readLFN(int jobId) throws Exception;
	
	public boolean deleteResults(int metaJobId) throws Exception;
	
}
