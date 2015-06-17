package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import util.mLogger; 
import util.mLoggerFactory; 

public class ResultDAOImpl extends DAOBase implements ResultDAO {
  
  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;
  
  public ResultDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public int createResult(int jobId, int metaJobId, String LFN) throws Exception{
    
    String sql = "INSERT INTO result (LFN, job_id, metajob_id) VALUES (?, ?, ?)";
    PreparedStatement stmt = null;
    
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, LFN);
      stmt.setInt(2, jobId);
      stmt.setInt(3, metaJobId);
      
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if(rows != 1){
        throw new SQLException();
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
    }
    
    return jobId;
  }

  @Override
  public List<String> readLFN(int jobId) throws Exception{
    
    String sql = "SELECT LFN FROM result WHERE job_id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<String> lfn = null;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      lfn = new ArrayList<String>();
      while(rs.next()){
        lfn.add(rs.getString(1));
      }
    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return lfn;
  }
  
  @Override
  public boolean deleteResults(int metaJobId) throws Exception {
    String sql ="DELETE FROM result WHERE metajob_id = ?";
    PreparedStatement stmt = null;
    boolean result = false;
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if(rows > 0){
        result = true;
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
    }
    
    return result;
  }
  
  public static void main(String arg[]){
    Connection conn = DAOUtil.getConnection();
    ResultDAOImpl result = new ResultDAOImpl(conn);
    try {
      System.out.println(result.readLFN(1).toString());
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }
}


