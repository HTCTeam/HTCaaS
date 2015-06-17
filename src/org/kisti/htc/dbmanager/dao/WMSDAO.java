package org.kisti.htc.dbmanager.dao;

import java.util.List;

import org.kisti.htc.dbmanager.beans.WMS;


public interface WMSDAO {
	
	public void createWMSObject(WMS wms, int voId) throws Exception;
	
	public int readWMSId(String wmsName) throws Exception;
	
	public WMS readWMSObject(String wmsName) throws Exception;
	
	public WMS readWMSObject(int wmsId) throws Exception;
	
	public String readWMSName(int wmsId) throws Exception;
	
	public List<String> readWMSesByVOName(String voName) throws Exception;
	
	public List<String> readWMSesAvailable(int voId, boolean avail) throws Exception;
	
	public void updateWMSAvailable(int wmsId, boolean avail) throws Exception;
	
	public void updateWMSCEInfo(int wmsId, long responseTime, int numCE) throws Exception;
	
	public void updateWMSNumCount(int wmsId, int count) throws Exception;
	
	public void deleteWMS(int wmsId) throws Exception;

}
