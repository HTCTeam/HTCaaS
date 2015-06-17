package org.kisti.htc.dbmanager.beans;

public class User {

	// 테이블의 각 필드에 해당
	private int id;
	private String userId;
	private String pw;
	private String dn;
	private String name;
	private String serviceInfraId;
	private int keepAgentno;
	private int otp_flag;
	private int ug_id;
	private boolean shared;
	
	/*
	* mysql> desc user;
	* +-------------------------+--------------+------+-----+---------+----------------+
	* | Field                   | Type         | Null | Key | Default | Extra          |
	* +-------------------------+--------------+------+-----+---------+----------------+
	* | id                      | int(20)      | NO   | PRI | NULL    | auto_increment | 
	* | dn                      | varchar(255) | YES  |     | NULL    |                | 
	* | userid                  | varchar(255) | NO   | UNI |         |                | 
	* | passwd                  | varchar(255) | NO   |     |         |                | 
	* | service_Infra_id |   set('1','2','3','4','5','6','7','8','9','10')     | NO   | MUL | 0       |                | 
	* | name                    | varchar(255) | NO   |     |         |                | 
	* | keepAgentNo | int(11) unsigned  | NO   |      |     |    0    |                |
	* | otp_flag                | int(3)       | YES  |     |    0    |                |
   	* | ug_id                   | int(3)       | No   |     |    0    |                |
   	* | shared                  | bit(1)       | No   |     |    0    |                |
	* +-------------------------+--------------+------+-----+---------+----------------+
	*/	

	public User() {}

	// id int(11)
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
		
	// userid char(255)
	public String getUserID() { return userId; }
	public void setUserID(String userId) { this.userId = userId; }
	
	// passwd char(255)
	public String getPw() { return pw; }
	public void setPw(String pw) { this.pw = pw; } 
	
	// dn char(255)
	public String getDN() { return dn; }
	public void setDN(String dn) { this.dn = dn; } 

	// service_Infra_id set('1','2','3','4','5','6','7','8','9','10')
	public String getServiceInfraID() { return serviceInfraId; } 
	public void setServiceInfraID(String ServiceInfraId) { this.serviceInfraId = ServiceInfraId; }
	
	// name char(255)
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	// keepAgentNo int(11)
	public int getKeepAgentNo() { return keepAgentno; }
	public void setKeepAgentNo(int keepAgentno) { this.keepAgentno = keepAgentno;}
	
	// otp_flag int(3)
	public int getOtpflag() { return otp_flag; }
	public void setOtpflag(int otp_flag) {this.otp_flag = otp_flag;}
	
	// ug_id int(3)
	public int getUsergroupId(){ return ug_id; }
	public void setUsergroupId(int ug_id) {this.ug_id = ug_id;}
	
		
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	// java.lang.Object 클래스의 toString() 를 override 함
	@Override
	public String toString() {
		return "HTCaaS_id:" + id + "\n UserId:" + userId + "\n dn:" + dn + "\n UserName:" + name + "\n Service id:" + serviceInfraId + "\n Group id:" + ug_id + "\n Shared : " + shared; 
	}

}
