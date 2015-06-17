package org.kisti.htc.dbmanager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

import util.mLogger; 
import util.mLoggerFactory; 


public class GFSDAOImpl extends DAOBase implements GFSDAO {
  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public GFSDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }


/*
* public static void main(String arg[]) {
*   Connection conn = DAOUtil.getPLSIConnection();
*   try {
*     GFSDAOImpl GFSDAO = new GFSDAOImpl(conn);
*     System.out.println(GFSDAO.readGFS_PATH("pwork01"));
*     DAOUtil.doCommit(conn);
*   } catch (Exception e) {
*     // TODO Auto-generated catch block
*     e.printStackTrace();
*     DAOUtil.doRollback(conn);
*   }
* }
*/

  @Override
  public String readGFS_PATH(String GFS_NAME) throws Exception {
    String path = null;

    String sql = "SELECT GFS_PATH FROM sc_gfs WHERE GFS_NAME=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, GFS_NAME);

      rs = stmt.executeQuery();

      if (rs.next()) {
        path = rs.getString(1);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    return path;
  }

}
