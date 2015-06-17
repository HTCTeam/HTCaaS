package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import util.mLogger; 
import util.mLoggerFactory; 


public class WMSCEDAOImpl extends DAOBase implements WMSCEDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public WMSCEDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

/*
mysql> desc wms;
+------------------+--------------+------+-----+---------+----------------+
| Field            | Type         | Null | Key | Default | Extra          |
+------------------+--------------+------+-----+---------+----------------+
| id               | int(11)      | NO   | PRI | NULL    | auto_increment | 
| available        | bit(1)       | NO   |     | NULL    |                | 
| banned           | bit(1)       | NO   |     | NULL    |                | 
| count            | int(11)      | NO   |     | 0       |                | 
| lastUpdateTime   | datetime     | YES  |     | NULL    |                | 
| name             | varchar(255) | YES  |     | NULL    |                | 
| numCE            | int(11)      | NO   |     | 0       |                | 
| responseTime     | bigint(20)   | NO   |     | NULL    |                | 
| service_Infra_id | int(11)      | YES  |     | NULL    |                | 
+------------------+--------------+------+-----+---------+----------------+
*/


/*
mysql> desc wms_ce;
+----------------+---------+------+-----+---------+----------------+
| Field          | Type    | Null | Key | Default | Extra          |
+----------------+---------+------+-----+---------+----------------+
| id             | int(11) | NO   | PRI | NULL    | auto_increment | 
| wms            | int(11) | NO   |     | NULL    |                | 
| ce             | int(11) | NO   |     | NULL    |                | 
| submitErrorNum | int(11) | NO   |     | 0       |                | 
+----------------+---------+------+-----+---------+----------------+
*/

  @Override
  public void createWMSCE(int wmsId, int ceId) throws Exception{

    String sql = "INSERT INTO wms_ce (wms, ce) VALUES (?, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;
    
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, wmsId);
      stmt.setInt(2, ceId);
      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if (rows != 1) {
        throw new SQLException();
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
  }
  
// SELECT id FROM wms_ce WHERE wms=@wmsId and ce=@ceId
  @Override
  public int readWMSCE(int wmsId, int ceId)
      throws Exception {
    String sql = "SELECT id FROM wms_ce WHERE wms=? and ce=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    int id = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, wmsId);
      stmt.setInt(2, ceId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
         id = rs.getInt("id");
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return id;
  }
  
// SELECT wms FROM wms_Ce WHERE ce=@ceId
  @Override
  public Set<Integer> readWMSes(int ceId)
      throws Exception {
    String sql = "SELECT wms FROM wms_ce WHERE ce=?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Set<Integer> wmsSet = new HashSet<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, ceId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
         wmsSet.add(rs.getInt(1));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return wmsSet;
  }
  
// SELECT submitErrorNum FROM wms_ce
// WHERE wms=@wmsId and ce=@ceID
  @Override
  public int readSubmitErrorNum(String wmsName, String ceName)
      throws Exception {
    String sql = "SELECT submitErrorNum FROM wms_ce WHERE wms=(SELECT id FROM WMS WHERE name=?) and ce=(SELECT id from ce where name=?)";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, wmsName);
      stmt.setString(2, ceName);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        num = rs.getInt(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return num;
  }

// UPDATE wms_ce set submitErrorNum=submitErrorNum+1
// WHERE wms=@wmsID and ce=@ceId
  @Override
  public void updateSubmitErrorNum(String wmsName, String ceName)
      throws Exception {
    String sql = "UPDATE wms_ce SET submitErrorNum=submitErrorNum+1 WHERE wms=(SELECT id FROM WMS where name=?) and ce=(SELECT id from ce where name=?)";

    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, wmsName);
      stmt.setString(2, ceName);

      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

// delte FROM wms_Ce WHERE wms=@wmsId
  @Override
  public void deleteWMSCE(int wmsId) throws Exception {
    String sql = "DELETE FROM wms_ce WHERE wms=?";
    
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
  
// delete FROM wms_Ce WHERE wms=@wmsId and ce=@ceId
  @Override
  public void deleteWMSCE(int wmsId, int ceId) throws Exception {
    String sql = "DELETE FROM wms_ce WHERE wms=? and ce=?";
    
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, wmsId);
      stmt.setInt(2, ceId);

      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if(rows != 1){
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  public static void main(String arg[]) {
    
    Connection conn = DAOUtil.getConnection();
    WMSCEDAOImpl wms_ceDAO = new WMSCEDAOImpl(conn);
    try {
      System.out.println(wms_ceDAO.readWMSCE(1, 2));
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }
}
