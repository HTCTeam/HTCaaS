package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kisti.htc.dbmanager.beans.ServiceInfra;

import util.mLogger;
import util.mLoggerFactory;


public class ServiceInfraDAOImpl extends DAOBase implements ServiceInfraDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public ServiceInfraDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public int createServiceInfra(String name, int serviceCode, int priority, boolean available) throws Exception {
    int id = -1;
    String sql = "INSERT INTO service_infra (name, service_Code_id, priority, available) VALUES (?, ?, ?, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      stmt.setInt(2, serviceCode);
      stmt.setInt(3, priority);
      stmt.setBoolean(4, available);
      
      int rows = _update(stmt);
      //int rows = stmt.executeUpdate();

      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);
      rs = _query(stmt);
      //rs = stmt.executeQuery();
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
  public List<ServiceInfra> readServiceInfraObjects() throws Exception {

//    String sql = "SELECT * FROM service_infra";
    String sql = "SELECT s.id,s.name,c.service,s.priority, s.available, s.runningAgentHP, s.submittedAgentHP, s.newAgentHP FROM service_infra s, service_code c  where s.Service_Code_id=c.id";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    List<ServiceInfra> sList = new ArrayList<ServiceInfra>();
    
    try {
      stmt = conn.prepareStatement(sql);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
    	  
    	  ServiceInfra si = new ServiceInfra();

    	  si.setId(rs.getInt(1));
    	  si.setName(rs.getString(2));
    	  si.setServiceCode(rs.getString(3));
    	  si.setPriority(rs.getInt(4));
    	  si.setAvailable(rs.getBoolean(5));
    	  si.setRunningAgentHP(rs.getInt(6));
    	  si.setSubmittedAgentHP(rs.getInt(7));
    	  si.setNewAgentHP(rs.getInt(8));
    	  
    	  sList.add(si);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return sList;
  }
  
  @Override
  public int readServiceInfraId(String name) throws Exception {

    String sql = "SELECT id FROM service_infra WHERE name=?";

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
  
  @Override
  public ServiceInfra readServiceInfra(String name) throws Exception {

//    String sql = "SELECT id,name,service_Code_id, priority, available FROM service_infra WHERE name=?";
	  String sql = "SELECT s.id,s.name,c.service,s.priority, s.available FROM service_infra s, service_code c  where s.Service_Code_id=c.id and s.name=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    ServiceInfra si  = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        si = new ServiceInfra();
        si.setId(rs.getInt(1));
        si.setName(rs.getString(2));
        si.setServiceCode((rs.getString(3)));
        si.setPriority(rs.getInt(4));
        si.setAvailable(rs.getBoolean(5));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return si;
  }
  
  @Override
  public ServiceInfra readServiceInfra(int id) throws Exception {

//    String sql = "SELECT id,name,service_Code_id, priority, available FROM service_infra WHERE id=?";
    String sql = "SELECT s.id,s.name,c.service,s.priority, s.available FROM service_infra s, service_code c  where s.Service_Code_id=c.id and s.id=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    ServiceInfra si  = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, id);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        si = new ServiceInfra();
        si.setId(rs.getInt(1));
        si.setName(rs.getString(2));
        si.setServiceCode((rs.getString(3)));
        si.setPriority(rs.getInt(4));
        si.setAvailable(rs.getBoolean(5));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return si;
  }
  
  @Override
  public boolean readServiceInfraAvail(String name) throws Exception {

    String sql = "SELECT available FROM service_infra WHERE name=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    boolean avail  = false;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        avail = rs.getBoolean(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return avail;
  }

  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    ServiceInfraDAOImpl voDAO = new ServiceInfraDAOImpl(conn);
    try {
//      System.out.println(voDAO.readServiceInfraId("biomed"));
      System.out.println(voDAO.readServiceInfra(1));
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
