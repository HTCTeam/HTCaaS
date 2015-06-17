package org.kisti.htc.dbmanager.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;

import org.kisti.htc.constant.ResourceConstant;
import org.kisti.htc.dbmanager.beans.AgentInfo;
import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.Constant;
import org.kisti.htc.dbmanager.beans.Job;
import org.kisti.htc.dbmanager.beans.MetaJob;
import org.kisti.htc.dbmanager.beans.ServiceInfra;
import org.kisti.htc.dbmanager.beans.User;
import org.kisti.htc.dbmanager.beans.WMS;
import org.kisti.htc.dbmanager.dao.AgentDAO;
import org.kisti.htc.dbmanager.dao.ApplicationDAO;
import org.kisti.htc.dbmanager.dao.CEDAO;
import org.kisti.htc.dbmanager.dao.DAOFactory;
import org.kisti.htc.dbmanager.dao.EnvDAO;
import org.kisti.htc.dbmanager.dao.JobDAO;
import org.kisti.htc.dbmanager.dao.MetaJobDAO;
import org.kisti.htc.dbmanager.dao.NoticeDAO;
import org.kisti.htc.dbmanager.dao.ResultDAO;
import org.kisti.htc.dbmanager.dao.ServerEnvDAO;
import org.kisti.htc.dbmanager.dao.ServiceCodeDAO;
import org.kisti.htc.dbmanager.dao.ServiceInfraDAO;
import org.kisti.htc.dbmanager.dao.SubmitErrorDAO;
import org.kisti.htc.dbmanager.dao.UserDAO;
import org.kisti.htc.dbmanager.dao.WMSCEDAO;
import org.kisti.htc.dbmanager.dao.WMSDAO;
import org.kisti.htc.message.DTO;
import org.kisti.htc.message.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;

public class DatabaseImpl implements Database {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseImpl.class);
//	final static mLogger logger = mLoggerFactory.getLogger("DB");

	DAOFactory daoFactory;

	public DatabaseImpl() {

		daoFactory = DAOFactory.getDAOFactory();
	}

	/**
	 * 작업 제출 UserDAO::readUserId() ApplicationDAO::readAppId()
	 * MetaJobDAO::createMetaJob()
	 * 
	 * @return 작업아이디를 리턴, 실패시 -1을 리턴
	 */
	@Override
	public Integer insertMetaJob(String userName, String appName, String metaJobDocument, Integer aMaxJobTimeMin, String pName, String sName, String ces) {

		logger.info("insertMetaJob " + userName + " A JobTime(min):" + aMaxJobTimeMin);

		int metaJob_id = -1;

		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			ApplicationDAO applicationDAO = daoFactory.getApplicationDAO(conn);
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);

			// get the user id from user name
			int user_id = userDAO.readUserId(userName);

			int app_id = applicationDAO.readAppId(appName);

			metaJob_id = metaJobDAO.createMetaJob(metaJobDocument, app_id, Constant.METAJOB_STATUS_WAIT, user_id, aMaxJobTimeMin, pName, sName, ces);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to insertMetaJob: uname " + userName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry insertMetaJob");
				return insertMetaJob(userName, appName, metaJobDocument, aMaxJobTimeMin, pName, sName, ces);
			}

		} catch (Exception e) {
			logger.error("failed to create MetaJob : {}, {}", metaJob_id, e.toString());
			daoFactory.rollback(conn);
			return -1;
		} finally {
			daoFactory.endTransaction(conn);
		}

		return metaJob_id;
	}

	/**
	 * 작업의 사용자 아이디를 구함 MetaJobDAO::readMetaJobUserID()
	 * 
	 * @return 사용자 아이디
	 */
	@Override
	public String getMetaJobUserId(Integer metaJobId) {
		logger.info("getMetaJobUserId " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		String name = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			name = metaJobDAO.readMetaJobUserID(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobUserId: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobUserId");
				getMetaJobUserId(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobUserId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return name;
	}

	/**
	 * 작업의 JSDL 문서를 리턴
	 * 
	 * @return jsdl 문서 (String)
	 */
	@Override
	public String getMetaJobJSDL(Integer metaJobId) {
		logger.info("getMetaJobJSDL " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		String name = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			name = metaJobDAO.readMetaJobJSDL(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobJSDL: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobJSDL");
				getMetaJobJSDL(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobJSDL: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return name;
	}

	/**
	 * 작업의 진행 상태를 조회 metaJobDAO::readMetaJobProgress()
	 * 
	 * @return
	 */
	@Override
	public Map<String, Integer> getMetaJobProgress(Integer metaJobId) {
		logger.info("getMetaJobProgress " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		Map<String, Integer> stats = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			stats = metaJobDAO.readMetaJobProgress(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobProgress: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobProgress");
				getMetaJobProgress(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobProgress: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return stats;
	}

	/**
	 * 작업 진행상태를 조회 JobDAO::readJobTotalStatusInfo()
	 * 
	 * @return
	 */
	@Override
	public Map<String, Integer> getMetaJobProgressAll() {
		logger.info("getMetaJobProgressAll ");

		Connection conn = daoFactory.beginTransaction();
		Map<String, Integer> stats = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			stats = jobDAO.readJobTotalStatusInfo();
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobProgressAll: {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobProgressAll");
				getMetaJobProgressAll();
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobProgressAll: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return stats;
	}
	
	/**
	 * 작업 진행상태를 조회  MetaJobDAO::readJobPartialStatusInfo()
	 * 
	 * @return
	 */
	@Override
	public List<Job> getMetaJobProgressinRange(String user, int start, int end) {
		logger.info("getMetaJobProgressinRange ");

		Connection conn = daoFactory.beginTransaction();
		List<Job> list = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			list = jobDAO.readJobPartialStatusInfo(user, start, end);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobProgressinRange: {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobProgressinRange");
				getMetaJobProgressAll();
			}
		} catch (Exception e) {
			logger.error("Failed to getMetaJobProgressinRange: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return list;
	}

	/**
	 * 작업아이디를 주고 MetaJob 객체를 리턴 MetaJobDAO::readMetaJobObject()
	 * 
	 * @return
	 */
	@Override
	public MetaJob getMetaJobObject(Integer metaJobId) {
		logger.info("getMetaJobObject " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		MetaJob metaJob = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			metaJob = metaJobDAO.readMetaJobObject(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobObject: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobObject");
				getMetaJobObject(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobObject: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return metaJob;
	}

	/**
	 * 특정사용자의 작업 객체 리스트를 리턴 MetaJobDAO::readMetaJobObjects()
	 * 
	 * @return
	 */
	@Override
	public List<MetaJob> getMetaJobObjectList(String user) {
		logger.info("getMetaJobObjectList " + user);

		Connection conn = daoFactory.beginTransaction();
		List<MetaJob> list = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			list = metaJobDAO.readMetaJobObjects(user);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobObjectList: " + user + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobObjectList");
				getMetaJobObjectList(user);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobObjectList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return list;
	}

	/**
	 * 특정사용자의 작업 객체 리스트를 리턴(Limit) MetaJobDAO::readMetaJobObjects(String, int)
	 * 
	 * @return
	 */
	@Override
	public List<MetaJob> getMetaJobObjectListLimit(String user, int num) {
		logger.info("getMetaJobObjectListLimit " + user + ", limit :" + num);

		Connection conn = daoFactory.beginTransaction();
		List<MetaJob> list = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			list = metaJobDAO.readMetaJobObjects(user, num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobObjectList: " + user + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobObjectList");
				getMetaJobObjectList(user);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobObjectList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return list;
	}

	/**
	 * 작업의 상태를 리턴 MetaJobDAO::readMetaJobStatus()
	 * 
	 * @return 작업상태 (String)
	 */
	@Override
	public String getMetaJobStatus(Integer metaJobId) {
		logger.info("getMetaJobStatus " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		String status = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			status = metaJobDAO.readMetaJobStatus(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobStatus: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobStatus");
				getMetaJobStatus(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobStatus: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return status;
	}

	/**
	 * 사용자의 작업 아이디 리스트를 리턴 MetaJobDAO::readMetaJobIdList()
	 * 
	 * @return 작업아이디의 리스트
	 */
	@Override
	public List<Integer> getMetaJobIdList(String user) {
		logger.info("getMetaJobIdList " + user);

		Connection conn = daoFactory.beginTransaction();
		List<Integer> list = null;
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			int userId = userDAO.readUserId(user);

			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			list = metaJobDAO.readMetaJobIdList(userId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobIdList: " + user + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobIdList");
				getMetaJobIdList(user);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobIdList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return list;
	}

	@Override
	public int getMetaJobListSubTotal(int startMetaId, int endMetaId){
		logger.info("getMetaJobListSubTotal" + startMetaId + "- " + endMetaId);

		Connection conn = daoFactory.beginTransaction();
		int total = 0;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			total = metaJobDAO.readMetaJobListSubTotal(startMetaId, endMetaId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobListSubTotal: " + startMetaId + "- " + endMetaId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobListSubTotal");
				getMetaJobListSubTotal(startMetaId, endMetaId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobListSubTotal: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return total;
		
	}
	
	
	@Override
	public Integer getMetaJobAJobTime(Integer metaJobId) {
		logger.info("getMetaJobAJobTime " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		int time = 0;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			time = metaJobDAO.readMetaJobAJobTime(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobAJobTime: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobAJobTime");
				getMetaJobAJobTime(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobAJobTime: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return time;
	}
	
	@Override
	public MetaJob getMetaJobLastRunningFromUser(String userId) {
		logger.info("getMetaJobLastRunningFromUser " + userId);

		Connection conn = daoFactory.beginTransaction();
		MetaJob mJob = null;
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			mJob = metaJobDAO.readMetaJobLastRunningFromUser(userId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getMetaJobLastRunningFromUser :  code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getMetaJobLastRunningFromUser");
				getMetaJobLastRunningFromUser(userId);
			}
		} catch (Exception e) {
			logger.error("Failed to get MetaJobLastRunningFromUser: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return mJob;
	}

	/**
	 * 작업의 상태를 변경 MetaJobDAO::updateMetaJobStatus()
	 */
	@Override
	public void setMetaJobStatus(Integer metaJobId, String status) {
		logger.info("setMetaJobStatus " + metaJobId + " " + status);

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			// update db table
			metaJobDAO.updateMetaJobStatus(metaJobId, status);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setMetaJobStatus:mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setMetaJobStatus");
				setMetaJobStatus(metaJobId, status);
			}
		} catch (Exception e) {
			logger.error("Failed to set MetaJobStatus: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 작업에 대한 에러메시지를 설정 MetaJobDAO::updateMetaJobError()
	 * 
	 * @return
	 */
	@Override
	public void setMetaJobError(Integer metaJobId, String error) {
		logger.info("setMetaJobError " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			metaJobDAO.updateMetaJobError(metaJobId, error);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setMetaJobError: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setMetaJobError");
				setMetaJobError(metaJobId, error);
			}
		} catch (Exception e) {
			logger.error("Failed to set MetaJobError: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 작업을 중지킨다. MetaJobDAO::updateMetaJobStatus(metaJobId, "stopped");
	 * 
	 * @return
	 */
	@Override
	public void stopMetaJob(Integer metaJobId) {
		logger.info("stopMetaJob " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			metaJobDAO.updateMetaJobStatus(metaJobId, Constant.METAJOB_STATUS_CANCEL);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to stopMetaJob: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry stopMetaJob");
				stopMetaJob(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to stop MetaJob: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * jobNum 을 증가시킨다.(?) MetaJobDAO::updateAddMetaJobNum(id, 1)
	 */
	@Override
	public void increaseMetaJobNum(Integer metaJobId) {
		logger.info("increaseMetaJobNum " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			metaJobDAO.updateAddMetaJobNum(metaJobId, 1);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to increaseMetaJobNum: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry increaseMetaJobNum");
				increaseMetaJobNum(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to increase MetaJobNum: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * jobNum 을 감소시킨다.(?) MetaJobDAO::updateAddMetaJobNum(id, -1)
	 */
	@Override
	public boolean decreaseMetaJobNum(Integer metaJobId) {
		logger.info("decreaseMetaJobNum " + metaJobId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			metaJobDAO.updateAddMetaJobNum(metaJobId, -1);

			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to decreaseMetaJobNum: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry decreaseMetaJobNum");
				return decreaseMetaJobNum(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to decrease MetaJobNum: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * jobNum 을 채크해서 작업 상태를 done 으로 만든다. MetaJobDAO::readMetaJobNum()
	 * MetaJobDAO::updateMetaJobStatus(id, "done")
	 * 
	 * @return
	 */
	@Override
	public void checkMetaJobStatusByNum(Integer metaJobId) {
		logger.info("checkMetaJobStatusByNum " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			if (metaJobDAO.readMetaJobNum(metaJobId) == 0) {
				metaJobDAO.updateMetaJobStatus(metaJobId, Constant.METAJOB_STATUS_DONE);
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to checkMetaJobStatusByNum: mid " + metaJobId + " {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry checkMetaJobStatusByNum");
				checkMetaJobStatusByNum(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to check MetaJobStatusByNum: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 서브작업중 pending 인것을 채크해서 작업의 상태를 'done' 으로 바꾼다.
	 * MetaJobDAO::readPendingJobNum(id) MetaJobDAO::updateMetaJobStatus(id,
	 * "done")
	 * 
	 * @return
	 */
	@Override
	public void checkMetaJobStatusBySubJob(Integer metaJobId) {
		logger.info("checkMetaJobStatusBySubJob " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			JobDAO jobDAO = daoFactory.getJobDAO(conn);

			if (jobDAO.readPendingJobNum(metaJobId) == 0) {
				metaJobDAO.updateMetaJobStatus(metaJobId, Constant.METAJOB_STATUS_DONE);
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to checkMetaJobStatusBySubJob: mid " + metaJobId + " {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry checkMetaJobStatusBySubJob");
				checkMetaJobStatusByNum(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to check MetaJobStatusBySubJob: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 작업을 삭제한다. MetaJobDAO::deleteMetaJob(id) return true if success
	 */
	@Override
	public boolean removeMetaJob(Integer metaJobId) {
		logger.info("removeMetaJob " + metaJobId);
		boolean result = false;

		Connection conn = daoFactory.beginTransaction();
		try {
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);

			result = metaJobDAO.deleteMetaJob(metaJobId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to removeMetaJob: mid " + metaJobId + " {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry removeMetaJob");
				return removeMetaJob(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to remove MetaJob: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 작업을 추가한다. JobDAO::createJob() MetaJobDAO::updateAddMetaJobNum(id, 1)
	 * MetaJobDAO::updateAddMetaJobTotal(id, 1)
	 * 
	 * @return
	 */
	@Override
	public Integer addJob(Integer metaJobId, Integer jobSeq) {
		logger.info("addJob {}", metaJobId + " " + jobSeq);

		int jobId = -1;

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			jobId = jobDAO.createJob(metaJobId, jobSeq);
			metaJobDAO.updateAddMetaJobNum(metaJobId, 1);
			metaJobDAO.updateAddMetaJobTotal(metaJobId, 1);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to addJob: mid " + metaJobId + " {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry addJob");
				return addJob(metaJobId, jobSeq);
			}
		} catch (Exception e) {
			logger.error("Failed to add Job: {}, {}", metaJobId, e.toString());
			daoFactory.rollback(conn);
			return -1;
		} finally {
			daoFactory.endTransaction(conn);
		}

		return jobId;
	}

	/**
	 * 작업을 시작함 JobDAO::updateJobStart(id)
	 * 
	 * @return
	 */
	@Override
	public boolean startJob(Integer jobId) {
		logger.info("startJob " + jobId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobStart(jobId);

			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to startJob: jid " + jobId + " {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry startJob");
				return startJob(jobId);
			}
		} catch (Exception e) {
			logger.error("Failed to start Job: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
			throw new RuntimeException();
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 작업을 종료한다. JobDAO::updateJobFinish(id) AgentDAO::updateAgentNumJobs(id)
	 * 
	 * @return
	 */
	@Override
	public boolean finishJob(Integer jobId, Integer agentId) {
		logger.info("S-finishJob job-" + jobId + " agent-" + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {

			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobFinish(jobId);
			daoFactory.onlyCommit(conn);

			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentNumJobs(agentId);

			logger.info("E-finishJob job-" + jobId + " agent-" + agentId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to finishJob: job-" + jobId + " agent-" + agentId + ", code-{}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry finishJob");
				return finishJob(jobId, agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to finish Job: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
			return false;
		} finally {
			daoFactory.endTransaction(conn);
		}

		return true;
	}

	/**
	 * 작업의 상태를 설정함 JobDAO::updateJobStatus(id, status)
	 * 
	 * @return
	 */
	@Override
	public boolean setJobStatus(Integer jobId, String status) {
		logger.info("setJobStatus " + jobId + " " + status);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobStatus(jobId, status);
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setJobStatus: " + jobId + " " + status + ", code:{}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setJobStatus");
				setJobStatus(jobId, status);
			}
		} catch (Exception e) {
			logger.error("Failed to set JobStatus: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 작업의 에러메시지를 설정 JobDAO::updateJobErrormsg(id, msg)
	 * 
	 * @return
	 */
	@Override
	public boolean setJobErrormsg(Integer jobId, String msg) {
		logger.info("setJobErrormsg " + jobId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobErrormsg(jobId, msg);

			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setJobErrormsg: jid " + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setJobErrormsg");
				return setJobErrormsg(jobId, msg);
			}
		} catch (Exception e) {
			logger.error("Failed to set JobErrormsg: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 작업 상세 정보를 설정 JobDAO::updateJobDetail(id, jobDetail) MetaJobDAO::
	 * 
	 * @return
	 */
	@Override
	public void setJobDetail(Integer jobId, String jobDetail) {
		logger.info("setJobDetail " + jobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobDetail(jobId, jobDetail);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setJobDetail jid " + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setJobDetail");
				setJobDetail(jobId, jobDetail);
			}
		} catch (Exception e) {
			logger.error("Failed to set JobDetail: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 작업을 취소한다. JobDAO::updateJobCancel(id)
	 * 
	 * @return
	 */
	@Override
	public void setJobCancel(Integer metaJobId) {
		logger.info("setJobCancel MetaJobID:" + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobCancel(metaJobId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setJobCancel : mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setJobCancel");
				setJobCancel(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to set JobCancel: {}, {}", metaJobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 작업 로그를 설정 JobDAO::updateJobLog()
	 * 
	 * @return
	 */
	@Override
	public boolean setJobLog(Integer jobId, String jobLog) {
		logger.info("setJobLog " + jobId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			// public class JobDAOImpl implements JobDAO
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			result = jobDAO.updateJobLog(jobId, jobLog);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setJobLog: jid" + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setJobLog");
				setJobLog(jobId, jobLog);
			}
		} catch (Exception e) {
			logger.error("Failed to set JobLog: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 작업명을 설정 JobDAO::updateJobName(id, name)
	 * 
	 * @return
	 */
	@Override
	public void setJobName(Integer jobId, String name) {
		logger.info("setJobName " + jobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobName(jobId, name);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setJobname: jid" + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setJobname");
				setJobName(jobId, name);
			}
		} catch (Exception e) {
			logger.error("Failed to set JobName: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 작업을 제거함 JobDAO::deleteJobs(id) MetaJobDAO::
	 * 
	 * @return
	 */
	@Override
	public boolean removeJobs(Integer metaJobId) {
		logger.info("removeJobs " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.deleteJobs(metaJobId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to removeJobs :mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry removeJobs");
				return removeJobs(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to remove Jobs: {}, {}", metaJobId, e.toString());
			daoFactory.rollback(conn);
			return false;
		} finally {
			daoFactory.endTransaction(conn);
		}

		return true;
	}

	/**
	 * 작업을 다시 제출 JobDAO::readJobDetail(id) ResultDAO::readLFN(id)
	 * JobDAO.updateJobStatus(id, "done"); JobDAO.readJobNumResubmit(id);
	 * JobDAO.updateJobObject(id, job); MetaJobDAO.readMetaJobUserID(metaJobId);
	 */
	@Override
	public boolean reEnqueueJob(Integer jobId) {
		logger.info("reEnqueueJob " + jobId);

		boolean ret = false;
		MessageSender upload = null;

		try {

			// Parse JobDetail
			DTO dto = new DTO();

			List<String> arguments = new ArrayList<String>();
			List<String> inputFiles = new ArrayList<String>();
			List<String> outputFiles = new ArrayList<String>();

			String detail = null;
			List<String> result = null;

			Connection conn = daoFactory.beginTransaction();
			try {
				JobDAO jobDAO = daoFactory.getJobDAO(conn);
				detail = jobDAO.readJobDetail(jobId);

				ResultDAO resultDAO = daoFactory.getResultDAO(conn);
				result = resultDAO.readLFN(jobId);

			} catch (Exception e) {
				daoFactory.rollback(conn);
				logger.error("Failed to get jobDetail and jobResult: {}", e.toString());
				return ret;
			} finally {
				daoFactory.endTransaction(conn);
			}

			String[] lines = detail.split("\n");
			for (String line : lines) {
				String regExpr = "\\[(\\p{Alnum}+)\\]:?(.+)";
				Pattern pattern = Pattern.compile(regExpr);
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String tag = matcher.group(1).trim();
					String value = matcher.group(2).trim();

					if (tag.equals("MetaJobID"))
						dto.setMetaJobId(Integer.parseInt(value));
					else if (tag.equals("JobID"))
						dto.setJobId(jobId);
					else if (tag.equals("UserID"))
						dto.setUserId(value);
					else if (tag.equals("AppName"))
						dto.setAppName(value);
					else if (tag.equals("Executable"))
						dto.setExecutable(value);
					else if (tag.equals("Arguments"))
						arguments.add(value);
					else if (tag.equals("InputFiles"))
						inputFiles.add(value);
					else if (tag.equals("OutputFiles"))
						outputFiles.add(value);
					else {
						logger.error("Unknown Tag in JobDetail: " + tag);
						return ret;
					}
				}
			}

			dto.setArguments(arguments);
			dto.setInputFiles(inputFiles);
			dto.setOutputFiles(outputFiles);

			// Check OutputFiles
			boolean outputAllExist = true;
			
			if(!outputFiles.isEmpty()){
				for (String output : outputFiles) {
					boolean exists = false;
					for (String lfn : result) {
						if (lfn.endsWith(output)) {
							exists = true;
							break;
						}
					}
					if (!exists) {
						outputAllExist = false;
						break;
					}
				}
			}else{
				outputAllExist = false;
			}

			if (outputAllExist) {

				conn = daoFactory.beginTransaction();
				try {
					JobDAO jobDAO = daoFactory.getJobDAO(conn);

					if (!jobDAO.readJobStatus(jobId).equals(Constant.JOB_STATUS_DONE))
						jobDAO.updateJobStatus(jobId, Constant.JOB_STATUS_DONE);

				} catch (SQLException e) {
					int code = e.getErrorCode();
					logger.error("Failed to set reenq-JobStatus done:jid " + jobId + ", code {}, {}", code, e.toString());
					logger.error("SQLState : {}", e.getSQLState());
					daoFactory.rollback(conn);
					return ret;
				} catch (Exception e) {
					logger.error("Failed to set reenq-jobStatus done : {}", e.toString());
					daoFactory.rollback(conn);
					return ret;
				} finally {
					daoFactory.endTransaction(conn);
				}
				logger.info("jobId: {}, outputAllExist", jobId);
			} else {
				logger.info("jobID: " + jobId);

				conn = daoFactory.beginTransaction();
				try {
					JobDAO jobDAO = daoFactory.getJobDAO(conn);
					int metaJobId = jobDAO.readJobMetaJobId(jobId);

					MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
					if (!jobDAO.readJobStatus(jobId).equals(Constant.JOB_STATUS_WAIT)) {
						metaJobDAO.updateAddMetaJobNum(metaJobId, 1);
					}
					daoFactory.onlyCommit(conn);

					jobDAO.updateJobStatus(jobId, Constant.JOB_STATUS_WAIT);
					jobDAO.updateJobNumResubmitAdd(jobId, 1);
					daoFactory.onlyCommit(conn);

					String userId = metaJobDAO.readMetaJobUserID(metaJobId);
					upload = new MessageSender(userId);
					upload.sendMessage(dto);

					logger.info(" job " + jobId + " are resubmitted");

					ret = true;

				} catch (SQLException e) {
					int code = e.getErrorCode();
					logger.error("Failed to reenq-UpdateJobObject:jid " + jobId + ", code {}, {}", code, e.toString());
					logger.error("SQLState : {}", e.getSQLState());
					daoFactory.rollback(conn);
					return ret;
				} catch (JMSException e1) {
					logger.error("Failed to send JobObject into queue : {}", e1.toString());
					return ret;
				} finally {
					daoFactory.endTransaction(conn);
					try {
						if (upload != null)
							upload.close();
					} catch (JMSException e) {
						e.printStackTrace();
						return ret;
					}
				}
			}

		} catch (Exception e) {
			logger.error("Failed to reEnqueueJob: {} {}", jobId, e);
			ret = false;
		} finally {
		}

		return ret;
	}

	/**
	 * ??? JobDAO::readJobNumResubmit(id)
	 * 
	 * @return
	 */
	@Override
	public Integer getJobNumResubmit(Integer jobId) {
		logger.info("getJobNumResubmit " + jobId);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			num = jobDAO.readJobNumResubmit(jobId);
		} catch (Exception e) {
			logger.error("Failed to get JobNumResubmit: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 작업 객체를 리턴(1) JobDAO::readJobObject(id)
	 * 
	 * @return
	 */
	@Override
	public Job getJobObject(Integer jobId) {
		logger.info("getJobObject " + jobId);

		Connection conn = daoFactory.beginTransaction();
		Job job = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			job = jobDAO.readJobObject(jobId);
		} catch (Exception e) {
			logger.error("Failed to get JobObject: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return job;
	}

	/**
	 * 작업 객체를 리턴(2) JobDAO::readJobObject(id, sequence)
	 * 
	 * @return
	 */
	@Override
	public Job getJobObject(Integer metaJobId, Integer jobSeq) {
		logger.info("getJobObject M:" + metaJobId + " S:" + jobSeq);

		Connection conn = daoFactory.beginTransaction();
		Job job = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			job = jobDAO.readJobObject(metaJobId, jobSeq);
		} catch (Exception e) {
			logger.error("Failed to get JobObject: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return job;
	}

	/**
	 * Job 객체 리스트를 리턴 JobDAO::readJobObjectList(id)
	 * 
	 * @return
	 */
	@Override
	public List<Job> getJobObjectList(Integer metaJobId) {
		logger.info("getJobObjectList " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		List<Job> list = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			list = jobDAO.readJobObjectList(metaJobId);
		} catch (Exception e) {
			logger.error("Failed to get JobObjectList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return list;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getJobMetaJobId(Integer jobId) {
		logger.info("getJobMetaJobId " + jobId);

		Connection conn = daoFactory.beginTransaction();
		int metaJobId = -1;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			metaJobId = jobDAO.readJobMetaJobId(jobId);

		} catch (Exception e) {
			logger.error("Failed to get JobMetaJobId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return metaJobId;
	}

	/**
	 * 서브작업 아이디 리스트를 얻음 JobDAO::readJobIdList(id)
	 * 
	 * @return
	 */
	@Override
	public List<Integer> getJobIdList(Integer metaJobId) {
		logger.info("getJobbIdList " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		List<Integer> list = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			list = jobDAO.readJobIdList(metaJobId);

		} catch (Exception e) {
			logger.error("Failed to get JobIdList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return list;
	}

	/**
	 * Audock 관련함수
	 * 
	 * @return
	 */
	@Override
	public List<Integer> getJobIdListAutodockEL(Integer metaJobId, Integer energyLvLow, Integer energyLvHigh) {
		logger.info("getJobbIdListAutodockEL " + metaJobId + " " + energyLvLow + " " + energyLvHigh);

		Connection conn = daoFactory.beginTransaction();
		List<Integer> list = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			list = jobDAO.readJobIdListAutodockEL(metaJobId, energyLvLow, energyLvHigh);

		} catch (Exception e) {
			logger.error("Failed to get JobIdListAutodockEL: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return list;
	}

	/**
	 * 작업 sequence 번호로 부터 아이디를 리턴 JobDAO::readJobId(id, seq)
	 * 
	 * @param jobSeq
	 *            시퀀스번호
	 * @return
	 */
	@Override
	public Integer getJobId(Integer metaJobId, Integer jobSeq) {
		logger.info("getJobbId " + metaJobId + ", JobSeq " + jobSeq);

		Connection conn = daoFactory.beginTransaction();
		int jobId = -1;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobId = jobDAO.readJobId(metaJobId, jobSeq);

		} catch (Exception e) {
			logger.error("Failed to get JobId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return jobId;
	}

	/**
	 * 주어진 메타작업과 작업시퀀스에 대한 로그를 얻음 JobDAO::readJobLog(mjid, seq)
	 * 
	 * @return
	 */
	@Override
	public String getJobLog(Integer metaJobId, Integer jobSeq) {
		logger.info("getJobbLog " + metaJobId + ", JobSeq " + jobSeq);

		Connection conn = daoFactory.beginTransaction();
		String log = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			log = jobDAO.readJobLog(metaJobId, jobSeq);

		} catch (Exception e) {
			logger.error("Failed to get JobLog: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return log;
	}

	@Override
	public String getJobLog(int jobId) {
		logger.info("getJobbId " + jobId);

		Connection conn = daoFactory.beginTransaction();
		String log = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			log = jobDAO.readJobLog(jobId);

		} catch (Exception e) {
			logger.error("Failed to get JobId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return log;
	}

	/**
	 * 작업 리스트를 구함 JobDAO::readJobIdByStatus(id, status)
	 * 
	 * @return List of Integer
	 */
	@Override
	public List<Integer> getJobIdListByStatus(Integer metaJobId, String status) {
		logger.info("getJobbIdList " + metaJobId + ", status " + status);

		Connection conn = daoFactory.beginTransaction();
		List<Integer> list = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			list = jobDAO.readJobIdByStatus(metaJobId, status);

		} catch (Exception e) {
			logger.error("Failed to get JobIdList by Status: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return list;
	}

	/**
	 * getJobStatus JobDAO::readJobStatus(jid)
	 * 
	 * @return status
	 */
	@Override
	public String getJobStatus(Integer jobId) {
		logger.info("getJobStatus " + jobId);

		Connection conn = daoFactory.beginTransaction();
		String status = null;
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			status = jobDAO.readJobStatus(jobId);

		} catch (Exception e) {
			logger.error("Failed to get jobStatus: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return status;
	}

	// 에이전트를 생성함 AddAgent (1)
	// AgentDAO::createAgent()
	// INSERT INTO Agent (lastSignal, status) values (NOW(),
	// Constant.AGENT_STATUS_NEW)
	// 생성된 에이전트 아이디를 리턴
	@Override
	public Integer addAgent() {
		logger.info("addAgent");
		int id = -1;

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			id = agentDAO.createAgent();
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to addAgent:, code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry addAgent");
				return addAgent();
			}
		} catch (Exception e) {
			logger.error("Failed to add Agent: {}", e.toString());
			daoFactory.rollback(conn);
			return -1;
		} finally {
			daoFactory.endTransaction(conn);
		}
		return id;
	}

	/**
	 * 에이전트를 생성함(2) AgentDAO::createAgent(userid)
	 * 
	 * @return
	 */
	@Override
	public Integer addAgent(String userId) {
		logger.info("addAgent " + userId);
		int id = -1;

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			id = agentDAO.createAgent(userId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to addAgent: " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry addAgent");
				return addAgent(userId);
			}
		} catch (Exception e) {
			logger.error("Failed to add Agent: " + userId + ", {}", e.toString());
			daoFactory.rollback(conn);
			return -1;
		} finally {
			daoFactory.endTransaction(conn);
		}
		return id;
	}

	/**
	 * 에이전트를 시작함 AgentDAO::updateAgentRunningStatus(agentid)
	 * CEDAO::updateCEWaitingTime(agentid)
	 * 
	 * @return
	 */
	@Override
	public boolean startAgent(Integer agentId) {
		logger.info("startAgent " + agentId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentRunningStatus(agentId);
			daoFactory.onlyCommit(conn);
			// int ceId = agentDAO.readAgentCEId(agentId);
			// if (ceId != -1) {
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			// ceDAO.updateCENumAgentRunning(ceId, agentId);
			ceDAO.updateCEWaitingTime(agentId);
			result = true;
			// }
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to startAgent: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry startAgent");
				startAgent(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to start Agent: aid " + agentId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 에이전트를 종료함 AgentDAO::updateAgentFinish(agentid)
	 * 
	 * @return
	 */
	@Override
	public boolean finishAgent(Integer agentId) {
		logger.info("finishAgent " + agentId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentFinish(agentId);
			// int ceId = agentDAO.readAgentCEId(agentId);
			// if (ceId != -1) {
			// CEDAO ceDAO = daoFactory.getCEDAO(conn);
			// ceDAO.updateCERunningTime(ceId, agentId);
			// }
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to finish Agent: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry finishAgent");
				finishAgent(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to finish Agent: aid " + agentId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 에이전트를 중지함 AgentDAO::updateAgentStop(agentid)
	 * 
	 * @return
	 */
	@Override
	public boolean stopAgent(Integer agentId) {
		logger.info("stopAgent " + agentId);
		
		boolean ret = false;
		
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentStop(agentId);
			ret = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to stopAgent: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry stopAgent");
				stopAgent(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to stop Agent: aid " + agentId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		
		return ret;
	}

	/**
	 * 에이전트 상태를 얻음 AgentDAO::readAgentStatus(agentid)
	 * 
	 * @return
	 */
	@Override
	public String getAgentStatus(Integer agentId) {
		logger.info("getAgentStatus " + agentId);

		Connection conn = daoFactory.beginTransaction();
		String status = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			status = agentDAO.readAgentStatus(agentId);

		} catch (Exception e) {
			logger.error("Failed to get AgentStatus: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return status;
	}

	/**
	 * 에이전트 호스트를 얻음 AgentDAO::readAgentHost(agentid)
	 * 
	 * @return
	 */
	@Override
	public String getAgentHost(Integer agentId) {
		logger.info("getAgentHost " + agentId);

		Connection conn = daoFactory.beginTransaction();
		String host = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			host = agentDAO.readAgentHost(agentId);

		} catch (Exception e) {
			logger.error("Failed to get AgentHost: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return host;
	}

	/**
	 * 에어전트 사용자 아이디를 구함 AgentDAO::readAgentUserId(agentid)
	 * 
	 * @return
	 */
	@Override
	public String getAgentUserId(Integer agentId) {
		logger.info("getAgentUserId " + agentId);

		Connection conn = daoFactory.beginTransaction();
		String userId = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			userId = agentDAO.readAgentUserId(agentId);

		} catch (Exception e) {
			logger.error("Failed to get AgentUserId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return userId;
	}

	/**
	 * 에이전트 CE 이름을 구함 AgentDAO::readAgentCEName(agentid)
	 * 
	 * @return
	 */
	@Override
	public String getAgentCEName(Integer agentId) {
		logger.info("getAgentCEName " + agentId);

		Connection conn = daoFactory.beginTransaction();
		String ceName = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			ceName = agentDAO.readAgentCEName(agentId);

		} catch (Exception e) {
			logger.error("Failed to get AgentCEName: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ceName;
	}

	/**
	 * 에이전트 submitId 를 구함 AgentDAO::readAgentSubmitId(agentid)
	 * 
	 * @return
	 */
	@Override
	public String getAgentSubmitId(Integer agentId) {
		logger.info("getAgentSubmitId " + agentId);

		Connection conn = daoFactory.beginTransaction();
		String submitId = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			submitId = agentDAO.readAgentSubmitId(agentId);

		} catch (Exception e) {
			logger.error("Failed to get AgentSubmitId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return submitId;
	}

	/**
	 * 에이전트 좀비 리스트(?)를 구함 AgentDAO::readAgentZombie("submitted", true)
	 * 
	 * @return
	 */
	@Override
	public List<Integer> getAgentSubmittedZombieList(int serviceInfra) {
		logger.info("getAgentSubmittedZombieList " + serviceInfra);

		Connection conn = daoFactory.beginTransaction();
		List<Integer> list = new ArrayList<Integer>();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			
//			List<CE> ceList = ceDAO.readCEObjectList(serviceInfra, true, false);
//			for(CE ce : ceList){
				list.addAll(agentDAO.readAgentList(Constant.AGENT_STATUS_SUBZOM, true, serviceInfra));
//			}

		} catch (Exception e) {
			logger.error("Failed to get AgentSubmittedZombieList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return list;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getAgentLastId() {
		logger.info("getAgentLastId ");

		Connection conn = daoFactory.beginTransaction();
		int aid = -1;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			aid = agentDAO.readAgentLastId();

		} catch (Exception e) {
			logger.error("Failed to get AgentLastId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return aid;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getAgentRunningNum(Integer minAgentId) {
		logger.info("getAgentRunningNum MinAgentId :" + minAgentId);

		Connection conn = daoFactory.beginTransaction();
		int num = -1;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentRunningNum(minAgentId);

		} catch (Exception e) {
			logger.error("Failed to get AgentRunningNum: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;

	}
	
	@Override
	public List<Integer> getAgentListFromStatus(int serviceInfra, String status, boolean flag){
		logger.info("getAgentListFromStatus " + serviceInfra + ", " + status);

		Connection conn = daoFactory.beginTransaction();
		List<Integer> list = new ArrayList<Integer>();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			
			list.addAll(agentDAO.readAgentList(status, flag, serviceInfra));

		} catch (Exception e) {
			logger.error("Failed to get AgentListFromStatus: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return list;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getNumAliveAgent(Integer timelimit) {
		logger.info("getNumAliveAgent timelimit: " + timelimit);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentNumAlive(timelimit);

		} catch (Exception e) {
			logger.error("Failed to get NumAliveAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getNumValidAgentAll() {
		logger.info("getNumValidAgentAll");

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentNumValidAll();

		} catch (Exception e) {
			logger.error("Failed to get NumValidAgentAll: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getNumUserAgent(Integer runningAgentHP, Integer submittedAgentHP, String userId) {
		logger.info("getNumUserAgent RunningAgentHP: " + runningAgentHP + ", SubmittedAgentHP : " + submittedAgentHP + ", UserID :" + userId);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readUserAgentNum(runningAgentHP, submittedAgentHP, userId);

		} catch (Exception e) {
			logger.error("Failed to get NumAliveAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getNumUserAgentValid(String userId) {
		logger.info("getNumUserAgent UserID :" + userId);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readUserAgentNum(userId);

		} catch (Exception e) {
			logger.error("Failed to get NumUserAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getNumUserAgentStatus(String userId, String status) {
		logger.info("getNumUserAgentStatus UserID :" + userId + ", status : " + status);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			int uid = userDAO.readUserId(userId);
			num = agentDAO.readUserAgentNumStatus(uid, status);

		} catch (Exception e) {
			logger.error("Failed to get NumUserAgentStatus: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public synchronized Integer getNumUserAgentRunning(String userId) {
		logger.info("getNumUserAgentRunning UserID :" + userId);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			UserDAO userDAO = daoFactory.getUserDAO(conn);

			num = agentDAO.readUserAgentNumStatus(userDAO.readUserId(userId), Constant.AGENT_STATUS_RUN);

		} catch (Exception e) {
			logger.error("Failed to get NumUserAgentRunning: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getNumAgent(String status) {
		logger.info("getNumAgent status :" + status);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentNumStatus(status);

		} catch (Exception e) {
			logger.error("Failed to get NumAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}
	
	@Override
	public Integer getNumAgent(String status, int serviceInfra) {
		logger.info("getNumAgent status :" + status + ", serviceInfra :" + serviceInfra);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentNumStatus(status, serviceInfra);

		} catch (Exception e) {
			logger.error("Failed to get NumAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentGangaId(Integer agentId, Integer gangaId) {
		logger.info("setAgentGangaId " + agentId + " " + gangaId);
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentGangaId(agentId, gangaId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setGangaId: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentGangaId");
				setAgentGangaId(agentId, gangaId);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentGangaId: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentSubmitId(Integer agentId, String submitId) {
		logger.info("setAgentSubmitId " + agentId + " " + submitId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentSubmitId(agentId, submitId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentSubmitId: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentSubmitId");
				setAgentSubmitId(agentId, submitId);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentSubmitId: aid " + agentId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentSubmitIdMap(Map agentIdMap, String submitId) {
		logger.info("setAgentSubmitIdMap " + " " + submitId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);

			for (int i = 0; i < agentIdMap.size(); i++) {
				agentDAO.updateAgentSubmitId((Integer) agentIdMap.get(i), submitId + "." + i);
			}
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentSubmitIdMap: sid " + submitId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentSubmitIdMap");
				setAgentSubmitIdMap(agentIdMap, submitId);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentSubmitIdMap: sid " + submitId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean setAgentHost(Integer agentId, String host) {
		logger.info("setAgentHost " + agentId + " " + host);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentHost(agentId, host);
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentHost: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentHost");
				setAgentHost(agentId, host);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentHost: aid " + agentId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean setAgentCurrentJob(Integer agentId, Integer jobId, Integer metaJobId) {
		logger.info("setAgentCurrentJob " + agentId + ", " + jobId + ", " + metaJobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);

			if (jobId == null) {
				agentDAO.updateAgentCurrentJob(agentId, jobId);

				return true;
			}

			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);

			int numJobs = agentDAO.readAgentNumJobs(agentId);

			if (numJobs == 0) {
				agentDAO.updateAgentCurrentJob(agentId, jobId);
				daoFactory.onlyCommit(conn);
				jobDAO.updateJobCEnAgentId(jobId, agentId);
				jobDAO.updateJobStatus(jobId, Constant.JOB_STATUS_PRE);
				daoFactory.onlyCommit(conn);
				metaJobDAO.updateAddMetaJobNum(metaJobId, -1);

				return true;
			}

			int agentRunningTime = agentDAO.readAgentRunningTime(agentId);
			int remainingTimeMin = (7 * 24 * 60) - (agentRunningTime / 60);
			int aJobTime = metaJobDAO.readMetaJobAJobTime(jobDAO.readJobMetaJobId(jobId));

			if (aJobTime > 0) {
				if (remainingTimeMin > aJobTime) {
					agentDAO.updateAgentCurrentJob(agentId, jobId);
					daoFactory.onlyCommit(conn);
					jobDAO.updateJobCEnAgentId(jobId, agentId);
					jobDAO.updateJobStatus(jobId, Constant.JOB_STATUS_PRE);
					daoFactory.onlyCommit(conn);
					metaJobDAO.updateAddMetaJobNum(metaJobId, -1);

					return true;
				}
			}

			if (numJobs > 0) {
				int aJobMeanTimeMin = agentRunningTime / numJobs / 60;
				if (remainingTimeMin > aJobMeanTimeMin) {
					agentDAO.updateAgentCurrentJob(agentId, jobId);
					daoFactory.onlyCommit(conn);
					jobDAO.updateJobCEnAgentId(jobId, agentId);
					jobDAO.updateJobStatus(jobId, Constant.JOB_STATUS_PRE);
					daoFactory.onlyCommit(conn);
					metaJobDAO.updateAddMetaJobNum(metaJobId, -1);

					return true;
				}
			}
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentCurrentJob: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentCurrentJob");
				return setAgentCurrentJob(agentId, jobId, metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentCurrentJob: aid " + agentId + "{}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return false;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentPushed(Integer agentId) {
		logger.info("setAgentPushed " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentStatus(agentId, Constant.AGENT_STATUS_PUSH);
			agentDAO.updateAgentSubmittedTimestamp(agentId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentPushed: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentPushed");
				setAgentPushed(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentPushed: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Map<String, Boolean> sendAgentSignal(Integer agentId, Integer jobId) {
		logger.info("sendAgentSignal " + agentId + " " + jobId);
		Map<String, Boolean> signal = new HashMap<String, Boolean>(2);
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentRunningTime(agentId);
			daoFactory.onlyCommit(conn);

			// int ceId = agentDAO.readAgentCEId(agentId);
			// if (ceId != -1) {
			// CEDAO ceDAO = daoFactory.getCEDAO(conn);
			// ceDAO.updateCERunningTime(ceId, agentId);
			// }

			if (jobId != -1) {
				JobDAO jobDAO = daoFactory.getJobDAO(conn);
				String status = jobDAO.readJobStatus(jobId);
				if (status.equals(Constant.JOB_STATUS_RUN)) {
					jobDAO.updateJobRunningTime(jobId);
				}
				signal.put(Constant.JOB_STOP, jobDAO.readJobStop(jobId));
			} else {
				signal.put(Constant.JOB_STOP, false);
			}

			signal.put(Constant.AGENT_QUIT, agentDAO.readAgentQuit(agentId));

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to sendAgentSignal: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry sendAgentSignal");
				return sendAgentSignal(agentId, jobId);
			}
		} catch (Exception e) {
			logger.error("Failed to send AgentSignal: {}", e.toString());
			daoFactory.rollback(conn);
			throw new RuntimeException();
		} finally {
			daoFactory.endTransaction(conn);
		}

		return signal;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentStatus(Integer agentId, String status) {
		logger.info("setAgentStatus " + agentId + " " + status);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			if(!status.equals(Constant.AGENT_STATUS_NEW) && !status.equals(Constant.AGENT_STATUS_RUN) && !status.equals(Constant.AGENT_STATUS_SUB)){
				agentDAO.updateAgentFlag(agentId, true);
			}
			
			agentDAO.updateAgentStatus(agentId, status);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentStatus: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentStatus");
				setAgentStatus(agentId, status);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentStatus: aid " + agentId + "{}", e.toString());
			daoFactory.rollback(conn);
			throw new RuntimeException();
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentCE(Integer agentId, String ceName) {
		logger.info("setAgentCE " + agentId + " " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentCE(agentId, ceName);
			// CEDAO ceDAO = daoFactory.getCEDAO(conn);
			// ceDAO.updateCENumAgentSubmitTry(ceName);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentCE: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentCE");
				setAgentCE(agentId, ceName);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentCE: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAgentLastSignal(Integer agentId, String date) {
		logger.info("setAgentLastSignal " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentLastSignal(agentId, date);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAgentLastSignal: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAgentLastSignal");
				setAgentLastSignal(agentId, date);
			}
		} catch (Exception e) {
			logger.error("Failed to set AgentLastSignal: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}
	
	@Override
	public void setAgentValidInit(int serviceInfra) {
		logger.info("setAgentValidInit ");

		//Unimplemented
	}


	/**
	 * 
	 * @return
	 */
	@Override
	public boolean reportAgentFailure(Integer agentId) {
		logger.info("reportAgentFailure " + agentId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentFail(agentId);

			// int ceId = agentDAO.readAgentCEId(agentId);
			// if (ceId != -1) {
			// CEDAO ceDAO = daoFactory.getCEDAO(conn);
			// ceDAO.updateCERunningTime(ceId, agentId);
			// }
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to reportAgentFailure: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry reportAgentFailure");
				return reportAgentFailure(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to report AgentFailure: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void increaseAgentNumJobs(Integer agentId) {
		logger.info("increaseNumJobs " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentNumJobs(agentId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to increaseAgentNumJobs: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry increaseAgentNumJobs");
				increaseAgentNumJobs(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to increase NumJobs: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void finishKSCAgent(Integer agentId, long runningTime) {
		logger.info("finishKSCAgent " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateKSCAgentFinish(agentId, runningTime);
			int ceId = agentDAO.readAgentCEId(agentId);
			if (ceId != -1) {
				CEDAO ceDAO = daoFactory.getCEDAO(conn);
				ceDAO.updateCERunningTime(ceId, agentId);
			}
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to finishKSCAgent: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry finishKSCAgent");
				finishKSCAgent(agentId, runningTime);
			}
		} catch (Exception e) {
			logger.error("Failed to finish KSCAgent: aid " + agentId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	// @Override
	// public void increaseNumAgentsKSC() {
	// logger.info("increaseNumAgentsKSC");
	//
	// String voName = "KSC";
	// Connection conn = daoFactory.beginTransaction();
	// try {
	// ServiceInfraDAO voDAO = daoFactory.getVODAO(conn);
	// voDAO.updateVONumAgentsWaitingToRun(voDAO.readServiceInfraId(voName), 1);
	//
	// daoFactory.endTransaction(conn);
	// } catch (Exception e) {
	// daoFactory.rollback(conn);
	// logger.error("Failed to increase NumAgentsKSC: {}", e.toString());
	// }
	// }

	// @Override
	// public void decreaseNumAgentsKSC() {
	// logger.info("decreaseNumAgentsKSC");
	//
	// String voName = "KSC";
	// Connection conn = daoFactory.beginTransaction();
	// try {
	// ServiceInfraDAO voDAO = daoFactory.getVODAO(conn);
	// voDAO.updateVONumAgentsWaitingToRun(voDAO.readVOId(voName), -1);
	//
	// daoFactory.endTransaction(conn);
	// } catch (Exception e) {
	// daoFactory.rollback(conn);
	// logger.error("Failed to decrease NumAgentsKSC: {}", e.toString());
	// }
	// }

	// @Override
	// public boolean needToSubmitAgentKSC() {
	// logger.info("needToSubmitAgentKSC");
	//
	// String voName = "KSC";
	// boolean ret = false;
	// Connection conn = daoFactory.beginTransaction();
	// try {
	// ServiceInfraDAO voDAO = daoFactory.getVODAO(conn);
	// int num = voDAO.readVONumAgentsWaitingToRun(voDAO.readVOId(voName));
	// if(num>0){
	// ret = true;
	// }
	//
	// daoFactory.endTransaction(conn);
	// } catch (Exception e) {
	// daoFactory.rollback(conn);
	// logger.error("Failed to decrease NumAgentsKSC: {}", e.toString());
	// }
	//
	// return ret;
	// }

	/**
	 * 
	 * @return
	 */
	@Override
	public void startJobKSC(Integer jobId) {
		logger.info("startJobKSC " + jobId);

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobStart(jobId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to startJobKsc: jid " + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry startJobKSC");
				startJobKSC(jobId);
			}
		} catch (Exception e) {
			logger.error("Failed to start JobKSC: jid {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void finishJobKSC(Integer agentId, Integer jobId, long runningTime) {
		logger.info("finishJobKSC " + agentId + " " + jobId + " " + runningTime);

		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			jobDAO.updateJobFinish(jobId);

			MetaJobDAO metaJobDAO = daoFactory.getMetaJobDAO(conn);
			int metaJobId = jobDAO.readJobMetaJobId(jobId);
			metaJobDAO.updateAddMetaJobNum(metaJobId, -1);
			// int num = metaJobDAO.readMetaJobNum(metaJobId);

			// if(num == 0){
			// metaJobDAO.updateMetaJobLastUpdateTime(metaJobId);
			// }

			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateAgentNumJobs(agentId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to finishJobKSC: jid " + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry finishJobKSC");
				finishJobKSC(agentId, jobId, runningTime);
			}
		} catch (Exception e) {
			logger.error("Failed to finish JobKSC: jid {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void reportKSCAgentFailure(Integer agentId, long runningTime) {
		logger.info("reportKSCAgentFailure " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentDAO.updateKSCAgentFail(agentId, runningTime);

			int ceId = agentDAO.readAgentCEId(agentId);
			if (ceId != -1) {
				CEDAO ceDAO = daoFactory.getCEDAO(conn);
				ceDAO.updateCERunningTime(ceId, agentId);
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to reportKSCAgentFailure: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry reportKSCAgentFailure");
				reportKSCAgentFailure(agentId, runningTime);
			}
		} catch (Exception e) {
			logger.error("Failed to report KSCAgentFailure: aid " + agentId + "{}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer addApplication(String name) {
		logger.info("addApplication " + name);

		int id = -1;
		Connection conn = daoFactory.beginTransaction();
		try {
			ApplicationDAO applicationDAO = daoFactory.getApplicationDAO(conn);
			id = applicationDAO.createApplication(name);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to addApplication: name " + name + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry addApplication");
				return addApplication(name);
			}
		} catch (Exception e) {
			logger.error("Failed to add Application name {} : {}", name, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return id;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getApplicationId(String name) {
		logger.info("getApplicationId " + name);

		int id = -1;
		Connection conn = daoFactory.beginTransaction();
		try {
			ApplicationDAO applicationDAO = daoFactory.getApplicationDAO(conn);
			id = applicationDAO.readAppId(name);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to getApplicationId: name " + name + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry getApplicationId");
				return getApplicationId(name);
			}
		} catch (Exception e) {
			logger.error("Failed to get ApplicationId name {} : {}", name, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return id;
	}

	/**
	 * 
	 * @return
	 */
	// @Override
	// public boolean checkApplication(String name) {
	// logger.info("checkApplication " + name);
	//
	// boolean ret = false;
	// Connection conn = daoFactory.beginTransaction();
	// try {
	// ApplicationDAO applicationDAO = daoFactory.getApplicationDAO(conn);
	// id = applicationDAO.createApplication(name);
	// } catch (SQLException e) {
	// int code = e.getErrorCode();
	// logger.error("Failed to addApplication: name " + name + ", code {}, {}",
	// code, e.toString());
	// logger.error("SQLState : {}", e.getSQLState());
	// daoFactory.rollback(conn);
	// if (code == 1213) {
	// logger.error("retry addApplication");
	// return addApplication(name);
	// }
	// } catch (Exception e) {
	// logger.error("Failed to add Application name {} : {}", name,
	// e.toString());
	// daoFactory.rollback(conn);
	// } finally {
	// daoFactory.endTransaction(conn);
	// }
	//
	// return id;
	// }

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer addResult(Integer jobId, Integer metaJobId, String LFN) {
		logger.info("addJobResult " + jobId + " " + LFN);

		Connection conn = daoFactory.beginTransaction();
		try {
			ResultDAO resultDAO = daoFactory.getResultDAO(conn);
			resultDAO.createResult(jobId, metaJobId, LFN);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to addResult: jid " + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry addResult");
				return addResult(jobId, metaJobId, LFN);
			}
		} catch (Exception e) {
			logger.error("Failed to add JobResult: jid {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
			return -1;
		} finally {
			daoFactory.endTransaction(conn);
		}
		return jobId;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean removeResults(Integer metaJobId) {
		logger.info("removeJobResults " + metaJobId);

		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			ResultDAO resultDAO = daoFactory.getResultDAO(conn);
			result = resultDAO.deleteResults(metaJobId);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to removeResults: mid " + metaJobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry removeResults");
				return removeResults(metaJobId);
			}
		} catch (Exception e) {
			logger.error("Failed to remove JobResults: mid {}, {}", metaJobId, e.toString());
			daoFactory.rollback(conn);
			return result;
		} finally {
			daoFactory.endTransaction(conn);
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getResults(Integer jobId) {
		logger.info("getResults " + jobId);

		Connection conn = daoFactory.beginTransaction();
		List<String> results = null;
		try {
			ResultDAO resultDAO = daoFactory.getResultDAO(conn);
			results = resultDAO.readLFN(jobId);
		} catch (Exception e) {
			logger.error("Failed to get Results: {}, {}", jobId, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return results;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer addUser(String dn, String name, String userId, String passwd, String infraMetric) {
		logger.info("addUser " + dn + " " + userId + " " + name);
		int id = -1;
		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			id = userDAO.createUser(dn, name, userId, passwd, infraMetric);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to addUser: name " + name + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry addUser");
				return addUser(dn, name, userId, passwd, infraMetric);
			}
		} catch (Exception e) {
			logger.error("Failed to add User: name {}, {}", name, e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return id;
	}

	// 해당 사용자의 패스워드를 조회
	/**
	 * 
	 * @return
	 */
	@Override
	public String getUserPasswd(String userId) {
		logger.info("getUserPasswd {}", userId);

		Connection conn = daoFactory.beginTransaction();
		String passwd = null;
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			passwd = userDAO.readUserPassword(userId);

		} catch (Exception e) {
			logger.error("Failed to get UserPasswd: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return passwd;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void deleteUser(Integer userId) {
		logger.info("deleteUser {}", userId);

		Connection conn = daoFactory.beginTransaction();

		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			userDAO.deleteUser(userId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to deleteUser: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry deleteUser");
				deleteUser(userId);
			}
		} catch (Exception e) {
			logger.error("Failed to delete User: uid " + userId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	// DatabaseImpl::checkUser
	/**
	 * 
	 * @return
	 */
	@Override
	public boolean checkUser(String userId) {
		logger.info("checkUserExist {}", userId);

		Connection conn = daoFactory.beginTransaction();
		boolean flag = false;
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			flag = userDAO.checkUser(userId);

		} catch (Exception e) {
			logger.error("Failed to check User: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return flag;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setUserPasswd(String userId, String pw) {
		logger.info("setUserPassword {}", userId);

		Connection conn = daoFactory.beginTransaction();
		// boolean flag= false;
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			userDAO.setUserPassword(userId, pw); // 수정

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setUserPasswd: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setUserPasswd");
				setUserPasswd(userId, pw);
			}
		} catch (Exception e) {
			logger.error("Failed to set UserPasswd: uid " + userId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<User> getUserObjectList() {
		logger.info("getUserObjectList {}");

		Connection conn = daoFactory.beginTransaction();
		List<User> usrList = null;

		UserDAO userDAO = daoFactory.getUserDAO(conn);

		try {
			usrList = userDAO.getUserObjectList();
		} catch (Exception e) {
			logger.error("Failed to get UserObjectList: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return usrList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getId(String userId) {
		logger.info("getId {}", userId);

		Connection conn = daoFactory.beginTransaction();
		int uid = -1;

		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			uid = userDAO.readUserId(userId);

		} catch (Exception e) {
			logger.error("Failed to get ID: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return uid;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public User getUserInfo(String userId) {

		logger.info("getUserInfo {}", userId);
		User user = null;
		Connection conn = daoFactory.beginTransaction();

		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			user = userDAO.getUserInfo(userId);
		} catch (Exception e) {
			logger.error("Failed to get Userinfo: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return user;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getUserKeepAgentNO(String userId) {

		logger.info("getUserKeepAgentNO {}", userId);

		int keepAgentNO = -1;
		Connection conn = daoFactory.beginTransaction();

		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			keepAgentNO = userDAO.readUserKeepAgentNO(userId);
		} catch (Exception e) {
			logger.error("Failed to get UserKeepAgentNO: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return keepAgentNO;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<ServiceInfra> getUserServiceInfra(String userId) {

		logger.info("getUserServiceInfra {}", userId);

		List<ServiceInfra> siList = new ArrayList<ServiceInfra>();
		Connection conn = daoFactory.beginTransaction();

		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);

			String temp = userDAO.readUserServiceInfra(userId);
			for (String si : temp.split(",")) {
				siList.add(serviceInfraDAO.readServiceInfra(Integer.parseInt(si)));
			}
		} catch (Exception e) {
			logger.error("Failed to get UserServiceInfra: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return siList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean setUserInfo(String userId, User user) {
		logger.info("setUserInfo {}", userId);
		boolean result = false;
		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			userDAO.updateUserInfo(user);
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setUserInfo: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setuserInfo");
				return setUserInfo(userId, user);
			}
		} catch (Exception e) {
			logger.error("Failed to setUserInfo: uid " + userId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;

	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setUserDN(String userId, String dn) {
		logger.info("setUserDN {}", userId);

		User usr = new User();
		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			usr = userDAO.getUserInfo(userId);
			usr.setDN(dn);
			userDAO.updateUserInfo(usr);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setUserDN: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setUserDN");
				setUserDN(userId, dn);
			}
		} catch (Exception e) {
			logger.error("Failed to set UserDN: uid " + userId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean setServiceId(String userId, String sid) {

		logger.info("setServiceId for  {}", userId);
		boolean result = false;
		User usr = new User();
		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			usr = userDAO.getUserInfo(userId);
			usr.setServiceInfraID(sid);
			userDAO.updateUserInfo(usr);
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setServiceId: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setServiceId");
				return setServiceId(userId, sid);
			}
		} catch (Exception e) {
			logger.error("Failed to set Service id: uid " + userId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean setUserName(String userId, String name) {
		logger.info("setUser Name {}", userId);
		boolean result = false;
		User usr = new User();
		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			usr = userDAO.getUserInfo(userId);
			usr.setName(name);
			userDAO.updateUserInfo(usr);
			result = true;

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setUserName: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setUserName");
				setUserName(userId, name);
			}
		} catch (Exception e) {
			logger.error("Failed to set User name: uid " + userId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean setUserKeepAgentNO(String userId, Integer keepAgentNO) {
		logger.info("setUserkeepAgentNO {}", userId);
		boolean result = false;

		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			userDAO.updateUserKeepAgentNO(userId, keepAgentNO);
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setUserKeepAgentNO: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setUserKeepAgentNO");
				return setUserKeepAgentNO(userId, keepAgentNO);
			}
		} catch (Exception e) {
			logger.error("Failed to set User KeepAgentNO: uid " + userId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean updateOTPflag(String userId, int otp_flag) {
		logger.info("update OTP flag {}", userId);
		boolean result = false;

		Connection conn = daoFactory.beginTransaction();
		try {
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			userDAO.updateUserOtpFlag(userId, otp_flag);
			result = true;
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to updateUserOtpFlag: uid " + userId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry updateUserOtpFlag");
				return updateOTPflag(userId, otp_flag);
			}
		} catch (Exception e) {
			logger.error("Failed to updatge User Otp_Flag : uid " + userId + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void updateWMSList(String sName, List<String> wmsList) {
		logger.info("updateWMSList, ServiceInfra: " + sName + " # of wmsList: " + wmsList.size());

		Connection conn = daoFactory.beginTransaction();
		try {
			int sId = -1;
			WMS wms = null;
			ServiceInfraDAO voDAO = daoFactory.getServiceInfraDAO(conn);
			sId = voDAO.readServiceInfraId(sName);
			if (sId == -1) {
				logger.info("New ServiceInfra added: " + sName);
				ServiceCodeDAO scDAO = daoFactory.getServiceCodeDAO(conn);
				int code = scDAO.readServiceCodeId(ResourceConstant.GRID);
				sId = voDAO.createServiceInfra(sName, code, -1, true);
			}

			WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);
			for (String wmsName : wmsList) {
				wms = wmsDAO.readWMSObject(wmsName);
				if (wms == null) {
					logger.info("New WMS added: " + wmsName);
					WMS newWMS = new WMS();
					newWMS.setName(wmsName);
					newWMS.setAvailable(true);
					newWMS.setBanned(false);

					wmsDAO.createWMSObject(newWMS, sId);
				} else {
					if (!wms.isBanned() && !wms.isAvailable()) {
						logger.info("WMS set to available: " + wmsName);
						wmsDAO.updateWMSAvailable(wms.getId(), true);
					}
				}
			}

			List<String> wmses = wmsDAO.readWMSesByVOName(sName);
			WMSCEDAO wmsceDAO = daoFactory.getWMSCEDAO(conn);

			for (String wmsName : wmses) {
				if (!wmsList.contains(wmsName)) {
					logger.info("WMS set to not available: " + wmsName);
					int wmsId = wmsDAO.readWMSId(wmsName);
					wmsceDAO.deleteWMSCE(wmsId);
					wmsDAO.deleteWMS(wmsId);
					// wmsDAO.updateWMSAvailable(wmsDAO.readWMSId(wmsName),
					// false);
				}
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to updateWMSList: code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry updateWMSList");
				updateWMSList(sName, wmsList);
			}
		} catch (Exception e) {
			logger.error("Failed to update WMSList : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setWMSInfo(String sName, String wmsName, long responseTime, List<String> ceList) {
		if (ceList.size() == 1 && ceList.get(0).equals("EMPTY")) {
			ceList.remove(0);
		}

		logger.info("setWMSInfo wmsName: " + wmsName + " responseTime: " + responseTime + " numCE:" + ceList.size());

		Connection conn = daoFactory.beginTransaction();
		try {
			int sId = -1;
			int wmsId = -1;
			CE ce = null;

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.info("New ServiceInfra added: " + sName);
				ServiceCodeDAO scDAO = daoFactory.getServiceCodeDAO(conn);
				int code = scDAO.readServiceCodeId(ResourceConstant.GRID);
				sId = serviceInfraDAO.createServiceInfra(sName, code, -1, true);
			}

			WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);
			wmsId = wmsDAO.readWMSId(wmsName);
			wmsDAO.updateWMSCEInfo(wmsId, responseTime, ceList.size());

			WMSCEDAO wmsceDAO = daoFactory.getWMSCEDAO(conn);
			wmsceDAO.deleteWMSCE(wmsId);

			for (String ceName : ceList) {
				CEDAO ceDAO = daoFactory.getCEDAO(conn);
				ce = ceDAO.readCEObject(ceName);

				if (ce == null) {
					logger.info("New CE added: " + ceName);
					CE newCE = new CE();
					newCE.setName(ceName);
					newCE.setAvailable(true);
					newCE.setBanned(false);
					newCE.setServiceInfraId(sId);

					int ceId = ceDAO.createCE(newCE);
					conn.commit();
					wmsceDAO.createWMSCE(wmsId, ceId);

				} else {
					int ceId = ce.getId();
					if (wmsceDAO.readWMSCE(wmsId, ceId) == -1) {
						wmsceDAO.createWMSCE(wmsId, ceId);
					}
					if (ce.isBanned() && ce.isAvailable()) {
						logger.info("Banned CE set to unavailable: " + ceName);
						ce.setAvailable(false);
					}

					if (!ce.isBanned() && !ce.isAvailable()) {
						logger.info("CE set to available: " + ceName);
						ce.setAvailable(true);
					}

					ceDAO.updateCEAvailable(ce.getId(), ce.isAvailable());
				}
			}

			if (ceList.size() == 0 || responseTime > 30) {
				wmsDAO.updateWMSAvailable(wmsId, false);
				logger.info("This WMS set to not available");
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setWMSInfo: code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setWMSInfo");
				setWMSInfo(sName, wmsName, responseTime, ceList);
			}
		} catch (Exception e) {
			logger.error("Failed to set WMSInfo : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getAvailableWMSList(String sName) {
		logger.info("getAvailableWMSList ServiceInfra: " + sName);

		Connection conn = daoFactory.beginTransaction();
		List<String> wmsList = null;
		try {
			int sId = -1;

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.info("New ServiceInfra added: " + sName);
				ServiceCodeDAO scDAO = daoFactory.getServiceCodeDAO(conn);
				int code = scDAO.readServiceCodeId(ResourceConstant.GRID);
				sId = serviceInfraDAO.createServiceInfra(sName, code, -1, true);
			}

			WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);

			wmsList = wmsDAO.readWMSesAvailable(sId, true);

		} catch (Exception e) {
			logger.error("Failed to get AvailableWMSList : {}", e.toString());
			daoFactory.rollback(conn);

		} finally {
			daoFactory.endTransaction(conn);
		}

		return wmsList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String nextWMS(String ceName) {
		logger.info("nextWMS ceName:" + ceName);

		Connection conn = daoFactory.beginTransaction();

		WMS nextWMS = null;
		try {
			int ceId = -1;
			Set<Integer> wmsSet = null;
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceId = ceDAO.readCEId(ceName);

			WMSCEDAO wmsceDAO = daoFactory.getWMSCEDAO(conn);
			wmsSet = wmsceDAO.readWMSes(ceId);

			WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);
			for (Integer wmsId : wmsSet) {
				WMS wms = wmsDAO.readWMSObject(wmsId);
				if (wms.isAvailable() && !wms.isBanned()) {
					long responseTime = wms.getResponseTime();
					int count = wms.getCount();

					if (nextWMS == null) {
						nextWMS = wms;
						continue;
					}

					if (count < nextWMS.getCount()) {
						nextWMS = wms;
					} else if (count == nextWMS.getCount() && responseTime < nextWMS.getResponseTime()) {

						nextWMS = wms;
					}
				}
			}

			wmsDAO.updateWMSNumCount(nextWMS.getId(), 1);

			logger.info("nextWMS:" + nextWMS.getName() + " id:" + nextWMS.getId() + " responseTime:" + nextWMS.getResponseTime());
		} catch (Exception e) {
			logger.error("Failed to get nextWMS : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return nextWMS.getName();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getAvailableWMSListForCE(String ceName) {
		logger.info("getAvailableWMSListForCE CE: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		List<String> wmsList = new ArrayList<String>();
		int ceId = -1;
		try {
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceId = ceDAO.readCEId(ceName);
			if (ceId == -1) {
				logger.error("Unknown CE");
				return null;
			}

			WMSCEDAO wmsceDAO = daoFactory.getWMSCEDAO(conn);
			Set<Integer> wmsSet = wmsceDAO.readWMSes(ceId);

			WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);

			for (Integer wmsId : wmsSet) {
				WMS wms = wmsDAO.readWMSObject(wmsId);
				if (wms.isAvailable() && !wms.isBanned()) {
					wmsList.add(wms.getName());
				}
			}

		} catch (Exception e) {
			logger.error("Failed to get AvailableWMSListForCE: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return wmsList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String nextCE(String sName) {
		logger.info("nextCE ServiceInfraName:" + sName);

		Connection conn = daoFactory.beginTransaction();

		CE nextCE = null;
		try {
			int maxRunningTimeLimit = 0;
			long minValue = 999999999L;

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			int sId = serviceInfraDAO.readServiceInfraId(sName);

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			List<CE> ces = ceDAO.readCEObjectSet(sId, maxRunningTimeLimit);

			for (CE ce : ces) {
				if (ce.getWaitingTime() < minValue) {
					minValue = ce.getWaitingTime();
					nextCE = ce;
				}
			}

			if (nextCE == null) {
				logger.info("no CE");
				return null;
			} else {
				logger.info("nextCE:" + nextCE.getName() + " id:" + nextCE.getId() + " waitingTime:" + minValue);
			}

		} catch (Exception e) {
			logger.error("Failed to get nextCE : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return nextCE.getName();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<CE> getAvailableCEObjectList(String sName) {
		logger.info("getAvailableCEObjectList ServiceInfra: " + sName);

		Connection conn = daoFactory.beginTransaction();

		List<CE> ceList = null;
		try {

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			int sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.info("The ServiceInfra does not exist");
				return null;
			}

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceList = ceDAO.readCEObjectSet(sId, 0);

		} catch (Exception e) {
			logger.error("Failed to get AvailableCEObjectList : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ceList;
	}


	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getIntelligentCEList(String sName, Integer waitingTime, Integer numAgentRunning, Integer numAgentSubmitFailure, Integer waitingJob) {
		logger.info("getIntelligentCEList ServiceInfra: " + sName + "  waitingTime:" + waitingTime + "  numAgentRunning:" + numAgentRunning
				+ "  numAgentSubmitFailure:" + numAgentSubmitFailure + "  waitingJob:" + waitingJob);

		Connection conn = daoFactory.beginTransaction();

		List<String> ceList = null;
		try {

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			int sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.info("The ServiceInfra does not exist");
				return ceList;
			}

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceList = ceDAO.readCEsIntelligent(sId, waitingTime, numAgentRunning, numAgentSubmitFailure, waitingJob);

		} catch (Exception e) {
			logger.error("Failed to get IntelligentCEList : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ceList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEFreeCPUTotal(Integer serviceInfra) {
		logger.info("getCEFreeCPUTotal ServiceInfra: " + serviceInfra);

		Connection conn = daoFactory.beginTransaction();

		int fcpu = 0;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			fcpu = ceDAO.readCEFreeCPUTotal(serviceInfra);

		} catch (Exception e) {
			logger.error("Failed to get CEFreeCPUTotal : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return fcpu;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEFreeCPU(String ceName) {
		logger.info("getCEFreeCPU ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int fcpu = 0;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			fcpu = ceDAO.readCEFreeCPU(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CEFreeCPU : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return fcpu;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEAliveAgent(String ceName) {
		logger.info("getCEAliveAgent : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int no = -1;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			no = ceDAO.readCEAliveAgent(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CEAliveAgent CEName : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return no;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEAliveAgentFromAMEnv(String siSet) {
		logger.info("getCEAliveAgentFromAMEnv : " + siSet);

		Connection conn = daoFactory.beginTransaction();

		int no = 0;
		try {
			String[] sid = siSet.split(",");
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			for (String s : sid) {
				no = +ceDAO.readCEAliveAgent(Integer.parseInt(s));
			}

		} catch (Exception e) {
			logger.error("Failed to get CEAliveAgent ServiceInfraMetric : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return no;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEEqualCnt(String ceName) {
		logger.info("getCEEqualCnt : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int cnt = -1;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			cnt = ceDAO.readCEEqualCnt(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CEEqualCnt : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return cnt;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEZeroCnt(String ceName) {
		logger.info("getCEZeroCnt : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int cnt = -1;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			cnt = ceDAO.readCEZeroCnt(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CEZeroCnt : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return cnt;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCEPriority(String ceName) {
		logger.info("getCEPriority : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int no = -1;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			no = ceDAO.readCEPriority(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CEPriority : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return no;
	}

	// 주어진 CE 이름으로 CE.limitClass 값을 조회
	@Override
	public String getCELimitClass(String ceName) {
		logger.info("getCELimitClass ceName : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		String limitClass = null;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			limitClass = ceDAO.readCELimitClass(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CELimitClass : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return limitClass;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getCENameList(String serviceInfra, boolean available, boolean banned) {
		logger.info("getCEList ServiceInfra : " + serviceInfra + ", Available : " + available);

		Connection conn = daoFactory.beginTransaction();

		List<String> ceList = null;
		try {

			ServiceInfraDAO siDAO = daoFactory.getServiceInfraDAO(conn);
			int siID = siDAO.readServiceInfraId(serviceInfra);
			
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceList = ceDAO.readCEName(siID, available, banned);

		} catch (Exception e) {
			logger.error("Failed to get CE List : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ceList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getCELimitCPU(String ceName) {
		logger.info("getCELimitCPU : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int num = 0;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			num = ceDAO.readCELimitCPU(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CELimitCPU : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	@Override
	public List<CE> getCEObjectList(int serviceInfra, boolean avail, boolean banned) {
		logger.info("getCEObjectList : " + serviceInfra + ", avail " + avail + ", banned " + banned);

		Connection conn = daoFactory.beginTransaction();

		List<CE> ceList = null;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceList = ceDAO.readCEObjectList(serviceInfra, avail, banned);

		} catch (Exception e) {
			logger.error("Failed to get CEObjectList : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ceList;
	}
	
	@Override
	public CE getCEObject(String name) {
		logger.info("getCEObject : " + name);

		Connection conn = daoFactory.beginTransaction();

		CE ce = null;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ce = ceDAO.readCEObject(name);

		} catch (Exception e) {
			logger.error("Failed to get CEObject : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ce;
	}
	
	@Override
	public CE getCEObject(int id) {
		logger.info("getCEObject : " + id);

		Connection conn = daoFactory.beginTransaction();

		CE ce = null;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ce = ceDAO.readCEObject(id);

		} catch (Exception e) {
			logger.error("Failed to get CEObject : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return ce;
	}

	@Override
	public Integer getCETotalCPU(String ceName) {
		logger.info("getCETotalCPU : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int num = 0;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			num = ceDAO.readCETotalCPU(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CELimitCPU : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return num;
	}

	@Override
	public Integer getCEAvailableTimeDiff(String ceName) {
		logger.info("getCEAvailableTimeDiff : " + ceName);

		Connection conn = daoFactory.beginTransaction();

		int diff = 0;
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			diff = ceDAO.readCEAvailableTimeDiff(ceName);

		} catch (Exception e) {
			logger.error("Failed to get CEAvailableTimeDiff : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return diff;
	}
	
	@Override
	public Integer getCEServiceInfraId(int id) {
		logger.info("getCEServiceInfraId : " + id);

		Connection conn = daoFactory.beginTransaction();
		int sid = -1;
		
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			sid = ceDAO.readCEServiceInfraId(id);

		} catch (Exception e) {
			logger.error("Failed to get CEServiceInfraId : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return sid;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAllCEsUnavailable(String sName) {
		logger.info("setAllCEsUnavailable ServiceInfra: " + sName);

		Connection conn = daoFactory.beginTransaction();
		try {
			int sId = -1;

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.error("This ServiceInfra doesn't exist");
				return;
			}

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEsAvailable(sId, false);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setAllCesUnavailable: sName " + sName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAllCEsUnavailable");
				setAllCEsUnavailable(sName);
			}
		} catch (Exception e) {
			logger.error("Failed to set AllCEsUnavailable : sName " + sName + " {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEAliveAgentAdd(Integer agentId, Integer num) {
		logger.info("setCEAliveAgentAdd " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);

			ceDAO.updateCEAliveAgentAdd(agentDAO.readAgentCEId(agentId), num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEAliveAgentAdd: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEAliveAgentAdd");
				setCEAliveAgentAdd(agentId, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEAliveAgent : aid " + agentId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEAliveAgentAddFromJob(Integer jobId, Integer num) {
		logger.info("setCEAliveAgentAddFromJob " + jobId);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			JobDAO jobDAO = daoFactory.getJobDAO(conn);

			ceDAO.updateCEAliveAgentAdd(jobDAO.readJobCEId(jobId), num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEAliveAgentAddFromJob: jid " + jobId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEAliveAgentAddFromJob");
				setCEAliveAgentAddFromJob(jobId, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEAliveAgentFromJob : jid " + jobId + ", {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEAliveAgentInit() {
		logger.info("setCEAliveAgentInit");

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEAliveAgentInit();

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEAliveAgentInit: code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEAliveAgentInit");
				setCEAliveAgentInit();
			}
		} catch (Exception e) {
			logger.error("Failed to set CEAliveAgentInit : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEZeroCnt(String ceName, Integer num) {
		logger.info("setCEZeroCnt ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEZeroCnt(ceName, num);
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEZeroCnt: ceName " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEZeroCnt");
				setCEZeroCnt(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEZeroCnt : {}", e.toString());
			daoFactory.rollback(conn);

		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEZeroCntAll(Integer num) {
		logger.info("setCEZeroCntAll num: " + num);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEZeroCntAll(0);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEZeroCntAll: num " + num + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEZeroCntAll");
				setCEZeroCntAll(num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEZeroCntAll : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCESelectCntAll(Integer num) {
		logger.info("setCESelectCntAll num: " + num);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCESelectCntAll(0);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCESelectCntAll: num " + num + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCESelectCntAll");
				setCESelectCntAll(num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CESelectCntAll : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEZeroCntAdd(String ceName, Integer num) {
		logger.info("setCEZeroCntAdd ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEZeroCntAdd(ceName, num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEZeroCntAdd: ceName " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEZeroCntAdd");
				setCEZeroCntAdd(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEZeroCntAdd : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEPriority(String ceName, Integer num) {
		logger.info("setCEPriority ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEPriority(ceName, num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEPriority: ceName " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEPriority");
				setCEPriority(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEPriority : {}", e.toString());
			daoFactory.rollback(conn);

		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEPriorityAdd(String ceName, Integer num) {
		logger.info("setCEPriorityAdd ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEPriorityAdd(ceName, num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEPriorityAdd: ceName " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEPriorityAdd");
				setCEPriorityAdd(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEPriorityAdd : {}", e.toString());
			daoFactory.rollback(conn);

		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEAvailable(String ceName, boolean avail) {
		logger.info("setCEAvailable ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEAvailable(ceDAO.readCEId(ceName), avail);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEAvailable: ceName " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEAvailable");
				setCEAvailable(ceName, avail);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEAvailable : {}", e.toString());
			daoFactory.rollback(conn);

		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCELimitCPU(String ceName, Integer num) {
		logger.info("setCELimitCPU ceName: " + ceName + ", num : " + num);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCELimitCPU(ceName, num);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCELimitCPU: ceName " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCELimitCPU");
				setCELimitCPU(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to set CELimitCPU : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setCEWaitingTime(Integer agentId) {
		logger.info("setCEWaitingTime agentId: " + agentId);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEWaitingTime(agentId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEWaitingTime: aid " + agentId + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEWaitingTime");
				setCEWaitingTime(agentId);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEWaitingTime : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	@Override
	public void setCEAvailableUpdateTime(String ceName) {
		logger.info("setCEAvailableUpdateTime ceName: " + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEAvailableUpdateTime(ceName);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEAvailableUpdateTime: cename " + ceName + ", code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEAvailableUpdateTime");
				setCEAvailableUpdateTime(ceName);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEAvailableUpdateTime : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}
	
	@Override
	public void setCEInfo(List<CE> ceList) {
		logger.info("setCEInfo ");

		Connection conn = daoFactory.beginTransaction();
		try {

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			ceDAO.updateCEObjectList(ceList);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to setCEInfo : code {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setCEInfo");
				setCEInfo(ceList);
			}
		} catch (Exception e) {
			logger.error("Failed to set CEInfo : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean updateCEInfo(String sName, List<String> ceList) {
		logger.info("updateCEInfo ServiceInfra: " + sName + " " + ceList.size() + " CEs");

		boolean ret = false;
		
		Connection conn = daoFactory.beginTransaction();
		try {
			int sId = -1;

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.error("This ServiceInfra doesn't exist");
				return ret;
			}

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			for (String ceInfo : ceList) {

				String[] info = ceInfo.split(" ");
				logger.info(info[0] + " " + info[1] + " " + info[2] + " " + info[3] + " " + info[4]);

				String ceName = info[0];
				int totalCPU = Integer.parseInt(info[1]);
				int freeCPU = Integer.parseInt(info[2]);
				int runningJob = Integer.parseInt(info[3]);
				int waitingJob = Integer.parseInt(info[4]);

				CE ce = ceDAO.readCEObject(ceName);

				if (ce != null && ce.getServiceInfraId() == sId) {
					ce.setTotalCPU(totalCPU);
					ce.setFreeCPU(freeCPU);
					ce.setRunningJob(runningJob);
					ce.setWaitingJob(waitingJob);

					ceDAO.updateCEObject(ce);
					logger.info(ceName + " " + totalCPU + " " + freeCPU + " " + runningJob + " " + waitingJob);
					
					ret = true;
				} else {
					logger.error("CE not exist: " + ceName);
				}

			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to updateCEInfo: sName " + sName + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry updateCEInfo");
				updateCEInfo(sName, ceList);
			}
		} catch (Exception e) {
			logger.error("Failed to update CEInfo : {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		
		return ret;
	}

	/**
	 * llmcst 로 부터 얻은 PLSI 자원에 대한 정보를 업데이트한다. AgentManager updateCEInfo() 함수로 부터
	 * 호출된다.
	 */
	@Override
	public void updateSCCEInfo(String sName, List<String> ceList) {
		logger.info("updateSCCEInfo ServiceInfra: " + sName + " " + ceList.size() + " CEs");

		Connection conn = daoFactory.beginTransaction();
		try {
			int sId = -1;

			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.error("This ServiceInfra doesn't exist");
				return;
			}

			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			for (String plsi : ceList) {

				String ceName = plsi.split(" ")[0];
				CE ce = ceDAO.readCEObject(ceName);

				if (ce == null) {
					logger.info("New CE added: " + ceName);

					CE newCE = new CE();
					newCE.setName(ceName);
					newCE.setAvailable(true);
					newCE.setBanned(false);
					newCE.setServiceInfraId(sId);

					ceDAO.createCE(newCE);
					conn.commit();

				} else {

					if (ce.isBanned() && ce.isAvailable()) {
						logger.info("Banned CE set to unavailable: " + ceName);
						ce.setAvailable(false);
					}

					// if (!ce.isBanned() && !ce.isAvailable()) {
					// logger.info("CE set to available: " + ceName);
					// ce.setAvailable(true);
					// }

					ceDAO.updateCEAvailable(ce.getId(), ce.isAvailable());
				}
			}

			for (String ceInfo : ceList) {

				String[] info = ceInfo.split(" ");
				logger.info(info[0] + " " + info[1] + " " + info[2] + " " + info[3] + " " + info[4]);

				String ceName = info[0];
				int totalCPU = Integer.parseInt(info[1]);
				int freeCPU = Integer.parseInt(info[2]);
				int runningJob = Integer.parseInt(info[3]);
				int waitingJob = Integer.parseInt(info[4]);

				CE ce = ceDAO.readCEObject(ceName);

				if (ce != null && ce.getServiceInfraId() == sId) {
					ce.setTotalCPU(totalCPU);
					ce.setFreeCPU(freeCPU);
					ce.setRunningJob(runningJob);
					ce.setWaitingJob(waitingJob);

					ceDAO.updateCEObject(ce);
					logger.info(ceName + " " + totalCPU + " " + freeCPU + " " + runningJob + " " + waitingJob);
				} else {
					logger.error("CE not exist: " + ceName);
				}

			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to updateSCCEInfo: sName " + sName + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry updateSCCEInfo");
				updateSCCEInfo(sName, ceList);
			}
		} catch (Exception e) {
			logger.error("Failed to update SCCEInfo : {}", e.toString());
			daoFactory.rollback(conn);

		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void initCESubmitCount(String sName) {
		logger.info("initCESubmitCount ServiceInfra: " + sName);

		Connection conn = daoFactory.beginTransaction();
		try {
			ServiceInfraDAO serviceInfraDAO = daoFactory.getServiceInfraDAO(conn);
			int sId = serviceInfraDAO.readServiceInfraId(sName);

			if (sId == -1) {
				logger.error("The ServiceInfra does not exist");
				return;
			}

			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			ceDAO.updateCEsInitSubmitCount(sId);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to init CESubmitCount: sName " + sName + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry initCESubmtCount");
				initCESubmitCount(sName);
			}
		} catch (Exception e) {
			logger.error("Failed to init CESubmitCount: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean increaseCESubmitCount(String ceName, Integer num) {
		logger.info("increaseCESubmitCount ceName: " + ceName + ", " + num);

		Connection conn = daoFactory.beginTransaction();
		boolean result = false;
		try {
			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			// if (ceDAO.readCEFreeCPU(ceName)>1) {
			ceDAO.updateCESubmitCountAdd(ceName, num);
			result = true;
			;
			// }

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to increase CESubmitCount: ceName " + ceName + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry increaseCESumbitCount");
				return increaseCESubmitCount(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to increase CESubmitCount: {}", e.toString());
			daoFactory.rollback(conn);
			return false;
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}
	
	@Override
	public boolean increaseCESelectCount(String ceName, Integer num) {
		logger.info("increaseCESelectCount ceName: " + ceName + ", " + num);

		Connection conn = daoFactory.beginTransaction();
		boolean result = false;
		try {
			CEDAO ceDAO = daoFactory.getCEDAO(conn);

			// if (ceDAO.readCEFreeCPU(ceName)>1) {
			ceDAO.updateCESelectCountAdd(ceName, num);
			result = true;
			;
			// }

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to increase CESelectCount: ceName " + ceName + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry increaseCESelectCount");
				return increaseCESelectCount(ceName, num);
			}
		} catch (Exception e) {
			logger.error("Failed to increase CESelectCount: {}", e.toString());
			daoFactory.rollback(conn);
			return false;
		} finally {
			daoFactory.endTransaction(conn);
		}

		return result;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer checkRunningZombieAgentJob(Integer timelimit, int serviceInfra) {
		logger.info("checkRunningZombieAgent & CurrentJob timelimit: " + timelimit);

		Connection conn = daoFactory.beginTransaction();
		// List<Integer> list = null;
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			CEDAO ceDAO = daoFactory.getCEDAO(conn);
			List<Integer> idList = agentDAO.readAgentList(Constant.AGENT_STATUS_RUN, false, serviceInfra, timelimit);
			
			for(Integer agentId : idList){
				
				int jobId = agentDAO.readAgentCurrentJob(agentId);
				reEnqueueJob(jobId);
				agentDAO.updateAgentFlag(agentId, true);
				ceDAO.updateCEAliveAgentAdd(agentDAO.readAgentCEId(agentId), -1);
				agentDAO.updateAgentStatus(agentId, Constant.AGENT_STATUS_RUNZOM);
				daoFactory.onlyCommit(conn);
				
			}
			
			
			
//			num = agentDAO.updateAgentStatusZombie(Constant.AGENT_STATUS_RUN, Constant.AGENT_STATUS_RUNZOM, timelimit, serviceInfra);
//			daoFactory.onlyCommit(conn);
//
//			if (num > 0){ 
//				CEDAO ceDAO = daoFactory.getCEDAO(conn);
//				agentDAO = daoFactory.getAgentDAO(conn);
//					for (Integer agentId : agentDAO.readAgentList(Constant.AGENT_STATUS_RUNZOM, false, serviceInfra)) {
//						logger.info("RunningZombieAgent : " + agentId);
//						
//						int jobId = agentDAO.readAgentCurrentJob(agentId);
//						
//						reEnqueueJob(jobId);
//						agentDAO.updateAgentFlag(agentId, true);
//						daoFactory.onlyCommit(conn);
//					}
//
//			}
		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to check RunningZombieAgentJob: timelimit " + timelimit + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry checkRunningZombieAgentJob");
				return checkRunningZombieAgentJob(timelimit, serviceInfra);
			}
		} catch (Exception e) {
			logger.error("Failed to check RunningZombieAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer checkNewZombieAgent(Integer timelimit, int serviceInfra) {
		logger.info("checkNewZombieAgent timelimit: " + timelimit);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.updateAgentStatusZombie(Constant.AGENT_STATUS_NEW, Constant.AGENT_STATUS_NEWZOM, timelimit, serviceInfra);
			daoFactory.onlyCommit(conn);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to check NewZombieAgent: timelimit " + timelimit + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry checkNewZombieAgent");
				return checkNewZombieAgent(timelimit, serviceInfra);
			}
		} catch (Exception e) {
			logger.error("Failed to check NewZombieAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer checkSubmittedZombieAgent(Integer timelimit, int serviceInfra) {
		logger.info("checkSubmittedZombieAgent timelimit: " + timelimit);

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.updateAgentStatusZombie(Constant.AGENT_STATUS_SUB, Constant.AGENT_STATUS_SUBZOM, timelimit, serviceInfra);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to check SubmittedZombieAgent: timelimit " + timelimit + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry checkSubmittedZombieAgent");
				return checkSubmittedZombieAgent(timelimit, serviceInfra);
			}
		} catch (Exception e) {
			logger.error("Failed to check SubmittedZombieAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer checkSubmitErrorAgent() {
		logger.info("checkSubmittErrorAgent");

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentNumStatus(Constant.AGENT_STATUS_SUBERR);

		} catch (Exception e) {
			logger.error("Failed to check Submit-Error: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer checkDoneAgent() {
		logger.info("checkDoneAgent");

		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			num = agentDAO.readAgentNumStatus(Constant.AGENT_STATUS_DONE);

		} catch (Exception e) {
			logger.error("Failed to check Done: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return num;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean checkAgentSleep(Integer agentId) {
		logger.info("checkAgentSleep {}", agentId);

		Connection conn = daoFactory.beginTransaction();
		boolean sleep = false;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			sleep = agentDAO.readAgentSleep(agentId);

		} catch (Exception e) {
			logger.error("Failed to check AgentSleep: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return sleep;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean checkAgentQuit(Integer agentId) {
		logger.info("checkAgentQuit {}", agentId);

		Connection conn = daoFactory.beginTransaction();
		boolean quit = false;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			quit = agentDAO.readAgentQuit(agentId);

		} catch (Exception e) {
			logger.error("Failed to check AgentQuit: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return quit;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void reportSubmitError(Integer agentId, Integer metaJobId, String wmsName, String ceName, String errorMsg) {
		logger.info("reportSubmitError aid:" + agentId + ", mid:" + metaJobId + ", wms:" + wmsName + ", ce:" + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {
			SubmitErrorDAO submitErrorDAO = daoFactory.getSubmitErrorDAO(conn);
			submitErrorDAO.insertSubmitError(metaJobId, ceName, wmsName, errorMsg);

			if (agentId > 0) {
				AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
				agentDAO.updateAgentStatus(agentId, Constant.AGENT_STATUS_SUBERR);
				agentDAO.updateAgentFlag(agentId, true);
				agentDAO.updateAgentSubmittedTimestamp(agentId);
			}

			CEDAO ceDAO = null;
			if (ceName != null) {
				ceDAO = daoFactory.getCEDAO(conn);
				ceDAO.updateCENumAgentSubmitFailure(ceName);
			}
			// ceDAO.updateCESubmitCountAdd(ceName, -1);

			if (wmsName != null) {
				if (!wmsName.equals(ResourceConstant.GLITECREAM)) {

					// increase number
					WMSCEDAO wmsceDAO = daoFactory.getWMSCEDAO(conn);
					wmsceDAO.updateSubmitErrorNum(wmsName, ceName);

					WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);

					int submitErrorNum = wmsceDAO.readSubmitErrorNum(wmsName, ceName);

					int wmsId = wmsDAO.readWMSId(wmsName);
					int ceId = ceDAO.readCEId(ceName);

					logger.info("wms: " + wmsId + ", ce: " + ceId + " submitErrorNum: " + submitErrorNum);

					if (submitErrorNum >= 3) {
						// remove the entry
						wmsceDAO.deleteWMSCE(wmsId, ceId);
						logger.info("Entry removed");
					}
				}
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to report SubmitError: aid " + agentId + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry reportSubmitError");
				reportSubmitError(agentId, metaJobId, wmsName, ceName, errorMsg);
			}
		} catch (Exception e) {
			logger.error("Failed to report SubmitError: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void reportSubmitErrorMap(Map agentIdMap, Integer metaJobId, String wmsName, String ceName, String errorMsg) {
		logger.info("reportSubmitErrorMap :mid:" + metaJobId + ", wms:" + wmsName + ", ce:" + ceName);

		Connection conn = daoFactory.beginTransaction();
		try {
			SubmitErrorDAO submitErrorDAO = daoFactory.getSubmitErrorDAO(conn);
			submitErrorDAO.insertSubmitError(metaJobId, ceName, wmsName, errorMsg);

			if (agentIdMap != null) {
				AgentDAO agentDAO = daoFactory.getAgentDAO(conn);

				for (Object key : agentIdMap.keySet()) {
					agentDAO.updateAgentStatus((Integer) agentIdMap.get(key), Constant.AGENT_STATUS_SUBERR);
					agentDAO.updateAgentFlag((Integer) agentIdMap.get(key), true);
					agentDAO.updateAgentSubmittedTimestamp((Integer) agentIdMap.get(key));
				}
			}

			CEDAO ceDAO = null;
			if (ceName != null) {
				ceDAO = daoFactory.getCEDAO(conn);
				ceDAO.updateCENumAgentSubmitFailure(ceName);
				// ceDAO.updateCESubmitCountAdd(ceName, -1);
			}

			if (wmsName != null) {
				if (!wmsName.equals(ResourceConstant.GLITECREAM)) {

					// increase number
					WMSCEDAO wmsceDAO = daoFactory.getWMSCEDAO(conn);
					wmsceDAO.updateSubmitErrorNum(wmsName, ceName);

					WMSDAO wmsDAO = daoFactory.getWMSDAO(conn);

					int submitErrorNum = wmsceDAO.readSubmitErrorNum(wmsName, ceName);

					int wmsId = wmsDAO.readWMSId(wmsName);
					int ceId = ceDAO.readCEId(ceName);

					logger.info("wms: " + wmsId + ", ce: " + ceId + " submitErrorNum: " + submitErrorNum);

					if (submitErrorNum >= 3) {
						// remove the entry
						wmsceDAO.deleteWMSCE(wmsId, ceId);
						logger.info("Entry removed");
					}
				}
			}

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to report SubmitErrorMap: mid " + metaJobId + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry reportSubmitErrorMap");
				reportSubmitErrorMap(agentIdMap, metaJobId, wmsName, ceName, errorMsg);
			}
		} catch (Exception e) {
			logger.error("Failed to report SubmitErrorMap: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean getAMEnvAutoMode(Integer id) {
		logger.info("getAMAutoMode");

		Connection conn = daoFactory.beginTransaction();
		boolean mode = false;
		try {
			EnvDAO envDAO = daoFactory.getEnvDAO(conn);
			mode = envDAO.readAMEnvAutoMode(id);

		} catch (Exception e) {
			logger.error("Failed to get AMAutoMode: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return mode;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getAMEnvId(String name) {
		logger.info("getAMEnvId");

		Connection conn = daoFactory.beginTransaction();
		int id = -1;
		try {
			EnvDAO envDAO = daoFactory.getEnvDAO(conn);
			id = envDAO.readAMEnvId(name);

		} catch (Exception e) {
			logger.error("Failed to get AMEnvId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return id;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Map<String, Object> getAMEnv(Integer id) {
		logger.info("getAMEnv");

		Connection conn = daoFactory.beginTransaction();
		Map<String, Object> env = null;
		try {
			EnvDAO envDAO = daoFactory.getEnvDAO(conn);
			env = envDAO.readAMEnv(id);

		} catch (Exception e) {
			logger.error("Failed to get AMEnv: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return env;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public void setAMEnv(Integer id, Map<String, Integer> env) {
		logger.info("setAMEnv");

		Connection conn = daoFactory.beginTransaction();
		try {
			EnvDAO envDAO = daoFactory.getEnvDAO(conn);
			envDAO.updateAMEnv(id, env);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to set AMEnv: id " + id + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry setAMEnv");
				setAMEnv(id, env);
			}
		} catch (Exception e) {
			logger.error("Failed to set AMEnv: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer insertAMEnv(String name) {
		logger.info("insertAMEnv");

		int id = -1;
		Connection conn = daoFactory.beginTransaction();
		try {
			EnvDAO envDAO = daoFactory.getEnvDAO(conn);
			id = envDAO.createAMEnv(name);

		} catch (SQLException e) {
			int code = e.getErrorCode();
			logger.error("Failed to insert AMEnv: name " + name + ", {}, {}", code, e.toString());
			logger.error("SQLState : {}", e.getSQLState());
			daoFactory.rollback(conn);
			if (code == 1213) {
				logger.error("retry insertAMEnv");
				insertAMEnv(name);
			}
		} catch (Exception e) {
			logger.error("Failed to insert AMEnv: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}

		return id;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public List<ServiceInfra> getServiceInfraObjects() {
		logger.info("getServiceInfraAll");

		Connection conn = daoFactory.beginTransaction();
		List<ServiceInfra> sList = null;
		try {
			ServiceInfraDAO sDAO = daoFactory.getServiceInfraDAO(conn);
			sList = sDAO.readServiceInfraObjects();
			
		} catch (Exception e) {
			logger.error("Failed to get ServiceInfraObjects: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return sList;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean getServiceInfraAvail(String name) {
		logger.info("getServiceInfraAvail");

		Connection conn = daoFactory.beginTransaction();
		boolean avail = false;
		try {
			ServiceInfraDAO sDAO = daoFactory.getServiceInfraDAO(conn);
			avail = sDAO.readServiceInfraAvail(name);

		} catch (Exception e) {
			logger.error("Failed to get ServiceInfraAvail: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return avail;
	}
	
	@Override
	public String getServiceInfraName(int id) {
		logger.info("getServiceInfraName " + id);

		Connection conn = daoFactory.beginTransaction();
		String name = null ;
		try {
			ServiceInfraDAO sDAO = daoFactory.getServiceInfraDAO(conn);
			name = sDAO.readServiceInfra(id).getName();

		} catch (Exception e) {
			logger.error("Failed to get ServiceInfraName: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return name;
	}
	
	@Override
	public int getServiceInfraId(String name) {
		logger.info("getServiceInfraId " + name);

		Connection conn = daoFactory.beginTransaction();
		int id = -1 ;
		try {
			ServiceInfraDAO sDAO = daoFactory.getServiceInfraDAO(conn);
			id = sDAO.readServiceInfraId(name);

		} catch (Exception e) {
			logger.error("Failed to get ServiceInfraId: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return id;
	}

	@Override
	public String getServerEnvValue(String name) {
		logger.info("getServerEnvValue " + name);

		Connection conn = daoFactory.beginTransaction();
		String value = null;
		try {
			ServerEnvDAO sDAO = daoFactory.getServerEnvDAO(conn);
			value = sDAO.readValue(name);

		} catch (Exception e) {
			logger.error("Failed to get ServerEnvValue: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return value;
	}

	@Override
	public String getServerEnvContent(String name) {
		logger.info("getServerEnvContent " + name);

		Connection conn = daoFactory.beginTransaction();
		String content = null;
		try {
			ServerEnvDAO sDAO = daoFactory.getServerEnvDAO(conn);
			content = sDAO.readContent(name);

		} catch (Exception e) {
			logger.error("Failed to get ServerEnvContents: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return content;
	}

	@Override
	public String getNoticeContent(String div, String version) {
		logger.info("getNoticeContent " + div + ", " + version);

		Connection conn = daoFactory.beginTransaction();
		String content = null;
		try {
			NoticeDAO nDAO = daoFactory.getNoticeDAO(conn);
			content = nDAO.readContent(div, version);

		} catch (Exception e) {
			logger.error("Failed to get ServerEnvContents: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return content;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Integer getInteger_T(Integer num) {
		logger.info("getInteger : " + num);
		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			// if (num == 1) {
			// throw new Exception("test");
			// }
			Thread.sleep(1000);
			logger.info("num : " + num);
			// } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception e) {
			logger.error("*occur error : " + e.toString());
			e.printStackTrace();
		}

		daoFactory.endTransaction(conn);

		return num;
	}

	@Override
	public Integer getInteger_N(Integer num) {
		logger.info("getInteger : " + num);
		Connection conn = daoFactory.beginTransaction();
		try {
			JobDAO jobDAO = daoFactory.getJobDAO(conn);
			// if (num == 1) {
			// throw new Exception("test");
			// }
			Thread.sleep(1000);
			logger.info("num : " + num);
			// } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (Exception e) {
			logger.error("*occur error : " + e.toString());
			e.printStackTrace();
		}

		return num;
	}
	
	@Override
	public Map<String, Integer> getNumUserAgentCE(String status, int userId) {
		logger.info("getNumUserAgentCE " + status + ", " + userId );
		
		Connection conn = daoFactory.beginTransaction();
		Map<String,Integer> map = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			map = agentDAO.readUserAgentNumCE(status, userId);
			
		} catch (Exception e) {
			logger.error("Failed to get NumUserAgentCE: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return map;
	}
	
	@Override
	public Integer getNumUserAgentFromCE(String status, String userId, String ceName) {
		logger.info("getNumUserAgentCE " + status + ", " + userId + ", ceName" );
		
		Connection conn = daoFactory.beginTransaction();
		int num = 0;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			UserDAO userDAO = daoFactory.getUserDAO(conn);
			num = agentDAO.readUserAgentNumFromCE(status, userDAO.readUserId(userId), ceName);
			
		} catch (Exception e) {
			logger.error("Failed to get NumUserAgentFromCE: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return num;
	}
	
	@Override
	public Set<Integer> getUserIdFromAgent(String status) {
		logger.info("getUserIdFromAgent " + status );
		
		Connection conn = daoFactory.beginTransaction();
		Set<Integer> uidSet = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			uidSet = agentDAO.readAgentUserId(status);
			
		} catch (Exception e) {
			logger.error("Failed to get UserIdFromAgent: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return uidSet;
	}
	
	@Override
	public Map<String, Integer> getAgentNumMapFromMetaJob(int metaJobId){
		logger.info("getAgentNumMapFromMetaJob " + metaJobId );
		
		Connection conn = daoFactory.beginTransaction();
		Map<String, Integer> numMap = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			numMap = agentDAO.readAgentNumMapFromMetaJob(metaJobId);
			
		} catch (Exception e) {
			logger.error("Failed to get agentNumMapFromMetaJob: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return numMap;
	}
	
	@Override
	public Map<String, Integer> getAgentTaskMapFromMetaJob(int metaJobId){
		logger.info("getAgentTaskMapFromMetaJob " + metaJobId );
		
		Connection conn = daoFactory.beginTransaction();
		Map<String, Integer> taskMap = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			taskMap = agentDAO.readAgentTaskMapFromMetaJob(metaJobId);
			
		} catch (Exception e) {
			logger.error("Failed to get agentTaskMapFromMetaJob: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return taskMap;
	}
	
	@Override
	public Set<AgentInfo> getAgentInfoSetFromMetaJob(int metaJobId){
		logger.info("getUserInfoSetFromMetaJob " + metaJobId );
		
		Connection conn = daoFactory.beginTransaction();
		Set<AgentInfo> agentSet = null;
		try {
			AgentDAO agentDAO = daoFactory.getAgentDAO(conn);
			agentSet = agentDAO.readAgentInfoSetFromMetaJob(metaJobId);
			
		} catch (Exception e) {
			logger.error("Failed to get agentInfoSetFromMetaJob: {}", e.toString());
			daoFactory.rollback(conn);
		} finally {
			daoFactory.endTransaction(conn);
		}
		return agentSet;
	}

	public static void main(String[] args) {

		DatabaseImpl dbi = new DatabaseImpl();


		dbi.reEnqueueJob(633816);
//		System.out.println(dbi.checkSubmittedZombieAgent(3, 7));
//		System.out.println(dbi.getServiceInfraObjects());
//		dbi.reEnqueueJob(252595);
		
//		System.out.println(dbi.getAgentSubmittedZombieList(3));
//		System.out.println(dbi.getAMEnv(8));
//		ArrayList<String> celist = new ArrayList<String>();
//		celist.add("darthvader.kisti.re.kr 104 -1 105 -1");
//		dbi.updateCEInfo("pbs", celist);
//		System.out.println(dbi.getNumUserAgentCE("running", 18));
		
//		System.out.println(dbi.getAgentInfoSetFromMetaJob(1225));
		
//		System.out.println(dbi.getAgentNumMapFromMetaJob(1225));
		
//		System.out.println(dbi.getAgentTaskMapFromMetaJob(1225));
		
	}


}
