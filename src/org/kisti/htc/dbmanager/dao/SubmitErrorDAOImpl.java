package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.mLogger; 
import util.mLoggerFactory; 


public class SubmitErrorDAOImpl extends DAOBase implements SubmitErrorDAO {
  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;
  
  public SubmitErrorDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public void insertSubmitError(Integer metaJobId, String ceName, String wmsName, String errorMsg) throws Exception{
    
    String sql = "INSERT INTO submiterror (metajob_id, ceName, wmsName, errorMsg) VALUES (?, ?, ?, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setObject(1, metaJobId);
      stmt.setString(2, ceName);
      stmt.setString(3, wmsName);
      stmt.setString(4, errorMsg);
      // int rows = stmt.executeUpdate();
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
  
  public static void main(String arg[]){
    Connection conn = DAOUtil.getConnection();
    SubmitErrorDAOImpl submitErrorDAO = new SubmitErrorDAOImpl(conn);
    try {
      submitErrorDAO.insertSubmitError(100, "test", null, "test");
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch blocks
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }
    
}
