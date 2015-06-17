package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kisti.htc.dbmanager.beans.AgentInfo;
import org.kisti.htc.dbmanager.beans.Constant;

import util.mLogger;
import util.mLoggerFactory;


public class AgentDAOImpl extends DAOBase implements AgentDAO {

  final static mLogger logger = mLoggerFactory.getLogger("DB");

  private Connection conn;

  public AgentDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  @Override
  public int createAgent() throws Exception {
    int id = -1;

    String sql = "INSERT INTO agent (lastSignal, status) values (NOW(), '" + Constant.AGENT_STATUS_NEW + "')";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      
      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);
      rs = _query(stmt);
      // rs = stmt.executeQuery();
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
  public int createAgent(String userId) throws Exception {
    int id = -1;

    String sql = "INSERT into agent (lastSignal, status, user_id) values (NOW(), '" + Constant.AGENT_STATUS_NEW + "', (SELECT id FROM user WHERE userid=?))";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);
      // int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);
      rs = _query(stmt);
      // rs = stmt.executeQuery();
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
  public String readAgentStatus(int agentId) throws Exception {

    String sql = "SELECT status FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String status = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
      // rs = stmt.executeQuery();
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
  public String readAgentHost(int agentId) throws Exception {

    String sql = "SELECT host FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String host = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        host = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return host;
  }

  @Override
  public int readAgentCEId(int agentId) throws Exception {

    String sql = "SELECT CE_id FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int ce_id = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        if (rs.getString(1) != null) {
          ce_id = Integer.parseInt(rs.getString(1));
        }
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return ce_id;
  }

  @Override
  public boolean readAgentSleep(int agentId) throws Exception {

    String sql = "SELECT sleep FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    boolean sleep = false;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
      //rs = stmt.executeQuery();
      while(rs.next()){
        sleep = rs.getBoolean(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return sleep;
  }

  @Override
  public boolean readAgentQuit(int agentId) throws Exception {
    
    String sql = "SELECT quit FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    boolean quit = false;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
      //rs = stmt.executeQuery();
      while(rs.next()){
        quit = rs.getBoolean(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return quit;
  }

  @Override
  public List<Integer> readAgentJobListByStatus(String status, boolean flag) throws Exception {

    String sql = "SELECT currentJob FROM agent WHERE status=? and flag=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = new ArrayList<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setBoolean(2, flag);
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
  public int readAgentCurrentJob(int agentId) throws Exception {

    String sql = "SELECT currentJob FROM agent WHERE id=?";
    
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int jobId = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        jobId = rs.getInt(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return jobId;
  }

  @Override
  public List<Integer> readAgentList(String status, boolean flag, int serviceInfra) throws Exception {

    String sql = "SELECT agent.id FROM agent, ce WHERE agent.status=? and agent.flag=? and agent.CE_id=ce.id and ce.service_Infra_id=? ";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = new ArrayList<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setBoolean(2, flag);
      stmt.setInt(3, serviceInfra);

      //rs = stmt.executeQuery();
      //System.out.println(stmt);
      rs = _query(stmt);

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
  public List<Integer> readAgentList(String status, boolean flag, int serviceInfra, int timelimit) throws Exception {

    String sql = "SELECT agent.id FROM agent, ce WHERE agent.status=? and agent.flag=? and agent.CE_id=ce.id and ce.service_Infra_id=? and TIME_TO_SEC(TIMEDIFF(now(), lastSignal))/60 > ? ";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = new ArrayList<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setBoolean(2, flag);
      stmt.setInt(3, serviceInfra);
      stmt.setInt(4, timelimit);

      //rs = stmt.executeQuery();
      //System.out.println(stmt);
      rs = _query(stmt);

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
  public int readAgentNumStatus(String status) throws Exception {

    String sql = "SELECT count(1) FROM agent WHERE status=? and flag=0";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);

      //  rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
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
  public int readAgentNumStatus(String status, int serviceInfra) throws Exception {

    String sql = "SELECT count(agent.id) FROM agent,ce WHERE agent.status=? and agent.flag=0 and ce.id=agent.CE_id and ce.service_Infra_id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, serviceInfra);

      //  rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
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
  public Set<Integer> readAgentUserId(String status) throws Exception {

    String sql = "SELECT DISTINCT(user_id) FROM agent WHERE status=?;";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Set<Integer> ret = new HashSet<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);

      //  rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
        ret.add(rs.getInt(1));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return ret;
  }

  @Deprecated
  @Override
  public int readAgentNumAlive(int timelimit) throws Exception {

    String sql = "SELECT count(1) FROM agent WHERE status='" + Constant.AGENT_STATUS_RUN + "' and TIME_TO_SEC(TIMEDIFF(now(), lastSignal))/60 <= ?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, timelimit);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
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
  public int readAgentNumValidAll() throws Exception {

    String sql = "SELECT count(1) FROM agent WHERE flag=0";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try {
      stmt = conn.prepareStatement(sql);

      //  rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
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

  @Deprecated
  @Override
  public int readUserAgentNum(int runningAgentHP, int submittedAgentHP, String userId) throws Exception {

    String sql = "SELECT count(1) FROM agent WHERE ((status='" + Constant.AGENT_STATUS_RUN + "' and TIME_TO_SEC(TIMEDIFF(now(), lastSignal))/60 <= ?) " + "or (status='" + Constant.AGENT_STATUS_SUB + "' and  TIME_TO_SEC(TIMEDIFF(now(), submittedTimestamp))/60 <= ?)) "
        + "and user_id=(SELECT id FROM user WHERE userid=?)";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, runningAgentHP);
      stmt.setInt(2, submittedAgentHP);
      stmt.setString(3, userId);
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
  public int readUserAgentNum(String userId) throws Exception {

    String sql = "SELECT count(1) FROM agent WHERE flag=0 and user_id=(SELECT id FROM user WHERE userid=?)";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, userId);
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
  public int readUserAgentNumStatus(int userId, String status) throws Exception {

    String sql = "SELECT count(1) FROM agent WHERE flag=0 and status=? and user_id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, userId);
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
  public Map<String, Integer> readUserAgentNumCE(String status, int userId) throws Exception {

    String sql = "SELECT ce.name, count(*) FROM agent, ce WHERE status=? and flag=0 and user_id=? and agent.CE_id=ce.id GROUP BY ce_id";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Integer> ceMap = new HashMap<String, Integer>();
    
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, userId);

      //  rs = stmt.executeQuery();
      rs = _query(stmt);

      while (rs.next()) {
        ceMap.put(rs.getString(1), rs.getInt(2));
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return ceMap;
  }
  
  @Override
  public Integer readUserAgentNumFromCE(String status, int userId, String ceName) throws Exception {

    String sql = "SELECT count(*) FROM agent, ce WHERE agent.status=? and agent.flag=0 and agent.user_id=? and ce.name=? and agent.CE_id=ce.id";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = 0;
    
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, userId);
      stmt.setString(3, ceName);;

      //  rs = stmt.executeQuery();
      rs = _query(stmt);

      if (rs.next()) {
        num =rs.getInt(1);
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
  public Timestamp readAgentRunningTimestamp(int agentId) throws Exception {

    String sql = "SELECT runningTimestamp FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Timestamp ts = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        ts = rs.getTimestamp(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return ts;
  }

  @Override
  public int readAgentNumJobs(int agentId) throws Exception {

    String sql = "SELECT numJobs FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int numJobs = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        numJobs = rs.getInt(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return numJobs;
  }

  @Override
  public int readAgentRunningTime(int agentId) throws Exception {

    String sql = "SELECT runningTime FROM agent WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int runningTime = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        runningTime = rs.getInt(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return runningTime;
  }

  @Override
  public String readAgentUserId(int agentId) throws Exception {

    String sql = "SELECT userid FROM user WHERE id=(SELECT user_id FROM agent WHERE id = ?)";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String userId = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        userId = rs.getString(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return userId;
  }

  @Override
  public String readAgentCEName(int agentId) throws Exception {

    String sql = "SELECT name FROM ce WHERE id=(SELECT CE_id FROM agent WHERE id = ?)";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String name = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        name = rs.getString(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return name;
  }

  @Override
  public String readAgentSubmitId(int agentId) throws Exception {

    String sql = "SELECT submitId FROM agent WHERE id = ?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String submitId = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        submitId = rs.getString(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return submitId;
  }

  @Override
  public int readAgentLastId() throws Exception {
    String sql = "SELECT max(id) FROM agent";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int aid = -1;
    try {
      stmt = conn.prepareStatement(sql);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      if (rs.next()) {
        aid = rs.getInt(1);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return aid;
  }

  @Override
  public int readAgentRunningNum(int minAgentId) throws Exception {
    String sql = "SELECT count(id) FROM agent where (status='" + Constant.AGENT_STATUS_RUN + "' or status='" + Constant.AGENT_STATUS_DONE + "' ) and id>?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int num = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, minAgentId);
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
  public Set<AgentInfo> readAgentInfoSetFromMetaJob(int metaJobId) throws Exception {
    String sql = "SELECT agent.id, agent.host, agent.numJobs, agent.status, agent.submittedTimestamp, "
    		+ "agent.runningTimestamp, agent.endTimestamp, agent.runningTime  FROM job, agent "
    		+ "WHERE job.metajob_id=? and job.agent_id=agent.id GROUP BY agent.id";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Set<AgentInfo> aSet = new HashSet<AgentInfo>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
    	  AgentInfo aInfo = new AgentInfo();
    	  aInfo.setId(rs.getInt(1));
    	  aInfo.setHost(rs.getString(2));
    	  aInfo.setNumJobs(rs.getInt(3));
    	  aInfo.setStatus(rs.getString(4));
    	  aInfo.setSubmittedTimestamp(rs.getTimestamp(5));
    	  aInfo.setStartTimestamp(rs.getTimestamp(6));
    	  aInfo.setEndTimestamp(rs.getTimestamp(7));
    	  aInfo.setRunningTime(rs.getInt(8));
    	  
    	  aSet.add(aInfo);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return aSet;
  }
  
  @Override
  public Map<String, Integer> readAgentNumMapFromMetaJob(int metaJob) throws Exception {
    String sql = "SELECT agent.host, count(distinct(agent.id)) FROM job, agent "
    		+ "WHERE job.metajob_id=? and job.agent_id=agent.id group by agent.host";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Integer> numMap = new HashMap<String, Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJob);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
    	  numMap.put(rs.getString(1), rs.getInt(2));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return numMap;
  }
  
  @Override
  public Map<String, Integer> readAgentTaskMapFromMetaJob(int metaJob) throws Exception {
    String sql = "SELECT agent.host, count(*) FROM job, agent "
    		+ "WHERE job.metajob_id=? and job.agent_id=agent.id and job.status='done' group by agent.host ;";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Integer> taskMap = new HashMap<String, Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJob);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
    	  taskMap.put(rs.getString(1), rs.getInt(2));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return taskMap;
  }
  

  @Override
  public void updateAgentNumJobs(int agentId) throws Exception {

    String sql = "UPDATE agent SET numJobs = numJobs+1, lastSignal = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);

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
  public void updateAgentGangaId(int agentId, int gangaId) throws Exception {

    String sql = "UPDATE agent SET gangaId=?, lastSignal=now(), status=?, submittedTimestamp=now() WHERE id=?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, gangaId);
      stmt.setString(2, Constant.AGENT_STATUS_SUB);
      stmt.setInt(3, agentId);
      
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
  public void updateAgentSubmitId(int agentId, String submitId) throws Exception {

    String sql = "UPDATE agent SET submitId=?, lastSignal=now(), status=?, submittedTimestamp=now() WHERE id=?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, submitId);
      stmt.setString(2, Constant.AGENT_STATUS_SUB);
      stmt.setInt(3, agentId);

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
  public void updateAgentHost(int agentId, String host) throws Exception {

    String sql = "UPDATE agent SET " + "host=?, lastSignal=now() WHERE id=?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, host);
      stmt.setInt(2, agentId);

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
  public void updateAgentRunningStatus(int agentId) throws Exception {

    String sql = "UPDATE agent SET status = '" + Constant.AGENT_STATUS_RUN + "', lastSignal = now(), runningTimestamp = now(), " + "waitingTime = TIME_TO_SEC(TIMEDIFF(now(),submittedTimestamp)) WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);

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
  public void updateAgentCurrentJob(int agentId, Integer jobId) throws Exception {

    String sql = "UPDATE agent SET currentJob = ?, lastSignal = now() WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setObject(1, jobId);
      stmt.setInt(2, agentId);

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
  public void updateAgentRunningTime(int agentId) throws Exception {

    String sql = "UPDATE agent SET runningTime = TIME_TO_SEC(TIMEDIFF(now(),runningTimestamp)), " + "lastSignal = now() WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      
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
  public void updateAgentStatus(int agentId, String status) throws Exception {

    String sql = "UPDATE agent SET status=?, lastSignal = now() WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, agentId);
      
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
  public void updateAgentSubmittedTimestamp(int agentId) throws Exception {

    String sql = "UPDATE agent SET submittedTimestamp=now() WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);

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
  public void updateAgentFinish(int agentId) throws Exception {

    String sql = "UPDATE agent SET status='" + Constant.AGENT_STATUS_DONE + "', lastSignal = now(), endTimestamp=now(), " + "runningTime = TIME_TO_SEC(TIMEDIFF(now(),runningTimestamp)), flag=1 WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);

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
  public void updateKSCAgentFinish(int agentId, long runningTime) throws Exception {

    String sql = "UPDATE agent SET status='" + Constant.AGENT_STATUS_DONE + "', lastSignal = now(), endTimestamp=now(), " + "runningTime = ?, flag=1 WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setLong(1, runningTime);
      stmt.setInt(2, agentId);

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
  public void updateAgentStop(int agentId) throws Exception {

    String status = readAgentStatus(agentId);
    String sql = "UPDATE agent SET status=concat(? ,'-stopped'), flag=1, lastSignal = now(), endTimestamp=now(), " + "runningTime = TIME_TO_SEC(TIMEDIFF(now(),runningTimestamp)) WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, agentId);

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
  public void updateAgentFail(int agentId) throws Exception {

    String sql = "UPDATE agent SET status='" + Constant.AGENT_STATUS_FAIL + "', lastSignal = now(), endTimestamp=now(), " + "runningTime = TIME_TO_SEC(TIMEDIFF(now(),runningTimestamp)), flag=1 WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);

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
  public void updateKSCAgentFail(int agentId, long runningTime) throws Exception {

    String sql = "UPDATE agent SET status='" + Constant.AGENT_STATUS_FAIL + "', lastSignal = now(), endTimestamp=now(), " + "runningTime = ?, flag=1 WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setLong(1, runningTime);
      stmt.setInt(2, agentId);

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
  public void updateAgentCE(int agentId, String ceName) throws Exception {

    String sql = "UPDATE agent SET CE_id=(SELECT id from ce where name=?) WHERE id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, ceName);
      stmt.setInt(2, agentId);

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
  public int updateAgentStatusZombie(String status, String zombie, int timelimit, int serviceInfra) throws Exception {

    String sql = null;
    if (status.equals(Constant.AGENT_STATUS_RUN)) {
      sql = "UPDATE agent INNER JOIN ce ON agent.CE_id=ce.id SET status=?, quit=1 WHERE status=? AND ce.service_Infra_id=? AND TIME_TO_SEC(TIMEDIFF(now(), lastSignal))/60 > ?";
    } else {
      sql = "UPDATE agent INNER JOIN ce ON agent.CE_id=ce.id SET status=?, quit=1, flag=1 WHERE status=? AND ce.service_Infra_id=? AND TIME_TO_SEC(TIMEDIFF(now(), lastSignal))/60 > ?";
    }
    PreparedStatement stmt = null;
    int rows = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, zombie);
      stmt.setString(2, status);
      stmt.setInt(3, serviceInfra);
      stmt.setInt(4, timelimit);
      
      rows = _update(stmt);
//      rows = stmt.executeUpdate();
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }

    return rows;
  }

  @Override
  public int updateAgentFlag(int agentId, boolean flag) throws Exception {

    String sql = "UPDATE agent SET flag=? WHERE id=?";
    PreparedStatement stmt = null;
    int rows = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setBoolean(1, flag);
      stmt.setInt(2, agentId);
      
      rows = _update(stmt);
//      rows = stmt.executeUpdate();
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
    return rows;
  }

  @Override
  public void updateAgentLastSignal(int agentId, String date) throws Exception {

    String sql = "UPDATE agent SET lastSignal = ? where id = ?";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, date);
      stmt.setInt(2, agentId);

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
  public void updateAgentValidInit(int serviceInfra) throws Exception {

  }

  public static void main(String arg[]) {

    Connection conn = DAOUtil.getConnection();
    AgentDAOImpl agentDAO = new AgentDAOImpl(conn);
    try {

//    	System.out.println(agentDAO.readUserAgentNumCE("done", 18));
    	System.out.println(agentDAO.readAgentList("failed", true, 2));
    	
      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
