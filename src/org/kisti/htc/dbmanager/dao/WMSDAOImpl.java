package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kisti.htc.dbmanager.beans.WMS;

import util.mLogger; 
import util.mLoggerFactory; 



public class WMSDAOImpl extends DAOBase implements WMSDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;
  
  public WMSDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public void createWMSObject(WMS wms, int voId) throws Exception{

    String sql = "INSERT INTO wms (available, banned, lastUpdateTime, name, responseTime, service_Infra_id) VALUES (?, ?, now(), ?, -1, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setBoolean(1, wms.isAvailable());
      stmt.setBoolean(2, wms.isBanned());
      stmt.setString(3, wms.getName());
      stmt.setInt(4, voId);
      
      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if(rows != 1){
        throw new SQLException();
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
  }

  @Override
  public int readWMSId(String wmsName) throws Exception {
    
    String sql = "SELECT id FROM wms WHERE name=?";
        
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, wmsName);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if(rs.next()){
        num = rs.getInt(1);
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return num;
  }
  
  @Override
  public WMS readWMSObject(String wmsName) throws Exception {
    
    String sql = "SELECT * FROM wms WHERE name=?";
        
    PreparedStatement stmt = null;
    ResultSet rs = null;
    WMS wms = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, wmsName);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if(rs.next()){
        wms = new WMS();
        wms.setId(rs.getInt("id"));
        wms.setAvailable(rs.getBoolean("available"));
        wms.setBanned(rs.getBoolean("banned"));
        wms.setCount(rs.getInt("count"));
        wms.setLastUpdateTime(rs.getDate("lastUpdateTime"));
        wms.setName(rs.getString("name"));
        wms.setNumCE(rs.getInt("numCE"));
        wms.setResponseTime(rs.getLong("responseTime"));
        wms.setServiceInfra(rs.getInt("service_Infra_id"));
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return wms;
  }
  
  @Override
  public WMS readWMSObject(int wmsId) throws Exception {
    
    String sql = "SELECT * FROM wms WHERE id=?";
        
    PreparedStatement stmt = null;
    ResultSet rs = null;
    WMS wms = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, wmsId);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if(rs.next()){
        wms = new WMS();
        wms.setId(rs.getInt("id"));
        wms.setAvailable(rs.getBoolean("available"));
        wms.setBanned(rs.getBoolean("banned"));
        wms.setCount(rs.getInt("count"));
        wms.setLastUpdateTime(rs.getDate("lastUpdateTime"));
        wms.setName(rs.getString("name"));
        wms.setNumCE(rs.getInt("numCE"));
        wms.setResponseTime(rs.getLong("responseTime"));
        wms.setServiceInfra(rs.getInt("service_Infra_id"));
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return wms;
  }

  @Override
  public String readWMSName(int wmsId) throws Exception {
    
    String sql = "SELECT name FROM wms WHERE id=?";
        
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String name = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, wmsId);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if(rs.next()){
        name = rs.getString(1);
      }
    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return name;
  }
  
  @Override
  public List<String> readWMSesByVOName(String name) throws Exception {
    
    String sql = "SELECT name FROM wms WHERE service_Infra_id=(SELECT id FROM service_infra WHERE name=?)";
        
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<String> wmses = new ArrayList<String>();
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while(rs.next()){
        wmses.add(rs.getString(1));
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return wmses; 
  }
  
  @Override
  public List<String> readWMSesAvailable(int voId, boolean avail) throws Exception {
    
    String sql = "SELECT name FROM wms WHERE service_Infra_id=? and available=?";
        
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<String> wmses = new ArrayList<String>();
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, voId);
      stmt.setBoolean(2, avail);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while(rs.next()){
        wmses.add(rs.getString(1));
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return wmses; 
  }
  
  @Override
  public void updateWMSAvailable(int wmsId, boolean avail) throws Exception {
    
    String sql = "UPDATE wms SET available = ?, lastUpdateTime=now() WHERE id = ?";
    PreparedStatement stmt = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setBoolean(1, avail);
      stmt.setInt(2, wmsId);
      
      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if(rows!=1){
        throw new SQLException();
      }
    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
    }
  }
  
  @Override
  public void updateWMSCEInfo(int wmsId, long responseTime, int numCE) throws Exception {
    
    String sql = "UPDATE wms SET responseTime = ?, numCE = ?, lastUpdateTime=now() WHERE id = ?";
        
    PreparedStatement stmt = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setLong(1, responseTime);
      stmt.setInt(2, numCE);
      stmt.setInt(3, wmsId);
      
      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if(rows!=1){
        throw new SQLException();
      }
    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
    }
  }
  
  @Override
  public void updateWMSNumCount(int wmsId, int count) throws Exception {
    
    String sql = "UPDATE wms SET count=count+? WHERE id = ?";
        
    PreparedStatement stmt = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, count);
      stmt.setInt(2, wmsId);
      
      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if(rows!=1){
        throw new SQLException();
      }
    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
    }
  }
  
  @Override
  public void deleteWMS(int wmsId) throws Exception {
    String sql = "DELETE FROM wms WHERE id=?";
    
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, wmsId);

      //stmt.executeUpdate();
      _update(stmt);

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }
  
  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    WMSDAOImpl wmsDAO = new WMSDAOImpl(conn);
    try {
      WMS wms = new WMS();
      wmsDAO.updateWMSNumCount(1, 2);
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }
}
