package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kisti.htc.dbmanager.beans.Constant;
import org.kisti.htc.dbmanager.beans.Job;

import util.mLogger; 
import util.mLoggerFactory; 



public class JobDAOImpl extends DAOBase implements JobDAO {
  final static mLogger logger = mLoggerFactory.getLogger("DB");
  private Connection conn;

  // constuctor
  public JobDAOImpl(Connection conn) {
    super();
    this.conn = conn;
    //System.out.println(this.getClass().getSimpleName() + ".......................... logger : " + logger);
  }

  public void __info(Object x) {
    logger.info(x);
  }

  /**
   * <pre>
   * 작업을 생성함
   * INSERT INTO job (lastUpdateTime, metajob_id, status, seq) VALUES (NOW(), ?, ?, ?)
   * </pre>
   */
  @Override
  public int createJob(int metaJobId, int jobSeq) throws Exception {
    int id = -1;

    String sql = "INSERT INTO job (lastUpdateTime, metajob_id, status, seq) VALUES (NOW(), ?, ?, ?)";
    ResultSet rs = null;
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      stmt.setString(2, Constant.JOB_STATUS_WAIT);
      stmt.setInt(3, jobSeq);

      logger.debug(stmt);

      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
      if (rows != 1) {
        throw new SQLException();
      }

      sql = "SELECT LAST_INSERT_ID()";
      stmt = conn.prepareStatement(sql);
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
  
  /**
   * <pre>
   * metajob_id 를 읽음
   * SELECT metajob_id FROM job WHERE id=?
   * </pre>
   */
  @Override
  public int readJobMetaJobId(int jobId) throws Exception {

    String sql = "SELECT metajob_id FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int metaJobId = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
        metaJobId = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return metaJobId;
  }

  /**
   * <pre>
   * SELECT status FROM job WHERE id=?
   * </pre>
   */
  @Override
  public String readJobStatus(int jobId) throws Exception {

    String sql = "SELECT status FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String status = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
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

  /**
   * <pre>
   * SELECT jobDetail FROM job WHERE id=?
   * </pre>
   * 
   */
  @Override
  public String readJobDetail(int jobId) throws Exception {

    String sql = "SELECT jobDetail FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String jobDetail = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        jobDetail = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return jobDetail;
  }

  /**
   * <pre>
   * SELECT numResubmit FROM job WHERE id=?
   * </pre>
   * 
   */
  @Override
  public int readJobNumResubmit(int jobId) throws Exception {

    String sql = "SELECT numResubmit FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int numResubmit = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        numResubmit = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return numResubmit;
  }

  /**
   * <pre>
   * 작업상태별로 개수를 구함
   * SELECT j.status, COUNT(1) AS number FROM job j GROUP BY j.status
   * </pre>
   */
  @Override
  public Map<String, Integer> readJobTotalStatusInfo() throws Exception {

    String sql = "SELECT j.status, COUNT(1) as number FROM job j GROUP BY j.status";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
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

  /**
   * <pre>
   * 해당 메타작업의 모든 작업 객체를 구함
   * SELECT * FROM job WHERE metajob_id=? ORDER BY id
   * </pre>
   */
  @Override
  public List<Job> readJobObjectList(int metaJobId) throws Exception {

    String sql = "SELECT * FROM job WHERE metajob_id=? ORDER BY id";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Job> list = new ArrayList<Job>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setSeq(rs.getInt("seq"));
        job.setName(rs.getString("name"));
        job.setErrormsg(rs.getString("errormsg"));
        job.setJobDetail(rs.getString("jobDetail"));
        job.setLastUpdateTime(rs.getTimestamp("lastUpdateTime"));
        job.setNumResubmit(rs.getInt("numResubmit"));
        job.setRunningTime(rs.getLong("runningTime"));
        job.setStartTimestamp(rs.getTimestamp("startTimestamp"));
        job.setStatus(rs.getString("status"));
        job.setCEId(rs.getInt("CE_id"));
        job.setMetaJob(rs.getInt("metajob_id"));
        job.setAgentId(rs.getInt("agent_id"));
        job.setStop(rs.getBoolean("stop"));
        job.setLog(rs.getString("log"));

        list.add(job);

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
  public List<Job> readJobPartialStatusInfo (String userId, int start, int end) throws Exception {

    String sql = "SELECT  jb.metajob_id, jb.seq, jb.status FROM  job AS jb, metajob AS mj, user AS us, application AS ap "
    		+ "WHERE mj.id  >= ? AND  mj.id <= ? AND jb.metajob_id=mj.id  AND us.userid=? AND us.id=mj.user_id "
    		+ "AND ap.id=mj.app_id  ORDER BY mj.id, jb.seq ASC ";
    
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Job> list = new ArrayList<Job>();
    
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, start);
      stmt.setInt(2, end);
      stmt.setString(3, userId);
      rs = _query(stmt);
//      rs = stmt.executeQuery();
      while (rs.next()) {
        Job job = new Job();  
    	job.setId(rs.getInt("metajob_id"));
    	job.setSeq(rs.getInt("seq"));
    	job.setStatus(rs.getString("status"));
    	
        list.add(job);
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return list;
  }
  

  /**
   * <pre>
   * 주어진 작업아이디의 작업 객체를 구함
   * SELECT * FROM job WHERE id=? 
   * </pre>
   */
  @Override
  public Job readJobObject(int jobId) throws Exception {

    String sql = "SELECT * FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Job job = new Job();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        job.setId(rs.getInt("id"));
        job.setSeq(rs.getInt("seq"));
        job.setName(rs.getString("name"));
        job.setErrormsg(rs.getString("errormsg"));
        job.setJobDetail(rs.getString("jobDetail"));
        job.setLastUpdateTime(rs.getTimestamp("lastUpdateTime"));
        job.setNumResubmit(rs.getInt("numResubmit"));
        job.setRunningTime(rs.getLong("runningTime"));
        job.setStartTimestamp(rs.getTimestamp("startTimestamp"));
        job.setStatus(rs.getString("status"));
        job.setCEId(rs.getInt("CE_id"));
        job.setMetaJob(rs.getInt("metajob_id"));
        job.setAgentId(rs.getInt("agent_id"));
        job.setStop(rs.getBoolean("stop"));
        job.setLog(rs.getString("log"));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return job;
  }

  /**
   * <pre>
   * 메타작업아이디와 시쿼스를 주고 작업 객체를 구함
   * SELECT * FROM job WHERE metajob_id=? AND seq=?
   * </pre>
   */
  @Override
  public Job readJobObject(int metaJobId, int jobSeq) throws Exception {

    String sql = "SELECT * FROM job WHERE metajob_id=? AND seq=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    Job job = new Job();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      stmt.setInt(2, jobSeq);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        job.setId(rs.getInt("id"));
        job.setSeq(rs.getInt("seq"));
        job.setName(rs.getString("name"));
        job.setErrormsg(rs.getString("errormsg"));
        job.setJobDetail(rs.getString("jobDetail"));
        job.setLastUpdateTime(rs.getTimestamp("lastUpdateTime"));
        job.setNumResubmit(rs.getInt("numResubmit"));
        job.setRunningTime(rs.getLong("runningTime"));
        job.setStartTimestamp(rs.getTimestamp("startTimestamp"));
        job.setStatus(rs.getString("status"));
        job.setCEId(rs.getInt("CE_id"));
        job.setMetaJob(rs.getInt("metajob_id"));
        job.setAgentId(rs.getInt("agent_id"));
        job.setStop(rs.getBoolean("stop"));
        job.setLog(rs.getString("log"));
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return job;
  }

  /**
   * <pre>
   * 주어진 메타작업에 대한 서브작업 아이디 리스트를 얻음
   * SELECT id FROM job WHERE metajob_id=? ORDER BY id
   * </pre>
   * @return List of Integer
   */
  @Override
  public List<Integer> readJobIdList(int metaJobId) throws Exception {

    String sql = "SELECT id FROM job WHERE metajob_id=? ORDER BY id";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = new ArrayList<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
        list.add(rs.getInt("id"));

      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return list;
  }

  /**
   * <pre>
   * Autodoc 작업 로그에서 Mean_Energy 값에 대한 검색
   * </pre>
   * @return 작업 아이디의 리스트
   */
  @Override
  public List<Integer> readJobIdListAutodockEL(int metaJobId, int energyLvLow, int energyLvHigh) throws Exception {

    String sql = "SELECT id FROM job WHERE log like '%Mean_Energy%' AND SUBSTRING(log, LOCATE('Mean_Energy', log)+13, 6) > ? AND SUBSTRING(log, LOCATE('Mean_Energy', log)+13, 6) <= ? AND metajob_id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = new ArrayList<Integer>();
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, energyLvLow);
      stmt.setInt(2, energyLvHigh);
      stmt.setInt(3, metaJobId);

      //rs = stmt.executeQuery();
      rs = _query(stmt);
      while (rs.next()) {
        list.add(rs.getInt("id"));

      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return list;
  }

  /**
   * <pre>
   * Job Stop 값을 읽음 
   * SELECT stop FROM job WHERE id=?
   * </pre>
   */
  @Override
  public boolean readJobStop(int jobId) throws Exception {

    String sql = "SELECT stop FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    boolean stop = false;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        stop = rs.getBoolean(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return stop;
  }

  @Override
  public List<Integer> readJobIdByStatus(int metaJobId, String status) throws Exception {

    String sql = "SELECT id FROM job WHERE metajob_id=? AND status=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<Integer> list = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      stmt.setString(2, status);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      list = new ArrayList<Integer>();
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
  public int readJobId(int metaJobId, int jobSeq) throws Exception {

    String sql = "SELECT id FROM job WHERE metajob_id=? AND seq=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int jobId = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      stmt.setInt(2, jobSeq);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
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
  public String readJobLog(int metaJobId, int jobSeq) throws Exception {

    String sql = "SELECT log FROM job WHERE metajob_id=? AND seq=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String log = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      stmt.setInt(2, jobSeq);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        log = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }
    
    return log;
  }
  
  @Override
  public String readJobLog(int jobId) throws Exception {

    String sql = "SELECT log FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    String log = null;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      
//      rs = stmt.executeQuery();
      rs = _query(stmt);
      
      if (rs.next()) {
        log = rs.getString(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return log;
  }

  @Override
  public int readPendingJobNum(int metaJobId) throws Exception {

    String sql = "SELECT count(id) FROM job WHERE metajob_id=? AND (status='" + Constant.JOB_STATUS_WAIT + "' or status='" + Constant.JOB_STATUS_PRE + "' or status='" + Constant.JOB_STATUS_RUN + "')";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int pNum = 0;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        pNum = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return pNum;
  }

  @Override
  public int readJobCEId(int jobId) throws Exception {

    String sql = "SELECT CE_id FROM job WHERE id=?";
    PreparedStatement stmt = null;
    ResultSet rs = null;
    int ceId = -1;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);
      //rs = stmt.executeQuery();
      rs = _query(stmt);
      if (rs.next()) {
        ceId = rs.getInt(1);
      }
    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
      DAOUtil.closeResultSet(rs);
    }

    return ceId;
  }

  /**
   * <pre>
   * 작업의 상태를 running 상태로 바꿈
   * UPDATE job SET status='running', startTimestamp=NOW(), lastUpdateTime=NOW() WHERE id=?
   * </pre>
   */
  @Override
  public void updateJobStart(int jobId) throws Exception {
    String sql = "UPDATE job SET status = '" + Constant.JOB_STATUS_RUN + "', startTimestamp = now(), lastUpdateTime = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);

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

  @Override
  public void updateJobFinish(int jobId) throws Exception {
    String sql = "UPDATE job SET status = '" + Constant.JOB_STATUS_DONE + "', runningTime = TIME_TO_SEC(TIMEDIFF(now(),startTimestamp)) ,lastUpdateTime = now()" + "WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);

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

  @Override
  public void updateJobStatus(int jobId, String status) throws Exception {
    String sql = "UPDATE job SET status = ?, lastUpdateTime = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setInt(2, jobId);

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

  @Override
  public void updateJobErrormsg(int jobId, String msg) throws Exception {
    String sql = "UPDATE job SET errormsg = ?, lastUpdateTime = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, msg);
      stmt.setInt(2, jobId);

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

  /**
   * <pre>
   * job 상세 정보를 업데이트
   * </pre>
   * 
   */
  @Override
  public void updateJobDetail(int jobId, String jobDetail) throws Exception {
    String sql = "UPDATE job SET jobDetail = ?, lastUpdateTime = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, jobDetail);
      stmt.setInt(2, jobId);

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

  @Override
  public void updateJobCEnAgentId(int jobId, int agentId) throws Exception {
    String sql = "UPDATE job SET CE_id = (SELECT CE_id FROM agent WHERE id=?), agent_id=? WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, agentId);
      stmt.setInt(2, agentId);
      stmt.setInt(3, jobId);

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

  @Override
  public void updateJobRunningTime(int jobId) throws Exception {
    String sql = "UPDATE job SET runningTime = TIME_TO_SEC(TIMEDIFF(now(),startTimestamp)), lastUpdateTime = now() WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);

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

  @Override
  public void updateJobObject(int jobId, Job job) throws Exception {
    String sql = "UPDATE job SET errormsg=?, lastUpdateTime = now(), runningTime=0, startTimestamp=null, numResubmit=?, status=?, CE_id=?, agent_id=?  WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, job.getErrormsg());
      stmt.setInt(2, job.getNumResubmit());
      stmt.setString(3, job.getStatus());
      stmt.setObject(4, job.getCEId());
      stmt.setObject(5, job.getAgentId());
      stmt.setInt(6, jobId);

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

  @Override
  public void updateJobStop(int jobId) throws Exception {
    String sql = "UPDATE job SET stop=1 WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, jobId);

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

  @Override
  public void updateJobCancel(int metaJobId) throws Exception {
    String sql = "UPDATE job SET status='" + Constant.JOB_STATUS_CANCEL + "', stop=1 WHERE metajob_id = ? AND (status = '" + Constant.JOB_STATUS_WAIT + "' OR status = '" + Constant.JOB_STATUS_PRE + "' OR status = '" + Constant.JOB_STATUS_RUN + "') ";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);

      //stmt.executeUpdate();
      int rows = _update(stmt);

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public boolean updateJobLog(int jobId, String jobLog) throws Exception {
    String sql = "UPDATE job SET log=? WHERE id = ?";
    PreparedStatement stmt = null;
    boolean result = false;
    
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, jobLog);
      stmt.setInt(2, jobId);

//      int ret = stmt.executeUpdate();
      int ret = _update(stmt);
      if(ret == 1){
        result = true;
      }

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
    
    return result;
  }

  @Override
  public void updateJobName(int jobId, String name) throws Exception {
    String sql = "UPDATE job SET name=? WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, name);
      stmt.setInt(2, jobId);

      //stmt.executeUpdate();
      int rows = _update(stmt);

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public void updateJobNumResubmitAdd(int jobId, int num) throws Exception {
    String sql = "UPDATE job SET numResubmit=numResubmit+? WHERE id = ?";
    PreparedStatement stmt = null;

    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, num);
      stmt.setInt(2, jobId);

      //stmt.executeUpdate();
      int rows = _update(stmt);

    } catch (SQLException e) {
      throw e;
    } finally {
      DAOUtil.closeStatement(stmt);
    }
  }

  @Override
  public boolean deleteJobs(int metaJobId) throws Exception {
    String sql = "DELETE FROM job WHERE metajob_id = ?";
    PreparedStatement stmt = null;
    boolean result = false;
    try {
      stmt = conn.prepareStatement(sql);
      stmt.setInt(1, metaJobId);

      //int rows = stmt.executeUpdate();
      int rows = _update(stmt);
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
    JobDAOImpl job = new JobDAOImpl(conn);
    try {
      // Job job = new Job();
      // job.setStatus("waiting");
      // job.setLastUpdateTime(new Date());
      // job.setNumResubmit(1);
      // job.setErrormsg(null);
      // job.setCEId(null);
      // job.setStartTimestamp(null);
      // job.setAgentId(null);
      // System.out.println(job1.readJobIdByStatus(1, "failed"));
      // System.out.println(job.readJobIdListAutodockEL(2, -21, -9));
      // System.out.println(job.readPendingJobNum(2));
      // System.out.println(job.createJob(1, 1));

//      job.updateJobNumResubmitAdd(1, 1);
      
      System.out.println(job.readJobLog(569, 15445));

      DAOUtil.doCommit(conn);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      DAOUtil.doRollback(conn);
    }
  }

}
