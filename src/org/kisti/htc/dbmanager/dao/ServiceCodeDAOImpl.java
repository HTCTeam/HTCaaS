package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.mLogger; 
import util.mLoggerFactory; 


public class ServiceCodeDAOImpl extends DAOBase implements ServiceCodeDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public ServiceCodeDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public int createServiceCode(String name) throws Exception {
    int id = -1;
    String sql = "INSERT INTO service_code (name) VALUES (?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if(rs.next()){
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

  @Override
  public int readServiceCodeId(String name) throws Exception {

    String sql = "SELECT id FROM service_code WHERE name=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    int id = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
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
  

  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    ServiceCodeDAOImpl voDAO = new ServiceCodeDAOImpl(conn);
    try {
      System.out.println(voDAO.readServiceCodeId("Grid"));
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
