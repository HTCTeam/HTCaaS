package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.mLogger; 
import util.mLoggerFactory; 

public class ApplicationDAOImpl extends DAOBase implements ApplicationDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  
  private Connection conn;
  
  public ApplicationDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //logger.set_prefix("[" + this.getClass().getSimpleName() +"] "); // logger message prefix
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

/*
* public static void main(String arg[]){
*   Connection conn = DAOUtil.getConnection();
*   ApplicationDAOImpl userDAO = new ApplicationDAOImpl(conn);
*   try {
*     System.out.println(userDAO.createApplication("test"));
*     DAOUtil.doCommit(conn);
*   } catch (Exception e) {
*     // TODO Auto-generated catch block
*     DAOUtil.doRollback(conn);
*   }
* }
*/
  
  @Override
  public int createApplication(String name) throws Exception{
    int id = -1;

    String sql = "INSERT INTO application (name) VALUES (?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    
    try{
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);

      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if(rows != 1){
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);

      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        id = rs.getInt(1);
      }
    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return id;
  }
  
  @Override
  public int readAppId(String name) throws Exception {
  
    int id = -1;

    String sql = "SELECT id FROM application WHERE name=?";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      
      while(rs.next()){
        id = Integer.parseInt(rs.getString("id"));
      }
    } catch (SQLException e) {
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    return id;
  }
}
