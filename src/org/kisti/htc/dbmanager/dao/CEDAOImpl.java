package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.kisti.htc.dbmanager.beans.CE;

import util.mLogger;
import util.mLoggerFactory;

public class CEDAOImpl extends DAOBase implements CEDAO {

	final static mLogger logger = mLoggerFactory.getLogger("DB");

	private Connection conn;

	public CEDAOImpl(Connection conn) {
		super();
		this.conn = conn;
	}

	public void __info(Object x) {
		logger.info(x);
	}

	@Override
	public int createCE(CE ce) throws Exception {
		int id = -1;

		String sql = "INSERT INTO ce (available, banned, lastUpdateTime, name, service_Infra_id) VALUES (?, ?, now(), ?, ?)";
		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, ce.isAvailable());
			stmt.setBoolean(2, ce.isBanned());
			stmt.setString(3, ce.getName());
			stmt.setInt(4, ce.getServiceInfraId());

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

			sql = "SELECT LAST_INSERT_ID()";
			stmt = conn.prepareStatement(sql);

			// rs = stmt.executeQuery();
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
	public int readCEId(String ceName) throws Exception {

		String sql = "SELECT id FROM ce WHERE name=?";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		int num = -1;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
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
	public CE readCEObject(String ceName) throws Exception {

		String sql = "SELECT * FROM ce WHERE name LIKE ?" ;

		PreparedStatement stmt = null;
		ResultSet rs = null;
		CE ce = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, "%"+ceName+"%");

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				ce = new CE();
				ce.setId(rs.getInt("id"));
				ce.setAvailable(rs.getBoolean("available"));
				ce.setBanned(rs.getBoolean("banned"));
				ce.setFreeCPU(rs.getInt("freeCPU"));
				ce.setTotalCPU(rs.getInt("totalCPU"));
				ce.setRunningJob(rs.getInt("runningJob"));
				ce.setWaitingJob(rs.getInt("waitingJob"));
				ce.setSubmitCount(rs.getInt("submitCount"));
				ce.setSelectCount(rs.getInt("selectCount"));
				ce.setServiceInfraId(rs.getInt("service_Infra_id"));
				ce.setName(rs.getString("name"));
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return ce;
	}
	
	@Override
	public CE readCEObject(int id) throws Exception {

		String sql = "SELECT * FROM ce WHERE id = ?" ;

		PreparedStatement stmt = null;
		ResultSet rs = null;
		CE ce = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				ce = new CE();
				ce.setId(rs.getInt("id"));
				ce.setAvailable(rs.getBoolean("available"));
				ce.setBanned(rs.getBoolean("banned"));
				ce.setFreeCPU(rs.getInt("freeCPU"));
				ce.setTotalCPU(rs.getInt("totalCPU"));
				ce.setRunningJob(rs.getInt("runningJob"));
				ce.setWaitingJob(rs.getInt("waitingJob"));
				ce.setSubmitCount(rs.getInt("submitCount"));
				ce.setServiceInfraId(rs.getInt("service_Infra_id"));
				ce.setName(rs.getString("name"));
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return ce;
	}

	@Override
	public List<CE> readCEObjectSet(int sId, int maxRunningTimeLimit) throws Exception {

		// String sql =
		// "SELECT id, name, waitingTime, maxRunningTime FROM ce WHERE available=1 AND banned=0 AND maxRunningTime >= ? AND service_Infra_id= ? AND numAgentSubmitTry=(SELECT min(numAgentSubmitTry) FROM ce WHERE available=1 AND banned=0 AND service_Infra_id=? AND maxRunningTime>=?)";
		String sql = "SELECT id, name, priority, waitingTime, maxRunningTime FROM ce WHERE available=1 AND banned=0 AND maxRunningTime >= ? AND service_Infra_id= ?";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<CE> ceList = new ArrayList<CE>();
		CE ce = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, maxRunningTimeLimit);
			stmt.setInt(2, sId);
			// stmt.setInt(3, sId);
			// stmt.setInt(4, maxRunningTimeLimit);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			while (rs.next()) {
				ce = new CE();
				ce.setId(rs.getInt("id"));
				ce.setName(rs.getString("name"));
				ce.setPriority(rs.getInt("priority"));
				ce.setWaitingTime(rs.getLong("waitingTime"));
				ce.setMaxRunningTime(rs.getLong("maxRunningTime"));

				ceList.add(ce);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return ceList;
	}

	@Override
	public List<String> readCEName(int voId, boolean available, boolean banned) throws Exception {
		String sql = "SELECT name FROM ce WHERE service_Infra_id=? and available=? and banned=?";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> ces = new ArrayList<String>();
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, voId);
			stmt.setBoolean(2, available);
			stmt.setBoolean(3, banned);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			while (rs.next()) {
				ces.add(rs.getString(1));
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return ces;
	}

	@Override
	public List<String> readCEsIntelligent(int voId, long waitingTime, int numAgentRunning, int numAgentSubmitFailure, int waitingJob) throws Exception {
		String sql = "SELECT name FROM ce WHERE service_Infra_id=? and available=true and (waitingTime <= ? or waitingTime is null) and numAgentRunning>=? and numAgentSubmitFailure<=? and waitingJob <=?";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> ces = new ArrayList<String>();
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, voId);
			stmt.setLong(2, waitingTime);
			stmt.setInt(3, numAgentRunning);
			stmt.setInt(4, numAgentSubmitFailure);
			stmt.setInt(5, waitingJob);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			while (rs.next()) {
				ces.add(rs.getString(1));
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return ces;
	}

	@Override
	public int readCEFreeCPUTotal(int serviceInfra) throws Exception {
		String sql = "SELECT sum(freeCPU) FROM ce WHERE service_Infra_id=? and available=1 and banned=0";

		int fcpu = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, serviceInfra);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				fcpu = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return fcpu;
	}

	@Override
	public int readCEFreeCPU(String ceName) throws Exception {
		String sql = "SELECT freeCPU FROM ce WHERE name=?";

		int freeCPU = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				freeCPU = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return freeCPU;
	}

	@Override
	public int readCEAliveAgent(String ceName) throws Exception {
		String sql = "SELECT aliveAgent FROM ce WHERE name=?";

		int no = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				no = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return no;
	}

	@Override
	public int readCEAliveAgent(int serviceInfra) throws Exception {
		String sql = "SELECT sum(aliveAgent) FROM ce WHERE service_Infra_id=? and aliveAgent>0";

		int no = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, serviceInfra);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				no = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return no;
	}

	@Override
	public int readCEZeroCnt(String ceName) throws Exception {
		String sql = "SELECT zeroCnt FROM ce WHERE name=?";

		int cnt = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				cnt = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return cnt;
	}

	@Override
	public int readCEEqualCnt(String ceName) throws Exception {
		String sql = "SELECT equalCnt FROM ce WHERE name=?";

		int cnt = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				cnt = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return cnt;
	}

	@Override
	public int readCEPriority(String ceName) throws Exception {
		String sql = "SELECT priority FROM ce WHERE name=?";

		int no = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				no = rs.getInt(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return no;
	}

	@Override
	public String readCELimitClass(String ceName) throws Exception {
		String sql = "SELECT limitClass FROM ce WHERE name=?";

		String limitClass = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				limitClass = rs.getString(1);
			}

		} catch (SQLException e) {
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return limitClass;
	}

	@Override
	public int readCELimitCPU(String ceName) throws Exception {
		String sql = "SELECT limitCPU FROM ce WHERE name=?";

		int num = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
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
	public List<CE> readCEObjectList(int serviceInfra, boolean avail, boolean banned) throws Exception {
		String sql = "SELECT id, name, freeCPU, totalCPU FROM ce WHERE service_Infra_id=? and available=? and banned=?";

		List<CE> list = new ArrayList<CE>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, serviceInfra);
			stmt.setBoolean(2, avail);
			stmt.setBoolean(3, banned);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			while (rs.next()) {
				CE ce = new CE();
				ce.setId(rs.getInt("id"));
				ce.setName(rs.getString("name"));
				ce.setFreeCPU(rs.getInt("freeCPU"));
				ce.setTotalCPU(rs.getInt("totalCPU"));

				list.add(ce);
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
	public int readCETotalCPU(String ceName) throws Exception {
		String sql = "SELECT totalCPU FROM ce WHERE name=?";

		int num = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				num = rs.getInt(1);
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return num;
	}

	@Override
	public int readCEAvailableTimeDiff(String ceName) throws Exception {
		String sql = "SELECT TIME_TO_SEC(TIMEDIFF(now(), availableUpdateTime)) FROM ce WHERE name=?";

		int diff = -1;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				diff = rs.getInt(1);
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return diff;
	}
	
	@Override
	public int readCEServiceInfraId(int id) throws Exception {
		String sql = "SELECT service_Infra_id FROM ce WHERE id=?";

		int sid = 0;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);

			// rs = stmt.executeQuery();
			rs = _query(stmt);

			if (rs.next()) {
				sid = rs.getInt(1);
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			DAOUtil.closeResultSet(rs);
		}

		return sid;
	}

	@Override
	public void updateCEObject(CE ce) throws Exception {
		String sql = "UPDATE ce SET available=?, banned=?, lastUpdateTime=now(), freeCPU=?, totalCPU=?, runningJob=?, waitingJob=?  WHERE id = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, ce.isAvailable());
			stmt.setBoolean(2, ce.isBanned());
			stmt.setInt(3, ce.getFreeCPU());
			stmt.setInt(4, ce.getTotalCPU());
			stmt.setInt(5, ce.getRunningJob());
			stmt.setInt(6, ce.getWaitingJob());
			stmt.setInt(7, ce.getId());

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEObjectList(List<CE> ceList) throws Exception {
		String sql = "UPDATE ce SET lastUpdateTime=now(), freeCPU=?, totalCPU=?, waitingTime=?  WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			for (CE ce : ceList) {
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, ce.getFreeCPU());
				stmt.setInt(2, ce.getTotalCPU());
				stmt.setLong(3, ce.getWaitingTime());
				stmt.setString(4, ce.getName());

				// int rows = stmt.executeUpdate();
				int rows = _update(stmt);

				if (rows != 1) {
					throw new SQLException();
				}
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCENumAgentRunning(int ceId, int agentId) throws Exception {
		// Connection conn = DAOUtil.getConnection();
		// String sql =
		// "UPDATE ce SET numAgentRunning=numAgentRunning+1, waitingTime = TIME_TO_SEC(TIMEDIFF(now(), (SELECT submittedTimestamp FROM agent WHERE id=?))) where id=?";
		String sql = "UPDATE ce SET numAgentRunning=numAgentRunning+1, waitingTime = (SELECT waitingTime FROM agent WHERE id=?))) where id=?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, agentId);
			stmt.setInt(2, ceId);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}
			// conn.commit();

		} catch (SQLException e) {
			// conn.rollback();
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
			// DAOUtil.closeJDBCConnection(conn);
		}
	}

	@Override
	public void updateCERunningTime(int ceId, int agentId) throws Exception {
		// String sql =
		// "UPDATE ce SET maxRunningTime=greatest(maxRunningTime, TIME_TO_SEC(TIMEDIFF(now(),(SELECT runningTimestamp FROM agent WHERE id=?)))), runningTime = TIME_TO_SEC(TIMEDIFF(now(), (SELECT runningTimestamp FROM agent where id=?))) where id=?";
		String sql = "UPDATE ce SET maxRunningTime=greatest(maxRunningTime, TIME_TO_SEC(TIMEDIFF(now(),(SELECT runningTimestamp FROM agent WHERE id=?)))), runningTime = TIME_TO_SEC(TIMEDIFF(now(), (SELECT runningTimestamp FROM agent where id=?))) where id=?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, agentId);
			stmt.setInt(2, agentId);
			stmt.setInt(3, ceId);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCENumAgentSubmitTry(String ceName) throws Exception {
		String sql = "UPDATE ce SET numAgentSubmitTry=numAgentSubmitTry+1 WHERE name=?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCENumAgentSubmitFailure(String ceName) throws Exception {
		String sql = "UPDATE ce SET numAgentSubmitFailure=numAgentSubmitFailure+1 WHERE name=?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEAvailable(int ceId, boolean avail) throws Exception {
		String sql = "UPDATE ce SET available = ?, lastUpdateTime=now() WHERE id = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, avail);
			stmt.setInt(2, ceId);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEsAvailable(int voId, boolean avail) throws Exception {
		String sql = "UPDATE ce SET available = ? lastUpdateTime=now() WHERE service_Infra_id = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, avail);
			stmt.setInt(2, voId);

			// stmt.executeUpdate();
			int rows = _update(stmt);

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEsInitSubmitCount(int voId) throws Exception {
		String sql = "UPDATE ce SET submitCount = 0 WHERE service_Infra_id = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, voId);

			// stmt.executeUpdate();
			int rows = _update(stmt);

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCESubmitCountAdd(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET submitCount = submitCount+(?) WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}
	
	@Override
	public void updateCESelectCountAdd(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET selectCount = selectCount+(?) WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEAliveAgentAdd(int ceId, int num) throws Exception {
		String sql = "UPDATE ce SET aliveAgent = aliveAgent+(?) WHERE id = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setInt(2, ceId);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEAliveAgentInit() throws Exception {
		String sql = "UPDATE ce SET aliveAgent = 0";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);

			// stmt.executeUpdate();
			int rows = _update(stmt);

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEZeroCnt(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET zeroCnt = ? WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEZeroCntAll(int num) throws Exception {
		String sql = "UPDATE ce SET zeroCnt = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);

			// stmt.executeUpdate();
			int rows = _update(stmt);

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCESelectCntAll(int num) throws Exception {
		String sql = "UPDATE ce SET selectCount = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);

			// stmt.executeUpdate();
			int rows = _update(stmt);

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEZeroCntAdd(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET zeroCnt = zeroCnt+(?) WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEPriority(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET priority = ? WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEPriorityAdd(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET priority = priority+(?) WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCELimitCPU(String ceName, int num) throws Exception {
		String sql = "UPDATE ce SET limitCPU = ? WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, num);
			stmt.setString(2, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEWaitingTime(int agentId) throws Exception {

		String sql = "";
		PreparedStatement stmt = null;

		try {

			sql = "SET @newtime:=(SELECT waitingTime FROM agent WHERE id=?), @ceid:=(select ce_id from agent where id = ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, agentId);
			stmt.setInt(2, agentId);

			stmt.execute();
			// _update(stmt);

			sql = "SET @oldtime:=(SELECT waitingTime FROM ce WHERE id = @ceid)";
			stmt.execute(sql);
			// _update(stmt);

			sql = "UPDATE ce SET waitingTime = @newtime WHERE @newtime > @oldtime and id = @ceid";
			stmt.executeUpdate(sql);
			// int rows = _update(stmt);

			// if (rows != 1) {
			// throw new SQLException();
			// }

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	@Override
	public void updateCEAvailableUpdateTime(String ceName) throws Exception {
		String sql = "UPDATE ce SET availableUpdateTime = now() WHERE name = ?";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ceName);

			// int rows = stmt.executeUpdate();
			int rows = _update(stmt);

			if (rows != 1) {
				throw new SQLException();
			}

		} catch (SQLException e) {
			logger.error(e);
			throw e;
		} finally {
			DAOUtil.closeStatement(stmt);
		}
	}

	public static void main(String arg[]) {

		Connection conn = DAOUtil.getConnection();
		CEDAOImpl ceDAO = new CEDAOImpl(conn);
		try {

			System.out.println(ceDAO.readCEServiceInfraId(1));
			// System.out.println(ceDAO.readAvailableTotalFreeCPU(5));
			// System.out.println(ceDAO.readCEsAvailable(5, true).size());
			// System.out.println(ceDAO.readCELimit(ceDAO.readSystemNO("kisti.glory")).getLimitDay());
			// ceDAO.updateCESubmitCountAdd("kias.gene", 1);
			// ceDAO.updateCEAliveAgentInit();
			// System.out.println(ceDAO.readCEObjectSet(4, 0));
			// System.out.println(ceDAO.readCEAliveAgent(4));
			// ceDAO.updateCEWaitingTime(1);
			// List<CE> ce =ceDAO.readCEObjectList(4, true, false);
			// System.out.println(ce.size());

			DAOUtil.doCommit(conn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DAOUtil.doRollback(conn);
		}
	}

}
