package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import util.mLogger; 
import util.mLoggerFactory; 


public class ServiceInfraMetricDAOImpl extends DAOBase implements ServiceInfraMetricDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public ServiceInfraMetricDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public String readServiceInfraIdSet(int id) throws Exception {

    String sql = "SELECT serviceInfra_id FROM service_infra_metric WHERE id=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    String sid = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, id);
      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        sid = rs.getString(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return sid;
  }
  

  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    ServiceInfraMetricDAOImpl sDAO = new ServiceInfraMetricDAOImpl(conn);
    try {
      System.out.println(sDAO.readServiceInfraIdSet(2));
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
