package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kisti.htc.dbmanager.beans.MetaJob;

import util.mLogger; 
import util.mLoggerFactory; 


public class MetaJobDAOImpl extends DAOBase implements MetaJobDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  public MetaJobDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public int createMetaJob(String jsdl, int app_id, String status, int user_id, int aJobTimeMin, String pName, String sName, String ces) throws Exception {
    int id = -1;

    String sql = "INSERT INTO metajob (JSDL, app_id, startTimestamp, status, user_id, aJobTime, project_name, script_name, ces) VALUES (?, ?, now(), ?, ?, ?, ?, ?, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, jsdl);
      stmt.setInt(2, app_id);
      stmt.setString(3, status);
      stmt.setInt(4, user_id);
      stmt.setInt(5, aJobTimeMin);
      stmt.setString(6, pName);
      stmt.setString(7, sName);
      stmt.setString(8, ces);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
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
  public Map<String, Integer> readMetaJobProgress(int metaJobId) throws Exception {

    String sql = "SELECT j.status, COUNT(1) as number FROM job j, metajob m WHERE j.metajob_id = m.id AND j.metajob_id = ? GROUP BY j.status";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        stats.put(rs.getString(1), rs.getInt(2));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return stats;
  }

  @Override
  public String readMetaJobUserID(int metaJobId) throws Exception {

    String sql = "SELECT userid FROM user WHERE id=(SELECT user_id FROM metajob WHERE id=?)";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String userid = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        userid = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return userid;
  }

  @Override
  public String readMetaJobJSDL(int metaJobId) throws Exception {

    String sql = "SELECT JSDL FROM metajob WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String jsdl = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        jsdl = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return jsdl;
  }

  @Override
  public int readMetaJobNum(int metaJobId) throws Exception {

    String sql = "SELECT num FROM metajob WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
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
  
  @Override
  public int readMetaJobListSubTotal(int startId, int endId ) throws Exception {

    String sql = "SELECT SUM(mj.total) as total from metajob AS mj, user AS us, application AS ap WHERE mj.id >=? AND mj.id <=? AND us.id =mj.user_id AND ap.id= mj.app_id";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, startId);
      stmt.setInt(2, endId);
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

  @Override
  public MetaJob readMetaJobObject(int metaJobId) throws Exception {

    String sql = "SELECT mj.id, mj.JSDL, mj.status, us.userid, ap.name, mj.startTimestamp, mj.lastUpdateTime, mj.num, mj.total, mj.project_name, mj.script_name, mj.error FROM metajob AS mj, user AS us, application AS ap WHERE mj.id=? AND us.id=mj.user_id AND ap.id=mj.app_id";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    MetaJob metaJob = new MetaJob();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        metaJob.setId(rs.getInt(1));
        metaJob.setJSDL(rs.getString(2));
        metaJob.setStatus(rs.getString(3));
        metaJob.setUser(rs.getString(4));
        metaJob.setApp(rs.getString(5));
        metaJob.setStartTimestamp(rs.getTimestamp(6));
        metaJob.setLastUpdateTime(rs.getTimestamp(7));
        metaJob.setNum(rs.getInt(8));
        metaJob.setTotal(rs.getInt(9));
        metaJob.setProjectName(rs.getString(10));
        metaJob.setScriptName(rs.getString(11));
        metaJob.setError(rs.getString(12));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return metaJob;
  }

  @Override
  public List<MetaJob> readMetaJobObjects(String user) throws Exception {

    String sql = "SELECT mj.id, mj.JSDL, mj.status, ap.name, mj.startTimestamp, mj.lastUpdateTime, mj.num, mj.total, mj.project_name, mj.script_name, mj.error  "
        + "FROM metajob AS mj, user AS us, application AS ap WHERE us.userid=? AND us.id=mj.user_id AND ap.id=mj.app_id ORDER BY mj.id";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<MetaJob> list = new ArrayList<MetaJob>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, user);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        MetaJob metaJob = new MetaJob();
        metaJob.setId(rs.getInt(1));
        metaJob.setJSDL(rs.getString(2));
        metaJob.setStatus(rs.getString(3));
        metaJob.setUser(user);
        metaJob.setApp(rs.getString(4));
        metaJob.setStartTimestamp(rs.getTimestamp(5));
        metaJob.setLastUpdateTime(rs.getTimestamp(6));
        metaJob.setNum(rs.getInt(7));
        metaJob.setTotal(rs.getInt(8));
        metaJob.setProjectName(rs.getString(9));
        metaJob.setScriptName(rs.getString(10));
        metaJob.setError(rs.getString(11));

        list.add(metaJob);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return list;
  }
  
  @Override
  public List<MetaJob> readMetaJobObjects(String user, int num) throws Exception {

    String sql = "SELECT * FROM (SELECT mj.id, mj.JSDL, mj.status, ap.name, mj.startTimestamp, mj.lastUpdateTime, mj.num, mj.total, mj.project_name, mj.script_name, mj.error  "
        + "FROM metajob AS mj, user AS us, application AS ap WHERE us.userid=? AND us.id=mj.user_id AND ap.id=mj.app_id ORDER BY mj.id DESC LIMIT ?) A ORDER BY id ASC";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<MetaJob> list = new ArrayList<MetaJob>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, user);
      stmt.setInt(2, num);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        MetaJob metaJob = new MetaJob();
        metaJob.setId(rs.getInt(1));
        metaJob.setJSDL(rs.getString(2));
        metaJob.setStatus(rs.getString(3));
        metaJob.setUser(user);
        metaJob.setApp(rs.getString(4));
        metaJob.setStartTimestamp(rs.getTimestamp(5));
        metaJob.setLastUpdateTime(rs.getTimestamp(6));
        metaJob.setNum(rs.getInt(7));
        metaJob.setTotal(rs.getInt(8));
        metaJob.setProjectName(rs.getString(9));
        metaJob.setScriptName(rs.getString(10));
        metaJob.setError(rs.getString(11));

        list.add(metaJob);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return list;
  }

  @Override
  public List<Integer> readMetaJobIdList(int userId) throws Exception {

    String sql = "SELECT id FROM metajob WHERE user_id = ?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = new ArrayList<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, userId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        list.add(rs.getInt(1));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return list;
  }


  @Override
  public String readMetaJobStatus(int metaJobId) throws Exception {

    String sql = "SELECT status FROM metajob WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String status = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        status = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return status;
  }

  @Override
  public int readMetaJobAJobTime(int metaJobId) throws Exception {

    String sql = "SELECT aJobTime FROM metajob WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int aJobTime = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        aJobTime = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return aJobTime;
  }
  
  @Override
  public MetaJob readMetaJobLastRunningFromUser(String userId) throws Exception {

    String sql = "SELECT metajob.id, metajob.ces, application.name FROM metajob, application WHERE metajob.app_id=application.id AND metajob.id = (SELECT MAX(id) FROM metajob WHERE user_id=(SELECT id from user where userid=?) AND (status='splitting' or status='split'))";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    MetaJob mJob = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
    	  mJob = new MetaJob();
        mJob.setId(rs.getInt(1));
        mJob.setCe((rs.getString(2)));
        mJob.setApp(rs.getString(3));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return mJob;
  }

  @Override
  public void updateMetaJobStatus(int metaJobId, String status) throws Exception {

    String sql = "UPDATE metajob SET status = ?, lastUpdateTime=now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, metaJobId);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public void updateAddMetaJobNum(int metaJobId, int num) throws Exception {

    String sql = "UPDATE metajob SET num = num+(?), lastUpdateTime=now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, num);
      stmt.setInt(2, metaJobId);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public void updateAddMetaJobTotal(int metaJobId, int num) throws Exception {

    String sql = "UPDATE metajob SET total = total+(?), lastUpdateTime=now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, num);
      stmt.setInt(2, metaJobId);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public void updateMetaJobLastUpdateTime(int metaJobId) throws Exception {

    String sql = "UPDATE metajob SET lastUpdateTime = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public void updateMetaJobError(int metaJobId, String error) throws Exception {

    String sql = "UPDATE metajob SET error = ? WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, error);
      stmt.setInt(2, metaJobId);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows != 1) {
        throw new SQLException();
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public boolean deleteMetaJob(int metaJobId) throws Exception {

    String sql = "DELETE FROM metajob WHERE id = ?";
    PreparedStatement stmt = null;
    boolean result = false;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);

      int rows = _update(stmt);
//      int rows = stmt.executeUpdate();
      if (rows > 0) {
        result = true;
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }

    return result;
  }

  public static void main(String arg[]) {
    Connection conn = DAOUtil.getConnection();
    MetaJobDAOImpl metaJob = new MetaJobDAOImpl(conn);
    try {
    	
//      List<MetaJob> a = metaJob.readMetaJobObjects("p258rsw", 5);
    	MetaJob a =  metaJob.readMetaJobLastRunningFromUser("p258rsw");
    	System.out.println(a.getApp());
    	
//		Map <Integer, String> stat = null;
//		stat = metaJob.readJobPartialStatusInfo("p260ksy", 78937, 78981);
//		
//		for (int i = 78937; i <= 78981; i++) {
//			System.out.println(i+"\t\t"+stat.get(i));
//		}
//    	
    	
//      System.out.println(metaJob.readMetaJobIdList(38).get(0));
      // metaJob.updateMetaJobStatus(1, "done");
      // System.out.println(metaJob.readMetaJobStatus(1));
      // System.out.println(metaJob.readMetaJobObject(1).toString());
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }
}
