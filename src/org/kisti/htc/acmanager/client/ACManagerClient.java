package org.kisti.htc.acmanager.client;


public interface ACManagerClient {

    
	boolean checkPLSICert(String userId, String userPw, String cert, String certPw); 
	
	boolean HTCaasLogin(String userId, String userPw, String cert, String certPw); 
	
	boolean HTCaasLoginNew(String userId, String userPw, String OTP);

}
