package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import util.mLogger; 
import util.mLoggerFactory; 


public class EnvDAOImpl extends DAOBase implements EnvDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");

  private Connection conn;

  public EnvDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  
  @Override
  public int createAMEnv(String name) throws Exception{
    int id = -1;

    String sql = "INSERT INTO am_env (name) VALUES (?)";
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

      if(rs.next()){
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
  public boolean readAMEnvAutoMode(int id) throws Exception {

    String sql = "";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    boolean auto = false;
    try {
      sql = "SELECT auto FROM am_env WHERE id=?";
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, id);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        auto = rs.getBoolean(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return auto;
  }
  
  @Override
  public int readAMEnvId(String name) throws Exception {

    String sql = "SELECT id FROM am_env WHERE name=?";

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
  public Map<String, Object> readAMEnv(int id) throws Exception {

    String sql = "SELECT * FROM am_env WHERE id = ?";

    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Object> env = new HashMap<String, Object>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, id);
      
      //rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        env.put("id", rs.getInt("id"));
        env.put("auto", rs.getInt("auto"));
        env.put("service_Infra_id", rs.getString("service_Infra_id"));
        env.put("agentScalingMetric_id", rs.getInt("agentScalingMetric_id"));
        env.put("ceSelectionMetric_id", rs.getInt("ceSelectionMetric_id"));
        env.put("addAgentNO", rs.getInt("addAgentNO"));
//        env.put("runningAgentHP", rs.getInt("runningAgentHP"));
//        env.put("submittedAgentHP", rs.getInt("submittedAgentHP"));
//        env.put("newAgentHP", rs.getInt("newAgentHP"));
        env.put("statusMonitoringHP", rs.getInt("statusMonitoringHP"));
        env.put("thresholdMaxAgent", rs.getInt("thresholdMaxAgent"));
        env.put("thresholdMinAgent", rs.getInt("thresholdMinAgent"));
        env.put("resourceAP", rs.getInt("resourceAP"));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return env;
  }
  
  @Override
  public void updateAMEnv(int id, Map<String, Integer> env) throws Exception {
//    String sql = "UPDATE am_env SET auto=?, serviceInfraMetric_id=?, agentScalingMetric_id=?, ceSelectionMetric_id=?, addAgentNO=?, runningAgentHP=?, submittedAgentHP=?, newAgentHP=?, statusMonitoringHP=?, thresholdMaxAgent=?, thresholdMinAgent=?, resourceAP=? WHERE id=?";
    String sql = "UPDATE am_env SET auto=?, serviceInfraMetric_id=?, agentScalingMetric_id=?, ceSelectionMetric_id=?, addAgentNO=?, statusMonitoringHP=?, thresholdMaxAgent=?, thresholdMinAgent=?, resourceAP=? WHERE id=?";
    PreparedStatement stmt = null;
    
    try{
      stmt = conn.prepareStatement(sql);
      

      stmt.setInt(1, env.get("auto"));
      stmt.setInt(2, env.get("serviceInfraMetric_id"));
      stmt.setInt(3, env.get("agentScalingMetric_id"));
      stmt.setInt(4, env.get("ceSelectionMetric_id"));
      stmt.setInt(5, env.get("addAgentNO"));
//      stmt.setInt(6, env.get("runningAgentHP"));
//      stmt.setInt(7, env.get("submittedAgentHP"));
//      stmt.setInt(8, env.get("newAgentHP"));
      stmt.setInt(6, env.get("statusMonitoringHP"));
      stmt.setInt(7, env.get("thresholdMaxAgent"));
      stmt.setInt(8, env.get("thresholdMinAgent"));
      stmt.setInt(9, env.get("resourceAP"));
      stmt.setInt(10, id);
        
      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);

      if(rows != 1){
        throw new SQLException();
      }

    }catch(SQLException e){
      throw e;
    }finally{
      DAOUtil.closeStatement(stmt);
    }
  }
  
  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    EnvDAOImpl envDAO = new EnvDAOImpl(conn);
    try {
//      System.out.println(envDAO.readAMAutoMode("portal_HTCaaS"));
      System.out.println(envDAO.readAMEnv(1));
//      Map<String, Integer> env = new HashMap<String, Integer>();
//      env.put("auto", 0);
//      env.put("workerScalingMetrics", 4);
//      env.put("numAddAgent", 5);
//      env.put("CEMetrics", 2);
//      env.put("CEResource", 3);
//      System.out.println(envDAO.readAMAutoMode("plsi"));
//      envDAO.updateAMEnv("plsi",env);
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
