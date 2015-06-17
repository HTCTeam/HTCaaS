package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.mLogger; 
import util.mLoggerFactory; 


public class ServerEnvDAOImpl extends DAOBase implements ServerEnvDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public ServerEnvDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public String readValue(String name) throws Exception {

    String sql = "SELECT value FROM Server_Env WHERE name=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    String value = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        value = rs.getString(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return value;
  }
  
  @Override
  public String readContent(String name) throws Exception {

    String sql = "SELECT content FROM Server_Env WHERE name=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    String content = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        content = rs.getString(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return content;
  }
  

  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    ServerEnvDAOImpl serverEnvDAO = new ServerEnvDAOImpl(conn);
    try {
      System.out.println(serverEnvDAO.readValue("CLI_Version"));
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
