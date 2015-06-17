package org.kisti.htc.dbmanager.dao;

import java.sql.Connection;


/**
* Data Access Object Factory
*/
public final class DAOFactory {

	private static final DAOFactory daoFactory;

  // static variables
  static {
    try {
      daoFactory = new DAOFactory();
      DAOUtil.setupDataSource();
      DAOUtil.setupPLSIDataSource();
    } catch (Throwable ex) {
      System.err.println("Initial DAOFactory creation failed." + ex);
      throw new ExceptionInInitializerError(ex);
    }
  }

	
	public static void main(String args[]){
		
		DAOFactory df = new DAOFactory();
		Connection conn = df.beginTransaction();
		df.endTransaction(conn);
	}
	
	

	public Connection beginTransaction() {

		Connection conn = DAOUtil.getConnection();

		return conn;
	}
	
	public Connection beginPLSITransaction() {

		Connection conn = DAOUtil.getPLSIConnection();

		return conn;
	}

	public void endTransaction(Connection conn) {
		DAOUtil.doCommit(conn);
	}
	
	public void onlyCommit(Connection conn){
		DAOUtil.onlyCommit(conn);
	}
	
	public void rollback(Connection conn){
		DAOUtil.doRollback(conn);
	}

	public static DAOFactory getDAOFactory() {
		return daoFactory;
	}

	public ApplicationDAO getApplicationDAO(Connection conn) {
		return new ApplicationDAOImpl(conn);
	}

	public UserDAO getUserDAO(Connection conn) {
		return new UserDAOImpl(conn);
	}

	public MetaJobDAO getMetaJobDAO(Connection conn) {
		return new MetaJobDAOImpl(conn);
	}

	public JobDAO getJobDAO(Connection conn) {
		return new JobDAOImpl(conn);
	}

	public AgentDAO getAgentDAO(Connection conn) {
		return new AgentDAOImpl(conn);
	}

	public ResultDAO getResultDAO(Connection conn) {
		return new ResultDAOImpl(conn);
	}

	public CEDAO getCEDAO(Connection conn) {
		return new CEDAOImpl(conn);
	}
	
	public SubmitErrorDAO getSubmitErrorDAO(Connection conn) {
		return new SubmitErrorDAOImpl(conn);
	}
	
	public WMSCEDAO getWMSCEDAO(Connection conn) {
		return new WMSCEDAOImpl(conn);
	}
	
	public WMSDAO getWMSDAO(Connection conn) {
		return new WMSDAOImpl(conn);
	}
	
	public ServiceInfraDAO getServiceInfraDAO(Connection conn){
		return new ServiceInfraDAOImpl(conn);
	}
	
	public ServiceCodeDAO getServiceCodeDAO(Connection conn){
		return new ServiceCodeDAOImpl(conn);
	}
	
	public EnvDAO getEnvDAO(Connection conn){
		return new EnvDAOImpl(conn);
	}
	
	public ServerEnvDAO getServerEnvDAO(Connection conn){
	  return new ServerEnvDAOImpl(conn);
	}
	
	public NoticeDAO getNoticeDAO(Connection conn){
	  return new NoticeDAOImpl(conn);
	}
}
