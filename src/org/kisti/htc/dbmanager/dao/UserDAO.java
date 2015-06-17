package org.kisti.htc.dbmanager.dao;

import java.util.List;
import org.kisti.htc.dbmanager.beans.User;


// UserDAOImpl
public interface UserDAO {
  
  public int createUser(String dn, String name, String userId, String passwd, String infraMetric) throws Exception;
  
  public int readUserId(String userId) throws Exception;
  
  public int readUserKeepAgentNO(String userId) throws Exception;
  
  public String readUserPassword(String userId) throws Exception;
  
  public String readUserServiceInfra(String userId) throws Exception;
  
  public void setUserPassword(String userId, String pw) throws Exception;
  
  public void updateUserKeepAgentNO(String userId, int keepAgentNO) throws Exception;
  
  public void deleteUser(int userId) throws Exception;

  public boolean checkUser(String userId) throws Exception;
  
  public List<String> getUserList() throws Exception;
  
  public List<User> getUserObjectList() throws Exception;
  
  public User getUserInfo(String userId) throws Exception;
  
  public void updateUserInfo(User usr) throws Exception;
  
  public void updateUserOtpFlag(String userId, int otp_flag) throws Exception;

}
