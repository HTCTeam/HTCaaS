package org.kisti.htc.dbmanager.beans;

import java.util.Date;
import java.util.Set;

public class WMS {

/*
mysql> desc wms;
+------------------+--------------+------+-----+---------+----------------+
| Field            | Type         | Null | Key | Default | Extra          |
+------------------+--------------+------+-----+---------+----------------+
| id               | int(11)      | NO   | PRI | NULL    | auto_increment | 
| available        | bit(1)       | NO   |     | NULL    |                | 
| banned           | bit(1)       | NO   |     | NULL    |                | 
| count            | int(11)      | NO   |     | 0       |                | 
| lastUpdateTime   | datetime     | YES  |     | NULL    |                | 
| name             | varchar(255) | YES  |     | NULL    |                | 
| numCE            | int(11)      | NO   |     | 0       |                | 
| responseTime     | bigint(20)   | NO   |     | NULL    |                | 
| service_Infra_id | int(11)      | YES  |     | NULL    |                | 
+------------------+--------------+------+-----+---------+----------------+
*/

	private int id;
	private boolean available;
	private boolean banned;
	private int count;
	private Date lastUpdateTime;
	private String name;
	private int numCE;
	private long responseTime;

	
	private Set<CE> CEs;
	private int serviceInfra;
	
	public WMS() {}
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public Date getLastUpdateTime() { return lastUpdateTime; }
	public void setLastUpdateTime(Date time) { this.lastUpdateTime = time; }
	
	public long getResponseTime() { return responseTime; }
	public void setResponseTime(long responseTime) { this.responseTime = responseTime; }

	public int getNumCE() { return numCE; } 
	public void setNumCE(int numCE) { this.numCE = numCE; }
	
	public Set<CE> getCEs() { return CEs; }
	public void setCEs(Set<CE> CEs) { this.CEs = CEs; }
	
	public boolean isAvailable() { return available; }
	public void setAvailable(boolean available) { this.available = available; }
	
	public boolean isBanned() {	return banned; }
	public void setBanned(boolean banned) { this.banned = banned; }
	
	public int getCount() { return count; }
	public void setCount(int count) { this.count = count; }
		
	public int getServiceInfra() { return serviceInfra; }
	public void setServiceInfra(int serviceInfra) { this.serviceInfra = serviceInfra; }
	
	@Override
	public String toString() {
		return "id:" + id + " Name:" + name + " lastUpdateTime:" + lastUpdateTime 
			+ " responseTime:" + responseTime + " numCE:" + numCE + " available:" + available;
	}

}
