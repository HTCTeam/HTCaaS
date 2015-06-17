/**
* The Interface ACManager.
* @author Seoyoung Kim
* @copyright KISTI
* @version 1.1
* 
*/

package org.kisti.htc.acmanager.server;

import java.util.*;

import org.kisti.htc.dbmanager.beans.User;

public interface ACManager {
		
	/**
	 * To check validity of user to login
	 * @param userId id
	 * @param userPw password
	 * @param flag 
	 * @param otp
	 * @return TRUE/FALSE
	 */
	public boolean Login(String userId, String userPw, String flag, String otp);
	
	/**
	 * To check validity of user to login
	 * @param userId id
	 * @param userPw password
	 * @param flag 
	 * @param otp
	 * @return Map<Integer{0||1}, Integer{-1||1||2||3||4||5||6||7||8||9}> 
	 */
	public Map<Integer,Integer> Login2(String userId, String userPw, String flag, String otp);
	
	/**
	 * To check whether the user is registered (on PLSI and/or HTCaaS)   
	 * @param userId
	 * @return flag (-00|00|01|10|11|+10|+11)
	 */
	public String checkUserID(String userId);	
	
	/**
	 * To add user to HTCaaS
	 * @param userId 
	 * @param dn (optional)
	 * @param userPw 
	 * @param name (ex. Gildong Hong) 
	 */
	public boolean HTCaasUserAdd(String dn, String name, String userId, String userPw, String serviceInfra);
	
	/**
	 * To delete user from HTCaaS
	 * @param userId
	 */
	public void HTCaasUserDel(String userId);
	
	/**
	 * To get HTCaaS UserList 
	 * @return List<String> HTCaaS User List
	 */
	public List<User> HTCaasUserList();
	
	/**
	 * To update HTCaaS User info. 
	 * @return TRUE/FALSE
	 */
	public boolean HTCaasUserUpdate(String userId, User user);
	
	/**
	 * To print the specified user's info   
	 * @param userId
	 * @return userinfo
	 */
	public User HTCaasUserInfo(String userId);
	
	/**
	 * To get PLSI cert info
	 * @param userId 
	 * @param userPw 
	 * @return Cert info (id/ name/ date/ serial no)
	 */
	public String getPLSICertInfo(String userId, String userPw);
	
	/**
	 * To check whether the user belongs to PLSI user, or not
	 * @param userId
	 * @return TRUE/FALSE
	 */
	boolean checkPlsiUser(String userId); 
	
	/**
	 * To check the user is valid in PLSI 
	 * @param userId
	 * @param userPw
	 * @return TRUE/FALSE
	 */
	boolean checkPLSIUserValid(String userId, String userPw, String otp);
	
	/**
	 * To check whether the user belongs to HTCaaS user, or not
	 * @param userId
	 * @return TRUE/FALSE
	 */
	boolean checkHTCaasUser(String userId);
	
	/**
	 * To check the user is valid in HTCaaS
	 * @param userId
	 * @param userPw
	 * @return TRUE/FALSE
	 */
	boolean checkHTCaasUserValid(String userId, String userPw);
	
	/**
	 * To check the user's PLSI cert is valid (modified version)
	 * @param data  combination of userid and serial no. and converted message
	 * @return message
	 */
	public String checkPLSICert(String data); 
	
	/**
	 * To get a group id for given user 
	 * @param userId  userid
	 * @return groupId 
	 */
	public int getUserGroup(String userId);

}
