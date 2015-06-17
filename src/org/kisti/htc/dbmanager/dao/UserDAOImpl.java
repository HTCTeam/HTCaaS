package org.kisti.htc.dbmanager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kisti.htc.dbmanager.beans.User;

import util.mLogger; 
import util.mLoggerFactory; 



public class UserDAOImpl extends DAOBase implements UserDAO {
  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public UserDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  public static void main(String arg[]) {
    Connection conn = DAOUtil.getConnection();
    try {
      UserDAOImpl userDAO = new UserDAOImpl(conn);
      List<User> aa =userDAO.getUserObjectList();
      for(User user : aa){
        System.out.println(user.toString());
      }
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

  /**
  * <pre>
  * 사용자를 추가함 (dn, name, userid, passwd 등의 정보를 넣는다.)
  * INSERT INTO user (dn, name, userid, passwd, service_Infra_id) VALUES (?, ?, ?, ?, ?)
  * </pre>
  * @return 입력된 사용자의 id 값
  */
  @Override 
  public int createUser(String dn, String name, String userId, String passwd, String infraMetric)
      throws Exception {

    String sql = "INSERT INTO user (dn, name, userid, passwd, service_Infra_id) VALUES (?, ?, ?, ?, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    int id = -1;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, dn);
      stmt.setString(2, name);
      stmt.setString(3, userId);
      stmt.setString(4, passwd);
      stmt.setString(5, infraMetric);
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        id = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return id;
  }

  /**
  * <pre>
  * 주어진 사용자명(string)에 대한 id 값을 구함
  * SELECT id FROM user WHERE userid=?
  * </pre>
  */
  @Override
  public int readUserId(String userId) throws Exception {
    int id = -1;

    // Connection conn = DAOUtil.getConnection();
    String sql = "SELECT id FROM user WHERE userid=?";
    // String sql = "SELECT id FROM user WHERE name=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        id = Integer.parseInt(rs.getString(1));
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
      // DAOUtil.closeJDBCConnection(conn);
    }
    return id;
  }
  
  /**
  * <pre>
  * 사용자의 KeepAgentNO 값을 구함
  * SELECT keepAgentNO FROM user WHERE userid=?
  * </pre>
  */
  @Override
  public int readUserKeepAgentNO(String userId) throws Exception {
    int keepAgentNO = -1;

    String sql = "SELECT keepAgentNO FROM user WHERE userid=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        keepAgentNO = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    return keepAgentNO;
  }

  /**
  * <pre>
  * 사용자의 패스워드 정보를 읽음
  * SELECT passwd FROM user WHERE userid=?
  * TODO: raw password 가 암호화 되지 않고 그대로 db 에 저장됨
  * </pre>
  */
  @Override
  public String readUserPassword(String userId) throws Exception {

    String pw = null;

    String sql = "SELECT passwd FROM user WHERE userid=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        pw = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    return pw;
  }
  
  /**
  * <pre>
  * 사용자의 service_Infra_id 값을 읽음
  * SELECT service_Infra_id FROM user WHERE userid=?
  * </pre>
  */
  @Override
  public String readUserServiceInfra(String userId) throws Exception {

    String serviceInfra = null;

    String sql = "SELECT service_Infra_id FROM user WHERE userid=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        serviceInfra = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    return serviceInfra;
  }

  /**
  * <pre>
  * 주어진 id의 사용자를 삭제
  * DELETE FROM user WHERE id=?
  * </pre>
  */
  @Override
  public void deleteUser(int id) throws Exception {

    String sql = "DELETE FROM user WHERE id=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, id);
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
  }

  /**
  * <pre>
  * 사용자아이디가 같은 레코드이 개수를 카운트
  * SELECT COUNT(*) AS cn FROM user WHERE userid=?
  * </pre>
  */
  @Override
  public boolean checkUser(String userId) throws Exception {

    String sql = "SELECT COUNT(*) AS cn FROM user WHERE userid=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    int a = -1;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        a = rs.getInt(1);
      }
      if (a > 0) {
        return true;
      } else {
        return false;
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
  }

  /**
  * <pre>
  * </pre>
  */
  @Override
  public void setUserPassword(String userId, String pw) throws Exception {

    String sql = "UPDATE user set passwd = ? WHERE userid = ? ";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, pw);
      stmt.setString(2, userId);
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
  }
  
  /**
  * <pre>
  * </pre>
  */
  @Override
  public void updateUserKeepAgentNO(String userId, int keepAgentNO) throws Exception {

    String sql = "UPDATE user set keepAgentNO = ? WHERE userid = ? ";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, keepAgentNO);
      stmt.setString(2, userId);
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  /**
  * <pre>
  * </pre>
  */
  @Override
  public List<String> getUserList() throws Exception {

    List<String> usrList = new ArrayList<String>();

    String sql = "SELECT userid FROM user";

    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      //rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
        usrList.add(rs.getString(1));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return usrList;
  }

  /**
  * <pre>
  * </pre>
  */
  @Override
  public List<User> getUserObjectList() throws Exception {

    List<User> usrList = new ArrayList<User>();
    User usr = null;

    String sql = "SELECT * FROM user";
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = conn.prepareStatement(sql);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
        usr = new User();
        usr.setId(rs.getInt("id"));
        usr.setDN(rs.getString("dn"));
        usr.setUserID(rs.getString("userid"));
        usr.setPw(rs.getString("passwd"));
        usr.setName(rs.getString("name"));
        usr.setServiceInfraID(rs.getString("service_Infra_id"));
        usr.setUsergroupId(rs.getInt("ug_id"));
        usrList.add(usr);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return usrList;
  }

  /**
  * <pre>
  * </pre>
  */
  @Override
  public User getUserInfo(String userId) throws Exception {

    String sql = "SELECT * FROM user WHERE userid=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    User usr = new User();

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        usr.setId(rs.getInt("id"));
        usr.setUserID(rs.getString("userid"));
        usr.setName(rs.getString("name"));
        usr.setServiceInfraID(rs.getString("service_Infra_id"));
        usr.setDN(rs.getString("dn"));
        usr.setPw(rs.getString("passwd"));
        usr.setKeepAgentNo(rs.getInt("KeepAgentNO"));
        usr.setUsergroupId(rs.getInt("ug_id"));
        usr.setOtpflag(rs.getInt("otp_flag"));
        usr.setShared(rs.getBoolean("shared"));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return usr;
  }

  /**
  * <pre>
  * </pre>
  */
  @Override
  public void updateUserInfo(User usr) throws Exception {
    String sql = "UPDATE user set passwd=? , dn=? , service_Infra_id=? , name =? WHERE userid = ? ";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, usr.getPw());
      stmt.setString(2, usr.getDN());
      stmt.setString(3, usr.getServiceInfraID());
      stmt.setString(4, usr.getName());
      stmt.setString(5, usr.getUserID());
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

  }

  /**
   * <pre>
   * </pre>
   */
@Override
public void updateUserOtpFlag(String userId, int otp_flag) throws Exception {
	
	 String sql = "UPDATE user set otp_flag = ? WHERE userid = ? ";
	    PreparedStatement stmt = null;

	    try {
	      stmt = conn.prepareStatement(sql);
	      stmt.setInt(1, otp_flag);
	      stmt.setString(2, userId);
	      // int rows = stmt.executeUpdate();
	      int rows = _update(stmt);

	      if (rows != 1) {
	        throw new SQLException();
	      }

	    } catch (SQLException e) {
	      throw e;
	    } finally {
	      DAOUtil.closeStatement(stmt);
	    }
	
}


}
