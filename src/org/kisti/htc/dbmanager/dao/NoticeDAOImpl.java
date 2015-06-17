package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.mLogger; 
import util.mLoggerFactory; 


public class NoticeDAOImpl extends DAOBase implements NoticeDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public NoticeDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public String readContent(String div, String version) throws Exception {

    String sql = "SELECT content FROM notice WHERE division=? and ver=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    String content = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, div);
      stmt.setString(2, version);
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
    NoticeDAOImpl serverEnvDAO = new NoticeDAOImpl(conn);
    try {
      System.out.println(serverEnvDAO.readContent("CLI", "beta 1.0.1.0"));
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
