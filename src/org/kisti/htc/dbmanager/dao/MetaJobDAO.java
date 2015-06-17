package org.kisti.htc.dbmanager.dao;

import java.util.List;
import java.util.Map;

import org.kisti.htc.dbmanager.beans.MetaJob;

public interface MetaJobDAO {
	
	public int createMetaJob(String jsdl, int app_id, String status, int user_id, int aJobTimeMin, String pName, String sName, String ces) throws Exception;
	
	public Map<String,Integer> readMetaJobProgress(int metaJobId) throws Exception;
	
	public String readMetaJobUserID(int metaJobId) throws Exception;
	
	public String readMetaJobJSDL(int metaJobId) throws Exception;
	
	public int readMetaJobNum(int metaJobId) throws Exception;
	
	public int readMetaJobListSubTotal(int startId, int endId) throws Exception;
	
	public MetaJob readMetaJobObject(int metaJobId) throws Exception;
	
	public List<MetaJob> readMetaJobObjects(String userId) throws Exception;
	
	public List<MetaJob> readMetaJobObjects(String userId, int num) throws Exception;
	
	public List<Integer> readMetaJobIdList(int userId) throws Exception;
	
	public String readMetaJobStatus(int metaJobId) throws Exception;
	
	public int readMetaJobAJobTime(int metaJobId) throws Exception;
	
	public MetaJob readMetaJobLastRunningFromUser(String userId) throws Exception;
	
	public void updateMetaJobStatus(int metaJobId, String status) throws Exception;
	
	public void updateAddMetaJobNum(int metaJobId, int num) throws Exception;
	
	public void updateAddMetaJobTotal(int metaJobId, int num) throws Exception;
	
	public void updateMetaJobLastUpdateTime(int metaJobId) throws Exception;
	
	public void updateMetaJobError(int metaJobId, String error) throws Exception;
	
	public boolean deleteMetaJob(int metaJobId) throws Exception;

}
