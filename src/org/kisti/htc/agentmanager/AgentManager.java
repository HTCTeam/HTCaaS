package org.kisti.htc.agentmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.constant.AgentConstant;
import org.kisti.htc.constant.JobConstant;
import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.MetaJob;
import org.kisti.htc.dbmanager.beans.ServiceInfra;
import org.kisti.htc.dbmanager.beans.User;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.jobmanager.server.Constant;
import org.kisti.htc.message.MessageCommander;
import org.kisti.htc.message.MetaDTO;
import org.kisti.htc.message.MetaMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// class start
public class AgentManager {

	private static final Logger logger = LoggerFactory.getLogger(AgentManager.class);
	// final static mLogger logger = mLoggerFactory.getLogger("AM");

	protected static final String AGENT_STATUS_SUB = AgentConstant.AGENT_STATUS_SUBMIT;
	protected static final String AGENT_STATUS_RUN = AgentConstant.AGENT_STATUS_RUN;
	protected static final String AGENT_STATUS_CANCEL = AgentConstant.AGENT_STATUS_CANCEL;

	protected static final String JOB_STATUS_PRE = JobConstant.JOB_STATUS_PRE;
	protected static final String JOB_STATUS_CANCEL = JobConstant.JOB_STATUS_CANCEL;

	static final String CODE_LOCAL = "Local";
	static final String CODE_GRID = "Grid";
	static final String CODE_SUPERCOMPUTER = "Cloud";
	static final String CODE_Cluster = "Cluster";

	protected static final String LOCAL = "local";
	protected static final String VOFA = "vo.france-asia.org";
	protected static final String BIOMED = "biomed";
	protected static final String PLSI = "PLSI";
	protected static final String _4TH = "4TH";
	protected static final String AMAZONEC2 = "Amazon EC2";
	protected static final String PBS = "pbs";
	protected static final String CONDOR = "condor";
	protected static final String SGE = "sge";
	protected static final String OPENSTACK = "Openstack";  //openstack

	// file-based communication for PLSI
	public static boolean fileEnabled = false;

	public static boolean pTestAgentEnabled = false;
	private static final long THREADWAITTIME = 20000;

	// PLSI agent wall clock time property
	static int aWallClockTime = 1200;
	static int dWallClockTime = 24;

	static String agentStorageAddress = "/htc_storage/"; // default

	static String PLSI_Remote_Home = "/htcaas/";
	static String Condor_Remote_Home = "/work/htcaas/";
	static String Default_Remote_Home = "/home/";
	static String Shared_Remote_Home = "/pwork01/";

	static final String configPath = "conf/HTCaaS_Server.conf";;

	// Worker Scaling Metrics
	public static final int everyNjobs = 1;
	public static final int avgEnqueueTime = 2;
	public static final int maxEnqueueTime = 3;
	public static final int addNagents = 4;
	public static final int keepQagents = 5;
	public static final int keepStaticAgents = 6;
	public static final int samplingNagents = 7;
	public static final int dynamicFairness = 8;
	public static final int firstFreeCPUSCluster = 9;
	public static final int gridScout = 10;
	public static final int testAllCombination = 11;

	public static int AGENT_SCALING_METRIC = 8;

	private static int SAMPLING_ADD_AGENT_NO = 3; // Sampling Agent size
	private static int ADD_AGENT_NO = 3552; // addNagents size
	private static int NUM_CREDIT_AGENT = 0;
	private static boolean checkUserThread = false;

	private int numSubmitWorkQueue = 6;
	private int numCheckWorkQueue = 3;

	private int MAX_ENQUEUE_COUNT_PER_WORKER = 1;
//	private int THRESHOLD_AVG_ENQUEUE_TIME = 0;
//	private int THRESHOLD_MAX_ENQUEUE_TIME = 0;

	// CE Selection Metrics
	public static final int freeCPU = 1;
	public static final int roundrobin = 2;
	public static final int intelligent = 3;
	public static final int priority = 4;

	public static int CE_SELECTION_METRIC = 1;

	private static String SERVICE_INFRA_SET = "\"7\"";

//	private static int numTotalSubmittedAgents = 0;

	private QueueViewMBean iqueue = null;
	private static String metaJobQueue;
	private long initialDequeueCount = 0;
	private int numLaunchedAgents = 0;
	private int lastLaunchedEnqueueCount = 0;
	private int runningAgentHeartbeatPeriod = 0;
	private int submittedAgentHeartbeatPeriod = 0;
	private int newAgentHeartbeatPeriod = 0;
	private int statusMonitoringHeartbeatPeriod = 0;
	private int resourceAvailablePeriod = 0;
	private int zombieAgentMonitoringPeriod = 1000;

	private int ceSize;
	private Map<String, MetaDTO> metaMap = new HashMap<String, MetaDTO>();;

	private static SubmitWorkQueue submitQueue;

	private String DBManagerURL;
	private String JMXServiceURL;
	private String JMXObjectName;

	public static Database dbClient; // DBManager client

	public static File scriptDir;
	public static File tempDir;
	public ResourceScheduler rscScheduler;
//	private MetaDirectConsumer metaConsumer = null;
	private MetaDTO mDTO = new MetaDTO();
	BrokerViewMBean mbean;
	MBeanServerConnection connection;

	public static CheckWorkQueue checkQueue;

	public File workDir;

	private boolean testCompleted; // For testAllCombination
//	private int amId;
//	private Map<Integer, Integer> serviceInfra;
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;

	Map<String, Object> env = null;

	boolean serviceInfraChangeFlag = false;

	private BackendResource resource;
	private List<BackendResource> brList = new ArrayList<BackendResource>();

	// prepare backend resource variables
	LLResource plsi;
	ClusterResource cluster;
	GliteResource biomed;
	GliteResource vofa;
	CondorResource condor;
	SGEResource sge;
	OpenstackResource openstack; //openstack
	
	private static AgentManager agentmgr = new AgentManager();
	
	public static SubmitWorkQueue getSubmitQueue(){
		return submitQueue;
	}

	public static AgentManager getInstance() {
		return agentmgr;
	}

	// constructor
	// AgentManager::AgentManager
	private AgentManager() {

		logger.info("====================================");
		logger.info("Agent Manager start");
		logger.info("====================================");

		// 1. read config file
		try {
			MetaMessageSender metaMessageSender = new MetaMessageSender();
			Properties prop = new Properties();

			logger.info("loading properties file " + configPath);
			prop.load(new FileInputStream(configPath));

			DBManagerURL = prop.getProperty("DBManager.Address");
			logger.info("DBManagerURL: " + DBManagerURL);

			if (prop.getProperty("SSL.Authentication").equals("true")) {
				SSL = true;
				DBManagerURL = DBManagerURL.replace("http", "https");
				logger.info("DBManagerURL: " + DBManagerURL);
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}

			JMXServiceURL = prop.getProperty("ActiveMQ.Broker.JMXServiceURL");
			logger.info("JMXServiceURL: " + JMXServiceURL);

			JMXObjectName = prop.getProperty("ActiveMQ.Broker.ObjectName");
			logger.info("JMXObjectName: " + JMXObjectName);

			metaJobQueue = prop.getProperty("ActiveMQ.MetaJobQueue");
			metaMessageSender.changeDestiQueue(metaJobQueue);
			logger.info("MetaJobQueueName: " + metaJobQueue);

			String value1 = prop.getProperty("RunningAgent.Heartbeat.Period");
			if (value1 != null) {
				runningAgentHeartbeatPeriod = Integer.parseInt(value1);
			} else {
				logger.error("RunningAgent.Heartbeat.Period is null. Using default value: 5");
				runningAgentHeartbeatPeriod = 5;
			}
			logger.info("RunningAgentHeartbeatPeriod: " + runningAgentHeartbeatPeriod);

			String value2 = prop.getProperty("SubmittedAgent.Heartbeat.Period");
			if (value2 != null) {
				submittedAgentHeartbeatPeriod = Integer.parseInt(value2);
			} else {
				logger.error("SubmittedAgent.Heartbeat.Period is null. Using default value: 1");
				submittedAgentHeartbeatPeriod = 1;
			}
			logger.info("SubmittedAgentHeartbeatPeriod: " + submittedAgentHeartbeatPeriod);

			String value3 = prop.getProperty("NewAgent.Heartbeat.Period");
			if (value3 != null) {
				newAgentHeartbeatPeriod = Integer.parseInt(value3);
			} else {
				logger.error("NewAgent.Heartbeat.Period is null. Using default value: 1");
				newAgentHeartbeatPeriod = 1;
			}
			logger.info("NewAgentHeartbeatPeriod: " + newAgentHeartbeatPeriod);

			// statusMonitoringHeartbeatPeriod
			String value4 = prop.getProperty("StatusMonitoring.Heartbeat.Period");
			if (value4 != null) {
				statusMonitoringHeartbeatPeriod = Integer.parseInt(value4);
			} else {
				logger.error("StatusMonitoring.Heartbeat.Period is null. Using default value: 1");
				statusMonitoringHeartbeatPeriod = 1;
			}
			logger.info("StatusMonitoringHeartbeatPeriod: " + statusMonitoringHeartbeatPeriod);

			// ResourceAvailablePeriod
			String value5 = prop.getProperty("Resource.Available.Period");
			if (value5 != null) {
				resourceAvailablePeriod = Integer.parseInt(value5);
			} else {
				logger.error("Resource.Available.Period is null. Using default value: 120");
				resourceAvailablePeriod = 120;
			}
			logger.info("ResourceAvailablePeriod: " + resourceAvailablePeriod);

			// ZombieAgentMonitoringPeriod
			String value6 = prop.getProperty("ZombieAgent.Monitoring.Period");
			if (value6 != null) {
				zombieAgentMonitoringPeriod = Integer.parseInt(value6);
			} else {
				logger.error("ZombieAgent.Monitoring.Period is null. Using default value: 30sec");
				zombieAgentMonitoringPeriod = 30;
			}
			logger.info("ZombieAgent.Monitoring.Period: " + zombieAgentMonitoringPeriod);
			
			AGENT_SCALING_METRIC = Integer.parseInt(prop.getProperty("AGENT_SCALING_METRIC"));
			logger.info("AGENT_SCALING_METRIC: " + AGENT_SCALING_METRIC);
			
			CE_SELECTION_METRIC = Integer.parseInt(prop.getProperty("CE_SELECTION_METRIC"));
			logger.info("CE_SELECTION_METRIC: " + CE_SELECTION_METRIC);
			
			SERVICE_INFRA_SET = prop.getProperty("SERVICE_INFRA_SET");
			logger.info("SERVICE_INFRA_SET: " + SERVICE_INFRA_SET);

			fileEnabled = Boolean.parseBoolean(prop.getProperty("File_Enabled"));
			logger.info("File_Enabled: " + fileEnabled);

			pTestAgentEnabled = Boolean.parseBoolean(prop.getProperty("PTestAgent_Enabled"));
			logger.info("PTestAgent_Enabled: " + pTestAgentEnabled);

			dWallClockTime = Integer.parseInt(prop.getProperty("Default_WallClockTime"));
			logger.info("Default_WallClockTime : " + dWallClockTime);

			aWallClockTime = Integer.parseInt(prop.getProperty("Additional_WallClockTime"));
			logger.info("Additional_WallClockTime : " + aWallClockTime);

			agentStorageAddress = prop.getProperty("Agent.Storage.Address");
			logger.info("Agent Storage Adress : " + agentStorageAddress);

			PLSI_Remote_Home = prop.getProperty("PLSI_Remote_Home");
			logger.info("PLSI Remote Home : " + PLSI_Remote_Home);

			Default_Remote_Home = prop.getProperty("Default_Remote_Home");
			logger.info("Default Remote Home : " + Default_Remote_Home);

			numSubmitWorkQueue = Integer.parseInt(prop.getProperty("Num_SubmitThread"));
			logger.info("The number of submitWork threads : " + numSubmitWorkQueue);

			createDirectories();

		} catch (Exception e) {
			error_and_exit("Failed to load config file and property: " + e.getMessage());
		}

		// 2. prepare DBManager client
		logger.info("prepare dbmanager client");
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Database.class);
		factory.setAddress(DBManagerURL);
		factory.setDataBinding(new AegisDatabinding());
		dbClient = (Database) factory.create();

		// plan to insert into new Constant class
		GliteResource.dbFlag = true;
		CondorResource.dbFlag = true;
		//

		logger.info("prepare httpConduit");
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(dbClient).getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(300000);
		httpClientPolicy.setReceiveTimeout(0);
		httpConduit.setClient(httpClientPolicy);

		if (SSL) {
			try {
				setupTLS(dbClient);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}
		}

		// logger.info("Grid_Enabled: " + gridEnabled);

		// // UDManager client (CXF)
		// udClient = new UDClient();

		// ActivqMQ Broker Statistics
		try {
			JMXServiceURL url = new JMXServiceURL(JMXServiceURL);
			JMXConnector connector = JMXConnectorFactory.connect(url);
			connector.connect();
			connection = connector.getMBeanServerConnection(); // MBeanServerConnection
			// connection;
			ObjectName name = new ObjectName(JMXObjectName); // import
			// javax.management.ObjectName;
			mbean = MBeanServerInvocationHandler.newProxyInstance(connection, name, BrokerViewMBean.class, true);

			logger.info("ActiveMQ broker id=" + mbean.getBrokerId());
			logger.info("ActiveMQ broker name=" + mbean.getBrokerName());
			for (ObjectName queueName : mbean.getQueues()) {
				QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
				if (queueMbean.getName().equals(metaJobQueue)) {
					iqueue = queueMbean;
					break;
				}
			}
			if (iqueue == null) {
				logger.error("ActiveMQ Queue does not exist");
				throw new Exception("Queue not exist: " + metaJobQueue);
			}

		} catch (Exception e) {
			error_and_exit("Cannot access ActiveMQ Broker" + e.getMessage());
		}

		scriptDir = new File("script");
		logger.info("script path = " + scriptDir.getAbsolutePath());
		if (!scriptDir.exists() || !scriptDir.isDirectory()) {
			error_and_exit("script path does not exist");
		}

		tempDir = new File("tmp");
		logger.info("temp path = " + tempDir.getAbsolutePath());
		if (!tempDir.exists() || !tempDir.isDirectory()) {
			tempDir.mkdirs();
		}

		rscScheduler = new ResourceScheduler();

		// 파일 기반 communication(?) for PLSI
		if (fileEnabled) { // false
			checkQueue = new CheckWorkQueue(this, "CheckWorkQueue", numCheckWorkQueue);
		}

		/*String host;
		try {
			host = InetAddress.getLocalHost().getHostAddress();
			logger.info("AgentManager hostname : " + host);
			if (dbClient.getAMEnvId(host) == -1) {
				amId = dbClient.insertAMEnv(host);
			} else {
				amId = dbClient.getAMEnvId(host);
			}
		} catch (UnknownHostException e) {
			host = "UnknownHost";
		}*/
	}

	// / SSL {{{
	private static void setupTLS(Database port) throws FileNotFoundException, IOException, GeneralSecurityException {

		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

		TLSClientParameters tlsCP = new TLSClientParameters();
		KeyStore keyStore = KeyStore.getInstance("JKS");
		String keyStoreLoc = SSLClientPath;
		keyStore.load(new FileInputStream(keyStoreLoc), SSLClientPassword.toCharArray());
		KeyManager[] myKeyManagers = getKeyManagers(keyStore, SSLClientPassword);
		tlsCP.setKeyManagers(myKeyManagers);

		KeyStore trustStore = KeyStore.getInstance("JKS");
		String trustStoreLoc = SSLCAPath;
		trustStore.load(new FileInputStream(trustStoreLoc), SSLCAPassword.toCharArray());
		TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
		tlsCP.setTrustManagers(myTrustStoreKeyManagers);

		// The following is not recommended and would not be done in a
		// prodcution environment,
		// this is just for illustrative purpose
		tlsCP.setDisableCNCheck(true);
		tlsCP.setSecureSocketProtocol("SSL"); // addme

		httpConduit.setTlsClientParameters(tlsCP);

	}

	private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
		fac.init(trustStore);
		return fac.getTrustManagers();
	}

	private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
		KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
		fac.init(keyStore, keyPass);
		return fac.getKeyManagers();
	}

	// / SSL }}}

	// AgentManager::createDirectories
	private void createDirectories() {
		workDir = new File("workspace");
		if (!workDir.exists()) {
			// DeleteFileAndDirUtil.deleteFilesAndDirs(workDir.getAbsolutePath());
			workDir.mkdirs();
		}
	}

	public void setMaxEnqueueCountPerWorker(int mec) {
		MAX_ENQUEUE_COUNT_PER_WORKER = mec;
	}

	// public void setAGENT_SCALING_METRIC(int asm) {
	// AGENT_SCALING_METRIC = asm;
	// }
	//
	// public int getAGENT_SCALING_METRIC() {
	// return AGENT_SCALING_METRIC;
	// }

	public int getnumLaunchedAgents() {
		return numLaunchedAgents;
	}

	public static int selectCount = 0;
	// / sub class {{{
	private class UserAgentKeeper extends Thread {

		private QueueViewMBean userQueue = null;
		private String userQName;
		private int userAgentNO;
		private int runningAgentNO;
		private int keepAgentNO;
		private int aliveAgentOld = 0;
		private long qSize = 0L;

		int waitingTime = 1;

		// UserAgentKeeper::getValidServiceInfra
		public int getValidServiceInfra(String user) {
			List<ServiceInfra> si = dbClient.getUserServiceInfra(userQName);
			int sId = -1;
			int resultId = -1;
			int priority = -1;
			for (ServiceInfra serviceInfra : si) {
				// In case that service infra range is 0~9, this is valid.
				if (serviceInfra.isAvailable() && validSISet.contains(serviceInfra.getId())) {
					sId = serviceInfra.getId();
					int sPri = serviceInfra.getPriority();
					if (sPri > priority) {
						if (dbClient.getCEFreeCPUTotal(sId) > 0) {
							priority = serviceInfra.getPriority();
							// result = serviceInfra.getName();
							resultId = sId;
						}
					}
				}
			}

			return resultId;
		}
		
		public boolean checkMetaJobCEFromValidSISet(String user){
			
			boolean ret = false;
			
			try{
				MetaDTO mDTO = metaMap.get(user);
				
				String mCE = mDTO.getCe();
				
				String[] ces = mCE.split(",");
				Set<Integer> siSetFromMetaJob = new HashSet<Integer>();
				
				for(String ce : ces){
					siSetFromMetaJob.add(dbClient.getCEServiceInfraId(Integer.parseInt(ce)));
				}
				
				for(Integer si : siSetFromMetaJob){
					
					if(validSISet.contains(si)){
							continue;
					}else{
						logger.error("Not matched serviceinfra from metajob and validSISet :" + user + ", " + si + "metajob is being canceled" );
						dbClient.setMetaJobStatus(mDTO.getMetaJobId(), Constant.METAJOB_STATUS_CANCEL);
						dbClient.setMetaJobError(mDTO.getMetaJobId(), "Not matched serviceinfra from validSISet");
						MessageCommander mc = new MessageCommander();
						int num = mc.removeMessage(mDTO.getMetaJobId(), userQName);
						logger.info(num + "jobs are removed");
						return ret;
					}
				}
				
				ret = true;
			}catch(Exception e){
				logger.error(e.toString());
				e.printStackTrace();
			}
			
			
			return ret;
			
		}

		// UserAgentKeeper::setKeepAgentNO
		// keepAgentNO 를 설정
		public boolean setKeepAgentNO() {

			runningAgentNO = dbClient.getNumUserAgentStatus(userQName, AGENT_STATUS_RUN);
			userAgentNO = dbClient.getNumUserAgentValid(userQName);

			long queueSize = userQueue.getQueueSize();
			logger.info("[3.UT]*****checking User Info : " + userQName + ", # of SubJob : " + queueSize + "*****");

			/*
			 * 참고 // Worker Scaling Metrics public static final int everyNjobs =
			 * 1; public static final int avgEnqueueTime = 2; public static
			 * final int maxEnqueueTime = 3; public static final int addNagents
			 * = 4; public static final int keepQagents = 5; public static final
			 * int keepStaticAgents = 6; public static final int samplingNagents
			 * = 7; public static final int dynamicFairness = 8; public static
			 * final int firstFreeCPUSCluster = 9; public static final int
			 * testAllCombination = 10;
			 */
			
			if(queueSize == 0){
				if(submitQueue.size() > 0){
					submitQueue.removeJobAll();
				}
			}

			if (queueSize > 0) {
				switch (AGENT_SCALING_METRIC) {

				// keepAgentNO 를 설정

				case dynamicFairness:
					if (NUM_CREDIT_AGENT > (queueSize + runningAgentNO)) {
						keepAgentNO = (int) queueSize + runningAgentNO;
						// } else if(userAgentNO >= NUM_CREDIT_AGENT){
						// keepAgentNO = NUM_CREDIT_AGENT;
					} else {
						keepAgentNO = NUM_CREDIT_AGENT;
					}

					break;

				case addNagents:
					keepAgentNO = ADD_AGENT_NO; // ADD_AGENT_NO = 1000
					break;

				case keepQagents:
					if (NUM_CREDIT_AGENT > (queueSize + runningAgentNO)) {
						keepAgentNO = (int) queueSize + runningAgentNO;
					} else {
						keepAgentNO = NUM_CREDIT_AGENT;
					}
					break;

				case keepStaticAgents:
					if (ADD_AGENT_NO > (queueSize + runningAgentNO)) {
						keepAgentNO = (int) queueSize + runningAgentNO;
					} else {
						keepAgentNO = ADD_AGENT_NO;
					}
					break;

				case gridScout:
//					keepAgentNO = ADD_AGENT_NO; // ADD_AGENT_NO = 1000
					keepAgentNO = NUM_CREDIT_AGENT;
					
					break;
				default:
					keepAgentNO = 0;
				}

			} else if (queueSize <= 0 && runningAgentNO == 0) {
				try {
					mbean.removeQueue(userQName);
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			} else {
				keepAgentNO = runningAgentNO;
			}
			return true;
		}

		// UserAgentKeeper::increaseCEPriority
		public void increaseCEPriority(String ceName) {
			logger.info("[3.UT] CE priority increase :" + ceName);
			if (dbClient.getCEPriority(ceName) >= 0) {
				dbClient.setCEPriorityAdd(ceName, 1);
			} else {
				dbClient.setCEPriority(ceName, 0);
			}

			dbClient.setCEZeroCnt(ceName, 0);
		}

		// UserAgentKeeper::decreaseCEPriority
		public void decreaseCEPriority(String ceName) {
			logger.info("[3.UT] CE priority decrease :" + ceName);

			// if (dbClient.getCEPriority(ceName) > 0) {
			// dbClient.setCEPriority(ceName, 0);
			// } else {
			// dbClient.setCEPriorityAdd(ceName, -1);
			// }

			dbClient.setCEPriority(ceName, 0);

			// available 을 false 로 바꿈
			logger.info("[3.UT] Set CE Available to false " + ceName);
			dbClient.setCEAvailable(ceName, false);
			dbClient.setCEAvailableUpdateTime(ceName);

			dbClient.setCEZeroCnt(ceName, 0);
		}

		// UserAgentKeeper::submitUserAgent
		public void submitUserAgent(int validSId) {

			String cePair[] = null;
			String siName = dbClient.getServiceInfraName(validSId);

			while (userAgentNO < keepAgentNO) {
				qSize = userQueue.getQueueSize();
				if (qSize > 0) {
					cePair = selectCE(validSId, userQName);
					if (cePair != null && !cePair[0].isEmpty()) {

						if (!(siName.equals(BIOMED) || siName.equals(VOFA) || siName.equals(OPENSTACK))) {
							aliveAgentOld = dbClient.getAgentLastId();
							
							int fCPU = dbClient.getCEFreeCPU(cePair[0]);
							
							int submitAgent = keepAgentNO - userAgentNO;
							
							if (AGENT_SCALING_METRIC != addNagents) {
								submitAgent = fCPU > submitAgent ? submitAgent : fCPU;
							}
							
							logger.warn("[3.UT] !!!!!");
							
							launchWorker(userQName, cePair, submitAgent);
							
							logger.info("[3.UT] |numSubmittedAgents: " + submitAgent);
							waitTimeForSubmittedAgent(submitAgent, siName);

							int aliveAgent = dbClient.getAgentRunningNum(aliveAgentOld);

							logger.warn("[3.UT] AliveAgent : " + aliveAgent + ", SubmittedAgent:" + submitAgent);

							int aliveRate = aliveAgent * 100 / submitAgent;
							if (aliveRate >= 100) {
								logger.info("[3.UT] aliveAgent-submitAgent aliveRate : " + aliveRate);
								increaseCEPriority(cePair[0]);
							} else if (aliveRate < 100 && aliveAgent > 0) {
								logger.info("[3.UT] aliveAgent-submitAgent aliveRate : " + aliveRate);
								logger.info("[3.UT] add CEZeroCountAdd 1");
								dbClient.setCEZeroCntAdd(cePair[0], 2);

								if (dbClient.getCEZeroCnt(cePair[0]) >= 2 && qSize > 0) {
									
									decreaseCEPriority(cePair[0]);
								}
							} else if (aliveAgent == 0) {
								logger.info("[3.UT] add CEZeroCountAdd 1");
								dbClient.setCEZeroCntAdd(cePair[0], 2);

								if (dbClient.getCEZeroCnt(cePair[0]) >= 2 && qSize > 0) {
									
									decreaseCEPriority(cePair[0]);
								}
							} else {
								logger.info("[3.UT] No Action");
							}

							dbClient.setCELimitCPU(cePair[0], fCPU - aliveAgent);
							logger.warn("[3.UT] !!!!!");
							
							userAgentNO = dbClient.getNumUserAgentValid(userQName);
						} else if(siName.equals(BIOMED) || siName.equals(VOFA)) {
							boolean tmp = false;
							////
							logger.warn("[3.UT] !!!!!");
							int submitAgent = 0;
							CE ce = dbClient.getCEObject(cePair[0]);
							
							selectCount = ce.getSelectCount();
							
							if(selectCount  == 1 ){
								tmp = checkSubmitQueueAndLaunchWorker(cePair, 1);
							}else{
								int submitCount = ce.getSubmitCount();
								int num = dbClient.getNumUserAgentFromCE(Constant.AGENT_STATUS_RUN, userQName, cePair[0]);
								long pct =0l;
								if(submitCount > 0){
									pct = 100*(num/submitCount);
								}
								logger.info("[3.UT] Total aliveAgent-submitAgent aliveRate : " + pct);
								if(pct > 50){
									submitAgent = 5;
									increaseCEPriority(cePair[0]);
									tmp = checkSubmitQueueAndLaunchWorker(cePair, submitAgent);

									logger.info("[3.UT] |numSubmittedAgents: " + submitAgent);
									
									logger.warn("[3.UT] TotalAliveAgent : " + num + ", TotalSubmittedAgent:" + submitCount);
								}else{
									logger.info("[3.UT] Set CE Available to false " + cePair[0]);
									dbClient.setCEAvailable(cePair[0], false);
									dbClient.setCEAvailableUpdateTime(cePair[0]);
								}
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							userAgentNO = userAgentNO + submitAgent;
							
							if(tmp) break;
						} else if (siName.equals(OPENSTACK)){
							aliveAgentOld = dbClient.getAgentLastId();
							
							int fCPU = dbClient.getCEFreeCPU(cePair[0]);
							
							int submitAgent = keepAgentNO - userAgentNO;
							
							if (AGENT_SCALING_METRIC != addNagents) {
								submitAgent = fCPU > submitAgent ? submitAgent : fCPU;
							}
							
							logger.warn("[3.UT] !!!!!");
							
							launchWorker(userQName, cePair, submitAgent);
							
							logger.info("[3.UT] |numSubmittedAgents: " + submitAgent);
							waitTimeForSubmittedAgent(submitAgent, siName);

							int aliveAgent = dbClient.getAgentRunningNum(aliveAgentOld);

							logger.warn("[3.UT] AliveAgent : " + aliveAgent + ", SubmittedAgent:" + submitAgent);

							decreaseCEPriority(cePair[0]);

							logger.warn("[3.UT] !!!!!");
							
							userAgentNO = dbClient.getNumUserAgentValid(userQName);
						}
					} else {
						logger.warn("[3.UT] There are no available CEs. try:");
						try {
							Thread.sleep(3500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						break;
					}
				}else{
					break;
				}
			}
		}
		
		public boolean checkSubmitQueueAndLaunchWorker(String[] cePair, int submitAgent){
			boolean ret = false;
			while(true){
				if(submitQueue.size() < 20){
					launchWorker(userQName, cePair, submitAgent);
					break;
				}else{
						logger.info("SubmitWorkerQueue is more than 20. break");
						ret  = true;
				}
			}
			
			return ret;
		}

		public void waitTimeForSubmittedAgent(int submitAgent, String siName) {

			if (siName.equals(PLSI)) {
				try {
					int time = 0;
					if (submitAgent < 50) {
						time = 30;
					} else if (submitAgent > 400) {
						time = 120;
					} else {
						time = 30 + (int) Math.ceil((submitAgent / 50.0)) * 10;
					}
					logger.info("[3.UT] Waiting to launch PLSI workers... Time(s) : " + time);
					Thread.sleep(time * 1000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (siName.equals(PBS)) {
				try {
					int time = 0;
					if (submitAgent < 50) {
						time = 30;
					} else if (submitAgent > 400) {
						time = 120;
					} else {
						time = 30 + (int) Math.ceil((submitAgent / 50.0)) * 10;
					}
					logger.info("[3.UT] Waiting to launch PBS workers... Time(s) : " + time);
					Thread.sleep(time * 1000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (siName.equals(SGE)) {
				try {
					int time = 0;
					if (submitAgent < 50) {
						time = 50;
					} else if (submitAgent > 400) {
						time = 130;
					} else {
						time = 50 + (int) Math.ceil((submitAgent / 50.0)) * 10;
					}
					logger.info("[3.UT] Waiting to launch SGE workers... Time(s) : " + time);
					Thread.sleep(time * 1000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} else if (siName.equals(OPENSTACK)) {
				try {
					int time = 0;
					if (submitAgent < 50) {
						time = 50;
					} else if (submitAgent > 400) {
						time = 130;
					} else {
						time = 50 + (int) Math.ceil((submitAgent / 50.0)) * 10;
					}
					logger.info("[3.UT] Waiting to launch OPENSTACK workers... Time(s) : " + time);
					Thread.sleep(time * 1000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else if (siName.equals(BIOMED)) {
				// try {
				int time = 0;
				// if (submitAgent < 50) {
				// time = 100;
				// } else if (submitAgent > 400) {
				// time = 140;
				// } else {
				// time = 100 + (int) Math.ceil((submitAgent / 50.0)) * 10;
				// }
				logger.info("[3.UT] Waiting to launch Biomed workers... Time(s) : " + time);
				// Thread.sleep(time * 1000);
				//
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
			} else if (siName.equals(VOFA)) {
				// try {
				int time = 0;

				// if (submitAgent < 50) {
				// time = 100;
				// } else if (submitAgent > 400) {
				// time = 140;
				// } else {
				// time = 100 + (int) Math.ceil((submitAgent / 50.0)) * 10;
				// }
				logger.info("[3.UT] Waiting to launch VOFA workers... Time(s) : " + time);
				// Thread.sleep(time * 1000);

				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
			} else if (siName.equals(LOCAL)) {
				int time = 10;

				logger.info("[3.UT] Waiting to launch " + LOCAL + " workers... Time(s) : " + time);
				try {
					Thread.sleep(time * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				try {
					int time = 0;
					if (submitAgent < 50) {
						time = 30;
					} else if (submitAgent > 400) {
						time = 120;
					} else {
						time = 30 + (int) Math.ceil((submitAgent / 50.0)) * 10;
					}
					logger.info("[3.UT] Waiting to launch default workers... Time(s) : " + time);
					Thread.sleep(time * 1000);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		// UserAgentKeeper::run
		public void run() {

			// dbClient.setCEAliveAgentInit();
			dbClient.setCEZeroCntAll(0);
			dbClient.setCESelectCntAll(0);
			initializeCEAvailable();
			StatusManager sm = new StatusManager();
			boolean tag= false;

			while (true) {

				try {
					logger.info("[3.UT]==========3.UserThread : Monitoring User Queues and Contoling User Agents==========");

					int length = mbean.getQueues().length;
					logger.debug("[3.UT] queue length : " + length);

					if (length > 1) {
						sm.run();
						tag = true;

						for (ObjectName queueName : mbean.getQueues()) {
							QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
							if (queueMbean.getName().equals(metaJobQueue)) {
								continue;
							} else {
								userQueue = queueMbean;
							}

							userQName = userQueue.getName();

							boolean cont = false;
							for(Integer i : validSISet){
	                			String sId = dbClient.getUserInfo(userQName).getServiceInfraID();
	                			if(sId.contains(i.toString())){
	                				cont = true;
	                				break;
	                			}
	                		}
							
							logger.info("[3.UT] userQName : " + userQName);
							
							
							if(cont){
								prepareMetaDTO(userQName);
								int validSId = getValidServiceInfra(userQName);
								if (!setKeepAgentNO()) {
									dbClient.setUserKeepAgentNO(userQName, 0);
									continue;
								}
								dbClient.setUserKeepAgentNO(userQName, (int) keepAgentNO);
								
								userAgentNO = dbClient.getNumUserAgentValid(userQName);
								logger.info("[3.UT] userAgentNo:" + userAgentNO + " keepAgentNO:" + keepAgentNO);
								
								if (validSId > -1) {
									submitUserAgent(validSId);
								} else {
									logger.warn("[3.UT] There is no valid compute resource. queue : " + userQName);
									logger.warn("[3.UT] Keep finding valid compute Resource!");
								}
							} else {
								userAgentNO = dbClient.getNumUserAgentValid(userQName);
								logger.info("[3.UT] UserAgentNo:" + userAgentNO);
							}
						}
						
						try {
							if (length == 2) {
								Thread.sleep(15000);
							} else {
								Thread.sleep(8000);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						logger.info("[3.UT] repeating Queue.....");
						waitingTime = 1;

					} else {
						logger.info("[3.UT] No User JobQueue. Waiting ..." + waitingTime + "s");
						if (numLaunchedAgents == 0 && tag ) {
							dbClient.setCEAliveAgentInit();
							dbClient.setCEZeroCntAll(0);
							dbClient.setCESelectCntAll(0);
							if(biomed != null){
								dbClient.initCESubmitCount(biomed.getName());
							}
							if(vofa != null){
								dbClient.initCESubmitCount(vofa.getName());
							}
							initializeCEAvailable();
							tag = false;
						}
						
						// when there are not user jobs, all the CEes are
						// initialized

						try {
							Thread.sleep(1000 * waitingTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (waitingTime < 32) {
							waitingTime = waitingTime * 2;
						} else {
							// this thread is terminated and set UserThread ==
							// false.
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					logger.error(e.toString());
					e.printStackTrace();
					logger.error("[3.UT] UserAgentKepper Error : Sleeping " + THREADWAITTIME / 1000 + "s to repeat UserAgentKeeper...");
					try {
						Thread.sleep(THREADWAITTIME);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}

			}

		}

		public void initializeCEAvailable() {

			for (BackendResource br : brList) {

				if (br.isEnabledForDB() && br.isEnabledForAM()) {
					List<String> ce = dbClient.getCENameList(br.getName(), false, false);

					if (ce != null && ce.size() > 0) {
						for (String name : ce) {
							dbClient.setCEAvailable(name, true);
						}
					}
				}
			}
		} // end initializeCEAvailable()
		
		
		public void prepareMetaDTO(String userId){
			MetaJob mJob = dbClient.getMetaJobLastRunningFromUser(userId);
			if(mJob != null){
				mDTO.setApp(mJob.getApp());
				mDTO.setCe(mJob.getCe());
				mDTO.setMetaJobId(mJob.getId());
				mDTO.setNumSubJob(mJob.getNum());
				mDTO.setUserId(mJob.getUser());
				
				metaMap.put(mDTO.getUserId(), mDTO);
			}
		}
		// AgentManager::checkInputQueue
		/*public void checkInputQueue(long mSize) {
			logger.info("Checking MetaJob queue");
			logger.info("| MetaQueueSize: " + mSize);

			//
			while (mSize > 0) {
				try {
					mDTO = metaConsumer.getMessage();
					metaMap.put(mDTO.getUserId(), mDTO);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mSize = iqueue.getQueueSize();
			}

			// if (needNewWorker()) {
			// launchWorker();
			// return true;
			// }
			// return false;
		}
*/
	} // end UserAgentKeeper class

	// / sub class }}}


	// AgentManager::needNewWorker
	// 새로운 워커를 필요로 하는지 체크
	/*private boolean needNewWorker() {
		long qSize = iqueue.getQueueSize();
		logger.info("| MetaQueueSize: " + qSize);

		//
		if (qSize > 0 && AGENT_SCALING_METRIC != samplingNagents) {
			try {
				mDTO = metaConsumer.getMessage();
				metaMap.put(mDTO.getUserId(), mDTO);
			} catch (Exception e) {
				e.printStackTrace();
			}

			switch (AGENT_SCALING_METRIC) {
			case everyNjobs:
				long currentEnqueueCount = iqueue.getEnqueueCount() - initialDequeueCount;
				logger.info("| Every " + MAX_ENQUEUE_COUNT_PER_WORKER + " Jobs (" + "CurrentEnqueueCount: " + currentEnqueueCount + " LastLauncedEnqueueCount: " + lastLaunchedEnqueueCount + ")");

				if (currentEnqueueCount - lastLaunchedEnqueueCount >= MAX_ENQUEUE_COUNT_PER_WORKER) {
					lastLaunchedEnqueueCount += MAX_ENQUEUE_COUNT_PER_WORKER;
					return true;
				}
				break;
			case avgEnqueueTime:
				logger.info("avgEnqueueTime");
				double atime = iqueue.getAverageEnqueueTime();
				if (atime >= THRESHOLD_AVG_ENQUEUE_TIME) {
					return true;
				}
				break;
			case maxEnqueueTime:
				logger.info("maxEnqueueTime");
				double mtime = iqueue.getMaxEnqueueTime();
				if (mtime >= THRESHOLD_MAX_ENQUEUE_TIME) {
					return true;
				}
				break;
			case addNagents:
				logger.info("addNagents");
				if (!checkUserThread) {
					Thread uThread = new UserAgentKeeper();
					uThread.setDaemon(true);
					uThread.start();
					checkUserThread = true;
				}
				return true;
			case keepQagents:
				logger.info("keepQagents");
				if (!checkUserThread) {
					Thread uThread = new UserAgentKeeper();
					uThread.setDaemon(true);
					uThread.start();
					checkUserThread = true;
				}
				return true;

			case keepStaticAgents:
				logger.info("keepStaticagents");
				if (!checkUserThread) {
					Thread uThread = new UserAgentKeeper();
					uThread.setDaemon(true);
					uThread.start();
					checkUserThread = true;
				}
				return true;

			case dynamicFairness:
				logger.info("dynamicFairness");
				if (!checkUserThread) {
					Thread uThread = new UserAgentKeeper();
					uThread.setDaemon(true);
					uThread.start();
					checkUserThread = true;
				}
				return true;

			case testAllCombination:
				logger.info("testAllCombination");
				logger.info("| numLaunchedAgents: " + numTotalSubmittedAgents);
				if (!testCompleted) {
					numTotalSubmittedAgents++;
					return true;
				}
				break;

			case gridScout:
				logger.info("gridScout");
				if (!checkUserThread) {
					Thread uThread = new UserAgentKeeper();
					uThread.setDaemon(true);
					uThread.start();
					checkUserThread = true;
				}
				return true;

			default:
				logger.error("Wrong AGENT_SCALING_METRIC: " + AGENT_SCALING_METRIC + " automatically set to default 'everyNjobs'");
				AGENT_SCALING_METRIC = everyNjobs;
			} // switch
		}

		return false;
	}*/

	// AgentManager::selectCE
	private String[] selectCE(int validSId, String userId) {
		logger.info("SelectCE :" + validSId);
		String[] pair = null;
		
		MetaJob mJob = dbClient.getMetaJobLastRunningFromUser(userId);
		
		if(mJob.getCe()!=null && !mJob.getCe().isEmpty()){
			
			String[] ces = mJob.getCe().split(",");
			Set<Integer> siSetFromMetaJob = new HashSet<Integer>();
			
			for(String ce : ces){
				siSetFromMetaJob.add(dbClient.getCEServiceInfraId(Integer.parseInt(ce)));
			}
			
			for(Integer si : siSetFromMetaJob){
				
				if(validSISet.contains(si)){
						continue;
				}else{
					logger.error("Not matched serviceinfra from metajob and validSISet :" + userId + ", " + si + "metajob is being canceled" );
					dbClient.setMetaJobStatus(mDTO.getMetaJobId(), Constant.METAJOB_STATUS_CANCEL);
					dbClient.setMetaJobError(mDTO.getMetaJobId(), "Not matched serviceinfra from validSISet");
					MessageCommander mc = new MessageCommander();
					int num = mc.removeMessage(mDTO.getMetaJobId(), userId);
					logger.info(num + "jobs are removed");
					return pair;
				}
			}
			
			for(String ceId : ces){
				CE ce = dbClient.getCEObject(Integer.parseInt(ceId));
				if(!ce.isBanned() && ce.isAvailable()){
					pair = new String[1];
					pair[0] = ce.getName();
					
					validSId = dbClient.getCEServiceInfraId(Integer.parseInt(ceId));
					
					break;
				}
			}
			String name = dbClient.getServiceInfraName(validSId);
			
			resource = rscScheduler.chooseOne(name);
			
			return pair;
			
			
		}else{
			String name = dbClient.getServiceInfraName(validSId);
			
			resource = rscScheduler.chooseOne(name);
			
			
			if (resource instanceof GliteResource) {
				logger.info("| GliteResource");
				
				GliteResource gr = (GliteResource) resource;
				// pair = gr.getNextWMSandCE(CE_SELECTION_METRIC);
				String ceName = gr.getNextCEName(CE_SELECTION_METRIC);
				if (ceName == null) {
					logger.info("No Next WMS and CE combination");
					return null;
				}
				pair = new String[2];
				
				pair[0] = ceName;
				pair[1] = "CREAM";
				
				logger.info("ce:" + pair[0] + ", wms:" + pair[1]);
				
			} else if (resource instanceof LLResource) {
				logger.info("| LoadLeveler");
				
				LLResource sc = (LLResource) resource;
				String ceName = sc.getNextCEName(CE_SELECTION_METRIC);
				// String ceName = sc.getNextCE(CE_SELECTION_METRIC);
				if (ceName == null) {
					logger.error("No Next CE ");
					return null;
				} else {
					pair = new String[1];
					pair[0] = ceName;
				}
				
				logger.info("ce:" + pair[0]);
				
			} else if (resource instanceof ClusterResource) {
				logger.info("| ClusterResource");
				
				ClusterResource cr = (ClusterResource) resource;
				String ceName = cr.getNextCEName(CE_SELECTION_METRIC);
				
				if (ceName == null) {
					logger.error("No Next CE ");
					return null;
				} else {
					pair = new String[1];
					pair[0] = ceName;
				}
				
				logger.info("ce:" + pair[0]);
			} else if (resource instanceof CondorResource) {
				logger.info("| CondorResource");
				
				CondorResource cr = (CondorResource) resource;
				String ceName = cr.getNextCEName(CE_SELECTION_METRIC);
				
				if (ceName == null) {
					logger.error("No Next CE ");
					return null;
				} else {
					pair = new String[1];
					pair[0] = ceName;
				}
				
				logger.info("ce:" + pair[0]);
			} else if (resource instanceof SGEResource) {
				logger.info("| SGEResource");
				
				SGEResource sr = (SGEResource) resource;
				String ceName = sr.getNextCEName(CE_SELECTION_METRIC);
				
				if (ceName == null) {
					logger.error("No Next CE ");
					return null;
				} else {
					pair = new String[1];
					pair[0] = ceName;
				}
				
			} else if (resource instanceof OpenstackResource) {
				logger.info("| OpenstackResource");
				
				OpenstackResource or = (OpenstackResource) resource;
				String ceName = or.getNextCEName(CE_SELECTION_METRIC);
				
				if (ceName == null) {
					logger.error("No Next CE ");
					return null;
				} else {
					pair = new String[1];
					pair[0] = ceName;
				}
			} else if (resource instanceof CloudResource) {
				logger.info("| CloudResource");
				
				logger.info("Not implemented");
				// CloudResource cr = (CloudResource) resource;
				// String ceName = cr.
				//
				// if (ceName == null) {
				// logger.error("No Next CE ");
				// return null;
				// } else {
				// pair = new String[1];
				// pair[0] = ceName;
				// }
				//
				// logger.info("ce:" + pair[0]);
			} else if (resource instanceof LocalMachine) {
				logger.info("| LocalResource");
				
				LocalMachine lm = (LocalMachine) resource;
				String ceName = lm.getNextCEName(CE_SELECTION_METRIC);
				
				if (ceName == null) {
					logger.error("No Next CE ");
					return null;
				} else {
					pair = new String[1];
					pair[0] = ceName;
				}
				
				logger.info("ce:" + pair[0]);
			} else {
				logger.error("| Unknown Resource Type");
				return null;
			}
			
			return pair;
		}

	}

	// AgentManager::launchWorker
	// 새로운 에이전트를 라운치한다.
	private void launchWorker(String userId, String[] pair, int num) {
		logger.info("| Preparing to launch a new Agent...");

		// g-Lite
		if (resource instanceof GliteResource) {
			logger.info("| GliteResource");

			GliteResource gr = (GliteResource) resource;

			String ceName = pair[0];
			String wmsName = pair[1];

			if (ceName.contains("cream") && wmsName.equals("CREAM")) {
				for (int i = 0; i < num; i++) {
					submitQueue.addJob(new GliteJob(gr, wmsName, ceName, "Glite-CREAM", userId, metaMap.get(userId)));
				}
			} else {
				for (int i = 0; i < num; i++) {
					submitQueue.addJob(new GliteJob(gr, wmsName, ceName, "Glite", userId, metaMap.get(userId)));
				}
			}

			// Local Machine
		} else if (resource instanceof LocalMachine) {

			logger.info("| LocalMachine");
			String ceName = pair[0];
			submitQueue.addJob(new LocalJob((LocalMachine) resource, userId, ceName));

			// 슈퍼컴퓨터
			// 주어진 개수 (num) 를 한꺼번에 실행함
		} else if (resource instanceof LLResource) {
			logger.info("| SuperComputer");

			LLResource sc = (LLResource) resource;

			String ceName = pair[0];

			// 큐에 KSCJob 작업을 만들어 넣음
			if (sc.getVoName().equals("PLSI")) {
				submitQueue.addJob(new LLJob(sc, ceName, "LoadL", userId, metaMap.get(userId), num));
			} else if (sc.getVoName().equals("4TH")) {
				submitQueue.addJob(new LLJob(sc, ceName, "SGE", userId, metaMap.get(userId), num));
			} else {
				logger.error("resource not match the ServiceInfra name");
			}

			// Cloud
		} else if (resource instanceof CloudResource) {
			logger.info("| CloudResource");

			CloudResource cr = (CloudResource) resource;
			submitQueue.addJob(new CloudJob(cr, "SSH"));

			// Cluster
		} else if (resource instanceof ClusterResource) {
			logger.info("| Cluster");

			User user = dbClient.getUserInfo(userId);
			ClusterResource clr = (ClusterResource) resource;

			String ceName = pair[0];

			if (clr.getVoName().equals("pbs")) {
				// submitQueue.addJob(new ClusterJob(clr, ceName,"pbs", userId,
				// metaMap.get(userId), num));
				for (int i = 0; i < num; i++) {
					submitQueue.addJob(new ClusterJob(clr, ceName, "pbs", userId, metaMap.get(userId), user.isShared()));
				}
			} else {
				logger.error("resource not match the ServiceInfra name");
			}
		} else if (resource instanceof CondorResource) {
			logger.info("| Condor");

			CondorResource cr = (CondorResource) resource;

			String ceName = pair[0];

			if (cr.getVoName().equals("condor")) {
				submitQueue.addJob(new CondorJob(cr, ceName, "condor", userId, metaMap.get(userId), num));
			} else {
				logger.error("resource not match the ServiceInfra name");
			}
		} else if (resource instanceof SGEResource) {
			logger.info("| SGE");
			
			User user = dbClient.getUserInfo(userId);
			SGEResource sr = (SGEResource) resource;
			
			String ceName = pair[0];
			
			if (sr.getVoName().equals("sge")) {
				for (int i = 0; i < num; i++) {
					submitQueue.addJob(new SGEJob(sr, ceName, "sge", userId, metaMap.get(userId), user.isShared()));
				}
			} else {
				logger.error("resource not match the ServiceInfra name");
			}
			
		} else if (resource instanceof OpenstackResource) {
			logger.info("| OpenstackResource");
			OpenstackResource or = (OpenstackResource) resource;
			or = new OpenstackResource("openstack");
			int suspendVMcnt = 0;				
			String ceName = pair[0];
			
			if (or.getVoName().equals("openstack")) {
				or.suspend_sl = or.getSuspendInstanceInfo();
				suspendVMcnt = or.suspend_sl.size();
//				suspendVMcnt=or.getSuspendInstanceInfo().size();
					// vmType: 1=Suspended, 2=New VM 
				 	if (suspendVMcnt > 0 ) { 
				 		logger.info("| Suspended VM # : " + suspendVMcnt); 
				 		if (suspendVMcnt >= num) {
				 			for(int i = 0; i< num;i++){ 
				 				logger.info("| sus #: " + i );
				 				submitQueue.addJob(new OpenstackJob(or, ceName, "openstack", userId, metaMap.get(userId), 1 , i)); 	
				 			}
				 		} else { // num > suspendVMcnt
				 			int temp = num-suspendVMcnt;
				 			for(int i = 0; i< suspendVMcnt;i++){
				 				submitQueue.addJob(new OpenstackJob(or, ceName, "openstack", userId, metaMap.get(userId), 1 , i));
				 			}
				 			
				 			for(int j = 0; j< temp;j++){
				 				submitQueue.addJob(new OpenstackJob(or, ceName, "openstack", userId, metaMap.get(userId), 2 , 0)); 
				 			}
				 		}
					} else { 
						for(int i = 0; i< num;i++){
							submitQueue.addJob(new OpenstackJob(or, ceName, "openstack", userId, metaMap.get(userId), 2, 0)); 		
			 			}
					}
			} else {
				logger.error("resource not match the ServiceInfra name");
			}	
		} else {
			logger.error("| Unknown Resource Type");
			return;
		}

		// agentNumberTag++;
	}

	

	static Set<Integer> validSISet = new HashSet<Integer>();

	// AgentManager::initializeBackendResources
	private void initializeBackendResources() {
		logger.info("[1.AM] + Initializing BackendResources");

		synchronized (validSISet) {
			validSISet.clear();
			ceSize = 0;
			
			for (BackendResource br : brList) {
				if (br.isEnabledForDB() && br.isEnabledForAM()) {
					logger.info("[1.AM] BackendResource :" + br.getName() + " Available");
					validSISet.add(br.getId());
					try {
						if (br instanceof GliteResource) {
							if (br.getName().equals(BIOMED)) {
								biomed = new GliteResource(BIOMED);
								biomed.setMaxJobNum(100);
								
								if (biomed.getVomsProxyTimeLeft() < 3600 * 2) {
									biomed.initVomsProxy();
								} // need edit!
								biomed.updateCEInfo();
								if (SAMPLING_ADD_AGENT_NO == -1) {
									SAMPLING_ADD_AGENT_NO = biomed.getCEList(CE_SELECTION_METRIC);
								}
								logger.info("get CE List ....");
								ceSize += biomed.getCEList(CE_SELECTION_METRIC);
								
								logger.info("[1.AM] ceSize = " + ceSize);
								dbClient.initCESubmitCount(biomed.getName());
								
								rscScheduler.addResource(biomed);
								logger.info("[1.AM] | New BackendResources(" + biomed.getName() + ") Prepared");
							} else if (br.getName().equals(VOFA)) {
								vofa = new GliteResource(VOFA);
								vofa.setMaxJobNum(100);
								if (vofa.getVomsProxyTimeLeft() < 3600 * 2) {
									vofa.initVomsProxy();
								} // need edit!
								
								vofa.updateCEInfo();
								if (SAMPLING_ADD_AGENT_NO == -1) {
									SAMPLING_ADD_AGENT_NO = vofa.getCEList(CE_SELECTION_METRIC);
								}
								logger.info("[1.AM] get CE List ....");
								ceSize += vofa.getCEList(CE_SELECTION_METRIC);
								
								logger.info("[1.AM] ceSize = " + ceSize);
								dbClient.initCESubmitCount(vofa.getName());
								
								rscScheduler.addResource(vofa);
								logger.info("[1.AM] | New BackendResources(" + vofa.getName() + ") Prepared");
							}
						} else if (br instanceof LLResource) {
							plsi = new LLResource(PLSI);
							plsi.setMaxJobNum(10);
							
							// sc.updateCEInfo();
							logger.info("[1.AM] get CE List ....");
							ceSize += plsi.getCEList(CE_SELECTION_METRIC);
							logger.info("[1.AM] ceSize = " + ceSize);
							dbClient.initCESubmitCount(plsi.getName());
							
							rscScheduler.addResource(plsi);
							logger.info("[1.AM] | New BackendResources(" + plsi.getName() + ") Prepared");
						} else if (br instanceof CloudResource) {
							
						} else if (br instanceof ClusterResource) {
							cluster = new ClusterResource(PBS);
							cluster.setMaxJobNum(10);
							
							cluster.updateCEInfo();
							logger.info("[1.AM] get CE List ....");
							ceSize += cluster.getCEList(CE_SELECTION_METRIC);
							
							logger.info("[1.AM] ceSize = " + ceSize);
							dbClient.initCESubmitCount(cluster.getName());
							
							rscScheduler.addResource(cluster);
							logger.info("[1.AM] | New BackendResources(" + cluster.getName() + ") Prepared");
						} else if (br instanceof LocalMachine) {
							LocalMachine lm = new LocalMachine(LOCAL);
							lm.setMaxJobNum(10);
//							if (lm.getVomsProxyTimeLeft() < 3600 * 2) {
//								lm.initVomsProxy();
//							}
							logger.info("[1.AM] get CE List ....");
							ceSize += 1;
							
							logger.info("[1.AM] ceSize = " + ceSize);
							rscScheduler.addResource(lm);
							logger.info("[1.AM] | New BackendResources(" + lm.getName() + ") Prepared");
						} else if (br instanceof CloudResource) {
							// not implemented
						} else if (br instanceof CondorResource) {
							// condor = new CondorResource("condor");
							condor = (CondorResource) br;
							condor.setMaxJobNum(10);
							
							// sc.updateCEInfo();
							logger.info("[1.AM] get CE List ....");
							ceSize += condor.getCEList(CE_SELECTION_METRIC);
							logger.info(" ceSize = " + ceSize);
							dbClient.initCESubmitCount(condor.getName());
							
							rscScheduler.addResource(condor);
							logger.info("[1.AM] | New BackendResources(" + condor.getName() + ") Prepared");
						} else if (br instanceof SGEResource) {
							sge = new SGEResource("sge");
							sge.setMaxJobNum(10);
							
							sge.updateCEInfo();
							logger.info("[1.AM] get CE List ....");
							ceSize += sge.getCEList(CE_SELECTION_METRIC);
							
							logger.info("[1.AM] ceSize = " + ceSize);
							dbClient.initCESubmitCount(sge.getName());
							
							rscScheduler.addResource(sge);
							logger.info("[1.AM] | New BackendResources(" + sge.getName() + ") Prepared");
							
						}
						else if (br instanceof OpenstackResource) {
							openstack = new OpenstackResource("openstack");
							openstack.setMaxJobNum(10);
							
							openstack.updateCEInfo();
							logger.info("[1.AM] get CE List ....");
							ceSize += openstack.getCEList(CE_SELECTION_METRIC);
							
							logger.info("[1.AM] ceSize = " + ceSize);
							dbClient.initCESubmitCount(openstack.getName());
							
							rscScheduler.addResource(openstack);
							logger.info("[1.AM] | New BackendResources(" + openstack.getName() + ") Prepared");
							
						}
					
					} catch (Exception e) {
						error_and_exit("[1.AM] backend resource init error :" + e.getMessage());
					}
					
				}
			}
		}

		// try {
		// SuperComputer sc = new SuperComputer("TACHYON2");
		// sc.setMaxJobNum(10);
		//
		// rscScheduler.addResource(sc);
		// logger.info("| New BackendResources(" + sc.getName() + ") Prepared");
		// } catch (Exception e) {
		// logger.error("Cannot initialize a backend resource", e);
		// System.exit(1);
		// }

		// try {

		// } catch (Exception e) {
		// logger.error("Cannot initialize a backend resource", e);
		// System.exit(1);
		// }
	}

	public void prepareBackendResource() {
		logger.info("[1.AM] Preparing BackenResource from DB");

		List<ServiceInfra> sList = dbClient.getServiceInfraObjects();

		for (ServiceInfra si : sList) {
			if (si.isAvailable()) {
				logger.info("[1.AM] ServiceInfra : " + si.getName() + " is available");
				if (si.getName().equals(PLSI)) {
					BackendResource br = new LLResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(BIOMED)) {
					BackendResource br = new GliteResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(VOFA)) {
					BackendResource br = new GliteResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(AMAZONEC2)) {
					BackendResource br = new CloudResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(LOCAL)) {
					BackendResource br = new LocalMachine(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(PBS)) {
					BackendResource br = new ClusterResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(CONDOR)) {
					BackendResource br = new CondorResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());

					brList.add(br);
				} else if (si.getName().equals(SGE)) {
					BackendResource br = new SGEResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());
					
					brList.add(br);
				}
				else if (si.getName().equals(OPENSTACK)) {
					BackendResource br = new OpenstackResource(si.getName());
					br.setPriority(si.getPriority());
					br.setEnabledForDB(true);
					br.setServicecode(si.getServiceCode());
					br.setId(si.getId());
					
					brList.add(br);
				} else {
					logger.warn("[1.AM] Not registered Resource. Check resource");
				}
			}
		}
	}

	public void setServiceInfraEnableFromAM() {
		logger.info("[1.AM] Set ServiceInfraEnable from AM_Env");

		initBackendResourceListEnabledAM();
		// env = dbClient.getAMEnv(amId);
		// SERVICE_INFRA_SET = (String) env.get("service_Infra_id");
		// SERVICE_INFRA_SET = "2";

		logger.info("[1.AM] Service_Infra_set:" + SERVICE_INFRA_SET);

		String[] sid = SERVICE_INFRA_SET.split(",");
		for (String si : sid) {
			int id = Integer.parseInt(si.trim());

			for (BackendResource br : brList) {
				if (br.getId() == id) {
					br.setEnabledForAM(true);
					logger.info("[1.AM] " + br.getName() + " Resource is set");
				}
			}
		}
	}

	public void initBackendResourceListEnabledAM() {
		logger.info("[1.AM] Set all the backendResource list to false");
		for (BackendResource br : brList) {
			br.setEnabledForAM(false);
		}
	}

	public void checkServiceInfraFromAM(String serviceInfraSet_new) {
		logger.info("[1.AM] Cheking Service Infra Set");

		if (serviceInfraChangeFlag && (numLaunchedAgents == 0)) {
			logger.info("[1.AM] Service Infra Set is changed and runningAgent is 0. Initializing resource...");
			
			SERVICE_INFRA_SET = serviceInfraSet_new;
			setServiceInfraEnableFromAM();
			rscScheduler.initResourceList();
			initializeBackendResources();

			serviceInfraChangeFlag = false;

		} else if (serviceInfraChangeFlag && (numLaunchedAgents != 0)) {
			logger.info("[1.AM] Service Infra Set is changed, but runningAgent is not 0. Skipping to initialize resource");
		} else {
			logger.info("[1.AM] Service infra is not changed");
		}
	}

	// AgentManager::run()
	public void run() {

		logger.info("[1.AM] + AgentManager started");

		prepareBackendResource();

		setServiceInfraEnableFromAM();

		initializeBackendResources();

		logger.info("[1.AM] *** AGENT_SCALING_METRIC = " + AGENT_SCALING_METRIC);
		logger.info("[1.AM] *** everyNjobs = " + everyNjobs);
		logger.info("[1.AM] *** addNagents = " + addNagents);
		logger.info("[1.AM] *** keepQagents = " + keepQagents);
		logger.info("[1.AM] *** dynamicFairness = " + dynamicFairness);

		if (AGENT_SCALING_METRIC == testAllCombination) {
			testCompleted = false;
		}

		if (AGENT_SCALING_METRIC == everyNjobs) {
			logger.info("+ For the case of everyNjobs, initial check on the previous status");
			initialDequeueCount = iqueue.getDequeueCount();
			logger.info("| initialDequeueCount: " + initialDequeueCount);

//			numLaunchedAgents = dbClient.getNumAliveAgent(runningAgentHeartbeatPeriod);
			logger.info("| " + numLaunchedAgents + " agents are alive now" + " and EnqueueCount per worker is " + MAX_ENQUEUE_COUNT_PER_WORKER);
			lastLaunchedEnqueueCount = numLaunchedAgents * MAX_ENQUEUE_COUNT_PER_WORKER;

			// TEST
			lastLaunchedEnqueueCount = 0;

			logger.info("| So, LastLauncedEnqueueCount: " + lastLaunchedEnqueueCount);

		} else if (AGENT_SCALING_METRIC == addNagents) {
			logger.info("+ Simply add N new agents");
			numLaunchedAgents = 0;

		} else if (AGENT_SCALING_METRIC == keepQagents) {
			logger.info("+ Intelligently add and keep N new agents");
			numLaunchedAgents = 0;

		} else if (AGENT_SCALING_METRIC == dynamicFairness) {
			logger.info("[1.AM] + Dynamic Fairness Assignment agents");
			numLaunchedAgents = 0;
		}

		// Launch StatusManager
//		Thread mThread = new StatusManager(this);
//		mThread.setDaemon(true);
//		mThread.start();

		// Launch ZombieChecker
		Thread zThread = new ZombieChecker();
		zThread.setDaemon(true);
		zThread.start();

		// Launch ProxyRenewer
		Thread pThread = new ProxyRenewer(this);
		pThread.setDaemon(true);
		pThread.start();

		int waitingTime = 30;
//		int count = 0;
//		int qstatCount = 0;

		logger.info("[1.AM] + Submission start");

		// 작업 제출을 위한 큐
		submitQueue = new SubmitWorkQueue(this, "SubmitWorkQueue", numSubmitWorkQueue);

		// Temporary code
		Thread uThread = new UserAgentKeeper();
		uThread.setDaemon(true);
		uThread.start();
		checkUserThread = true;

		String SERVICE_INFRA_SET_NEW = null;

		while (true) {

			try {
				logger.info("[1.AM] ==========1.Main Thread:Checking AgentManager Environment Mode==========");

				logger.info("[1.AM] SubmitWorkerThread Info - " + submitQueue.currentInfo());

				// 1. read config file
				try {
					Properties prop = new Properties();

					logger.info("loading properties file " + configPath);
					prop.load(new FileInputStream(configPath));

					String value1 = prop.getProperty("RunningAgent.Heartbeat.Period");
					if (value1 != null) {
						runningAgentHeartbeatPeriod = Integer.parseInt(value1);
					} else {
						logger.error("RunningAgent.Heartbeat.Period is null. Using default value: 5");
						runningAgentHeartbeatPeriod = 5;
					}
					logger.info("RunningAgentHeartbeatPeriod: " + runningAgentHeartbeatPeriod);

					String value2 = prop.getProperty("SubmittedAgent.Heartbeat.Period");
					if (value2 != null) {
						submittedAgentHeartbeatPeriod = Integer.parseInt(value2);
					} else {
						logger.error("SubmittedAgent.Heartbeat.Period is null. Using default value: 1");
						submittedAgentHeartbeatPeriod = 1;
					}
					logger.info("SubmittedAgentHeartbeatPeriod: " + submittedAgentHeartbeatPeriod);

					String value3 = prop.getProperty("NewAgent.Heartbeat.Period");
					if (value3 != null) {
						newAgentHeartbeatPeriod = Integer.parseInt(value3);
					} else {
						logger.error("NewAgent.Heartbeat.Period is null. Using default value: 1");
						newAgentHeartbeatPeriod = 1;
					}
					logger.info("NewAgentHeartbeatPeriod: " + newAgentHeartbeatPeriod);

					// statusMonitoringHeartbeatPeriod
					String value4 = prop.getProperty("StatusMonitoring.Heartbeat.Period");
					if (value4 != null) {
						statusMonitoringHeartbeatPeriod = Integer.parseInt(value4);
					} else {
						logger.error("StatusMonitoring.Heartbeat.Period is null. Using default value: 1");
						statusMonitoringHeartbeatPeriod = 1;
					}
					logger.info("StatusMonitoringHeartbeatPeriod: " + statusMonitoringHeartbeatPeriod);

					// ResourceAvailablePeriod
					String value5 = prop.getProperty("Resource.Available.Period");
					if (value5 != null) {
						resourceAvailablePeriod = Integer.parseInt(value5);
					} else {
						logger.error("Resource.Available.Period is null. Using default value: 120");
						resourceAvailablePeriod = 120;
					}
					logger.info("ResourceAvailablePeriod: " + resourceAvailablePeriod);

					// ZombieAgentMonitoringPeriod
					String value6 = prop.getProperty("ZombieAgent.Monitoring.Period");
					if (value6 != null) {
						zombieAgentMonitoringPeriod = Integer.parseInt(value6);
					} else {
						logger.error("ZombieAgent.Monitoring.Period is null. Using default value: 30sec");
						zombieAgentMonitoringPeriod = 30;
					}
					logger.info("ZombieAgent.Monitoring.Period: " + zombieAgentMonitoringPeriod);
					
					AGENT_SCALING_METRIC = Integer.parseInt(prop.getProperty("AGENT_SCALING_METRIC"));
					logger.info("AGENT_SCALING_METRIC: " + AGENT_SCALING_METRIC);
					
					CE_SELECTION_METRIC = Integer.parseInt(prop.getProperty("CE_SELECTION_METRIC"));
					logger.info("CE_SELECTION_METRIC: " + CE_SELECTION_METRIC);
					
					SERVICE_INFRA_SET_NEW = prop.getProperty("SERVICE_INFRA_SET");
					 if (SERVICE_INFRA_SET.equals(SERVICE_INFRA_SET_NEW)) {
						 logger.info("SERVICE_INFRA_SET: " + SERVICE_INFRA_SET);
					 } else {
					 logger.info("service_Infra_set:" + SERVICE_INFRA_SET);
					 logger.info("service_Infra_set_new:" + SERVICE_INFRA_SET_NEW);
					 serviceInfraChangeFlag = true;
					 }
					

					fileEnabled = Boolean.parseBoolean(prop.getProperty("File_Enabled"));
					logger.info("File_Enabled: " + fileEnabled);

					pTestAgentEnabled = Boolean.parseBoolean(prop.getProperty("PTestAgent_Enabled"));
					logger.info("PTestAgent_Enabled: " + pTestAgentEnabled);

					dWallClockTime = Integer.parseInt(prop.getProperty("Default_WallClockTime"));
					logger.info("Default_WallClockTime : " + dWallClockTime);

					aWallClockTime = Integer.parseInt(prop.getProperty("Additional_WallClockTime"));
					logger.info("Additional_WallClockTime : " + aWallClockTime);

					agentStorageAddress = prop.getProperty("Agent.Storage.Address");
					logger.info("Agent Storage Adress : " + agentStorageAddress);

					PLSI_Remote_Home = prop.getProperty("PLSI_Remote_Home");
					logger.info("PLSI Remote Home : " + PLSI_Remote_Home);

					Default_Remote_Home = prop.getProperty("Default_Remote_Home");
					logger.info("Default Remote Home : " + Default_Remote_Home);

					numSubmitWorkQueue = Integer.parseInt(prop.getProperty("Num_SubmitThread"));
					logger.info("The number of submitWork threads : " + numSubmitWorkQueue);

					createDirectories();

				} catch (Exception e) {
					error_and_exit("Failed to load config file and property: " + e.getMessage());
				}
				
//				 if (!dbClient.getAMEnvAutoMode(amId)) {
//				 logger.info("HTCaaS Manual Mode! Getting AgentManager Environment");
//				 env = dbClient.getAMEnv(amId);
//				 SERVICE_INFRA_SET_NEW = (String) env.get("service_Infra_id");
//				 if (SERVICE_INFRA_SET.equals(SERVICE_INFRA_SET_NEW)) {
//				 logger.info("service_Infra_set:" + SERVICE_INFRA_SET);
//				 } else {
//				 logger.info("service_Infra_set_new:" + SERVICE_INFRA_SET_NEW);
//				 serviceInfraChangeFlag = true;
//				 }
//				 AGENT_SCALING_METRIC = (Integer)
//				 env.get("agentScalingMetric_id");
//				 logger.info("agentScalingMetric:" + AGENT_SCALING_METRIC);
//				 ADD_AGENT_NO = (Integer) env.get("addAgentNO");
//				 logger.info("addAgentNo:" + ADD_AGENT_NO);
//				
//				 CE_SELECTION_METRIC = (Integer)env.get("ceSelectionMetric_id");
//				 logger.info("ceSelectionMetric:" + CE_SELECTION_METRIC);
//				
////				 runningAgentHeartbeatPeriod = (Integer)env.get("runningAgentHP");
////				 submittedAgentHeartbeatPeriod = (Integer)env.get("submittedAgentHP");
////				 newAgentHeartbeatPeriod = (Integer) env.get("newAgentHP");
//				
//				
//				 statusMonitoringHeartbeatPeriod = (Integer)
//				 env.get("statusMonitoringHP");
//				 resourceAvailablePeriod = (Integer) env.get("resourceAP");
//				
//				 } else {
//				 logger.info("Autonomous Mode!");
//				 }

				logger.info("[1.AM] Checking Available Resource ceSize = " + ceSize);

				// if (ceSize < 1) {
				// logger.info("No Free CPU. Wating for Available  Resource. sleep 5000");
				// Thread.sleep(5000);
				// checkServiceInfraFromAM();
				// updateCEInfo();
				// continue;
				// }

				logger.info("[1.AM] + Checking Running Agent");

				numLaunchedAgents = 0;

				Set<Integer> uidSet = dbClient.getUserIdFromAgent(AgentConstant.AGENT_STATUS_RUN);

				for (Integer uid : uidSet) {
					Map<String, Integer> ceMap = dbClient.getNumUserAgentCE(AGENT_STATUS_RUN, uid);

					int tot = 0;
					if (ceMap != null && ceMap.size() > 0) {
						logger.info("[1.AM] User(" + uid + ") Resource : " + ceMap);
						int num = 0;
						for (String key : ceMap.keySet()) {
							num += ceMap.get(key);
						}
						tot += num;
					}

					numLaunchedAgents += tot;
					logger.info("[1.AM] User(" + uid + ") agent : " + tot);
				}

				// numLaunchedAgents = dbClient.getNumAgent(AGENT_STATUS_RUN);

				logger.info("[1.AM] | # of Running agents: " + numLaunchedAgents);

				// if (iqueue.getQueueSize() == 0) {
				// if (count < 4)
				// waitingTime *= 2;
				//
				// count++;
				//
				// logger.info("| No need to launch a new agent, sleeping for "
				// + waitingTime + " secs");
				// } else {
				// waitingTime = 2;
				// count = 0;
				// }

				checkServiceInfraFromAM(SERVICE_INFRA_SET_NEW);

				logger.info("[1.AM] | Sleeping for " + waitingTime + " secs");
				Thread.sleep(waitingTime * 1000);

				// qstatCount++;
				// if (qstatCount == 5) {
				// logger.info(submitQueue.currentInfo());
				// qstatCount = 0;
				// }

			} catch (InterruptedException e) {
				e.printStackTrace();
				logger.info("[1.AM] + AgentManager finished");
				System.exit(0);
			} catch (Exception e2) {
				logger.error(e2.toString());
				e2.printStackTrace();
				logger.error("[1.AM] AgentManager Error : Sleeping " + THREADWAITTIME / 1000 + "s to repeat AgentManager...");
				try {
					Thread.sleep(THREADWAITTIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void error_and_exit(String msg) {
		logger.error(msg);
		System.exit(1);
	}

	/*
	 * public void test() { while (true) {
	 * logger.info("+ Finding Zombie Agents");
	 * 
	 * try { Thread.sleep(runningAgentHeartbeatPeriod * 3 * 60 * 1000); } catch
	 * (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } } }
	 */

	public static void main(String[] args) throws Exception {
		AgentManager am = AgentManager.getInstance();
		// am.initializeBackendResources();
		am.run();
		// List<Integer> list = new ArrayList<Integer>();
		// list.add(13534);
		// am.cancelLLSubmittedJob();
		// am.checkAvailableCE(20);
		// am.selectCE(4);

		// am.test();
	}

	private class ProxyRenewer extends Thread {

		private AgentManager am;

		public ProxyRenewer(AgentManager am) {
			this.am = am;
		}

		@Override
		public void run() {

			while (true) {

				for (BackendResource br : brList) {
					if (br.getServicecode().equals(CODE_GRID)) {
						if (br.isEnabledForDB() && br.isEnabledForAM()) {
							logger.info("[4.PR] + Renewing Glite Proxies");
							GliteResource gm = (GliteResource) br;
							try {
								logger.info("[4.PR] | GliteResource: " + br.getName());
								int timeleft = gm.getVomsProxyTimeLeft();
								if (timeleft < 3600 * 2) {
									gm.initVomsProxy();
									logger.info("[4.PR] | Proxy Recreated");
								}
							} catch (Exception e) {
								logger.error("[4.PR] | Failed to renew a Glite Proxy" + e.toString());
							}
						}
					}
				}

				try {
					Thread.sleep(1 * 60 * 60 * 1000); // every an hour
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	private class ZombieChecker extends Thread {

		// ZombieChecker::run
		@Override
		public void run() {

			// List<Integer> rlist = null;

			while (true) {
				logger.info("[5.ZC]+ Finding... Zombie Agents ");

				try {

					// String[] si = SERVICE_INFRA_SET.split(",");
					List<ServiceInfra> serviceInfra = dbClient.getServiceInfraObjects();
					
					synchronized (validSISet) {
						for(Integer vsi : validSISet){
							for (ServiceInfra si : serviceInfra) {
								if(si.getId() == vsi){
									logger.info("[5.ZC] | " + si.getName());
									logger.info("[5.ZC] CheckRunningZombie : " + dbClient.checkRunningZombieAgentJob(si.getRunningAgentHP(), si.getId()));
									logger.info("[5.ZC] CheckNewZombie : " + dbClient.checkNewZombieAgent(si.getNewAgentHP(), si.getId()));
									logger.info("[5.ZC] CheckSubmittedZombie : " + dbClient.checkSubmittedZombieAgent(si.getSubmittedAgentHP(), si.getId()));
								}
							}
						}
					}
					
					for (BackendResource br : brList) {
						br.cancelZombieJob();
					}

					try {
						Thread.sleep(zombieAgentMonitoringPeriod * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} catch (Exception e2) {
					logger.error(e2.toString());
					e2.printStackTrace();
					logger.error("[5.ZC] ZombieChecker Error: Sleeping " + THREADWAITTIME + "s to repeat Zombiechecker...");
					try {
						Thread.sleep(THREADWAITTIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

		}
	}

	//private class StatusManager extends Thread {
	private class StatusManager {

		private AgentManager am;

		public StatusManager() {
		}
		
		public StatusManager(AgentManager am) {
			this.am = am;
		}

		public int getTotalFreeCPU() {
			// String sIdSet =
			// dbClient.getServiceInfraIdSet(SERVICE_INFRA_METRIC);
			// String[] sid = SERVICE_INFRA_SET.split(",");
			int fcpu = 0;
			for (int si : validSISet) {
				fcpu = fcpu + dbClient.getCEFreeCPUTotal(si);
			}
			return fcpu;
		}
		
		// StatusManager::updateCEInfo
		synchronized public void updateCEInfo() {
			try {
				ceSize = 0;
				for (BackendResource br : brList) {
					if (br.enabledForDB && br.enabledForAM) {
						logger.info("[2.SM] Updating " + br.getName() + " CE INFO");
						br.updateCEInfo();
						ceSize += br.getCEList(CE_SELECTION_METRIC);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		// StatustManager::checkCEAvailable
		public void checkCEAvailable() {
			logger.info("[2.SM] Check unavailable CEs.");

			for (BackendResource br : brList) {
				if (br.isEnabledForDB() && br.isEnabledForAM()) {
					List<String> ceList = dbClient.getCENameList(br.getName(), false, false);
					for (String ce : ceList) {
						logger.info(ce);
						int fCPU = dbClient.getCEFreeCPU(ce);
						int tCPU = dbClient.getCETotalCPU(ce);
						if (tCPU != 0 && fCPU == tCPU) {
							logger.info("[2.SM] TotalCPU is same the freeCPU. setCEAvailable true : " + ce);
							dbClient.setCEAvailable(ce, true);

						} else {
							int lCPU = dbClient.getCELimitCPU(ce);
							if (fCPU > lCPU) {
								logger.info("[2.SM] The freeCPU is greater than limitcpu. setCEAvailable true : " + ce);
								dbClient.setCEAvailable(ce, true);
							} else {
								int diffTime = dbClient.getCEAvailableTimeDiff(ce);
								int period = resourceAvailablePeriod * 60;
								if (diffTime > period) {
									logger.info("[2.SM] The DiffTime(" + diffTime + ") is greater than resourceAvailablePeriod(" + period + "). setCEAvailable true" + ce);
									dbClient.setCEAvailable(ce, true);
								}

							}
						}
					}
				}
			}
		}

		
//		@Override
		public void run() {

			// int numRunningAgent =
			// dbClient.getNumAliveAgent(runningAgentHeartbeatPeriod);
			// int numRunningAgent = dbClient.getNumValidAgentAll();

//			int numRunningAgent = dbClient.getNumAgent(AGENT_STATUS_RUN);
			int numRunningAgent = 0; 
//			for(Integer si : validSISet){
////				numRunningAgent += dbClient.getNumAgent(AGENT_STATUS_RUN,si);
//				numRunningAgent += dbClient.getNumAgent(AGENT_STATUS_RUN, si) + dbClient.getNumAgent(AGENT_STATUS_SUB, si);
//			}
			int fcpu = 0;
//					getTotalFreeCPU();
 			int dedicatedTotalCPU = numRunningAgent + fcpu;
			int numUsedUserQueue = 0;
			// NUM_CREDIT_AGENT = (int) (dedicatedTotalCPU * THRESHOLD_MIN_AGENT
			// * 0.01f);
			// NUM_MIN_AGENT = (int) (dedicatedTotalCPU * THRESHOLD_MIN_AGENT *
			// 0.01f);
			// int tempThresholdMinAgent = THRESHOLD_MIN_AGENT;
			// logger.info("NUM_MIN_AGENT : " + NUM_MIN_AGENT);
			// float percentOfMaxAgent = 0f;
			// float percentOfMinAgent = 0f;
			// float increaseMetric = 0f;

//			while (true) {

				logger.info("[2.SM]==========2.Status Monitoring Thread:Contoling Total Quantity of Agents==========");
				try {
					switch (AGENT_SCALING_METRIC) {

					case dynamicFairness:
						numUsedUserQueue = 0;

						// numRunningAgent = dbClient.getNumValidAgentAll();
//						numRunningAgent = dbClient.getNumAgent(AGENT_STATUS_RUN);
						
						updateCEInfo();
						fcpu = getTotalFreeCPU();
						
						numRunningAgent = 0;
						for(Integer si : validSISet){
							numRunningAgent += dbClient.getNumAgent(AGENT_STATUS_RUN,si) + dbClient.getNumAgent(AGENT_STATUS_SUB, si);
						}
						dedicatedTotalCPU = numRunningAgent + fcpu;

						logger.info("[2.SM] DynamicFairness - Total Free CPU : " + fcpu);

						for (ObjectName queueName : mbean.getQueues()) {
							QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
							if (queueMbean.getQueueSize() > 0) {
								String userQueue = queueMbean.getName();
								if (!userQueue.equals(metaJobQueue)) {
									for (Integer i : validSISet) {
										String sId = dbClient.getUserInfo(userQueue).getServiceInfraID();
										if (sId.contains(i.toString())) {
											numUsedUserQueue++;
											break;
										}
									}
								}
							}
						}
						logger.info("[2.SM] Num of Existing User : " + numUsedUserQueue);
						if (numUsedUserQueue > 0 && checkUserThread) {
							NUM_CREDIT_AGENT = (int) Math.ceil(((double) dedicatedTotalCPU / numUsedUserQueue));
						} else {
							NUM_CREDIT_AGENT = 0;
						}
						logger.info("[2.SM] ### NUM_CREDIT_AGENT : " + NUM_CREDIT_AGENT);

//						try {
////							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 30 * 1000); // 30
//							Thread.sleep(5000);
//							// secs
//							// =>
//							// 60/////////////////////////////////////////////////////////
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

						checkCEAvailable();

						break;

					case keepQagents:
						numUsedUserQueue = 0;

						numRunningAgent = dbClient.getNumValidAgentAll();
						updateCEInfo();
						fcpu = getTotalFreeCPU();
						logger.info("[2.SM] KeepQAgents - Total Free CPU : " + fcpu);

						long maxQ = 0;
						for (ObjectName queueName : mbean.getQueues()) {
							QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
							long numQ = queueMbean.getQueueSize();
							if (numQ > 0) {
								if (!queueMbean.getName().equals(metaJobQueue))
									if (numQ > maxQ) {
										maxQ = numQ;
									}
								numUsedUserQueue++;
							}
						}

						logger.info("[2.SM] Num of Existing User : " + numUsedUserQueue);
						if (numUsedUserQueue > 0 && checkUserThread) {
							NUM_CREDIT_AGENT = (int) maxQ;
						} else {
							NUM_CREDIT_AGENT = 0;
						}
						logger.info("[2.SM] ### NUM_CREDIT_AGENT : " + NUM_CREDIT_AGENT);

//						try {
//							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 30 * 1000); // 60
//							// secs
//							// =>
//							// 60/////////////////////////////////////////////////////////
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

						break;

					case samplingNagents:

//						try {
//							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 60 * 1000); // 60
//							// secs
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

						break;

					case keepStaticAgents:

						logger.info("[2.SM] KeepStaticAgents. Agent # is " + ADD_AGENT_NO);

						updateCEInfo();

//						try {
//							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 30 * 1000); // 30
//							// secs
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

						break;
					case addNagents:

						updateCEInfo();

						logger.info("[2.SM] AddNAgents. Agent # is " + ADD_AGENT_NO);

//						try {
//							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 30 * 1000); // 30
//							// secs
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

						break;
					case gridScout:

						// updateCEInfo();
						numUsedUserQueue = 0;
						
						for (ObjectName queueName : mbean.getQueues()) {
							QueueViewMBean queueMbean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
							if (queueMbean.getQueueSize() > 0) {
								String userQueue = queueMbean.getName();
								if (!userQueue.equals(metaJobQueue)) {
									for (Integer i=2;i<=3;i++) {
										String sId = dbClient.getUserInfo(userQueue).getServiceInfraID();
										if (sId.contains(i.toString())) {
											numUsedUserQueue++;
											break;
										}
									}
								}
							}
						}
						
						if (numUsedUserQueue > 0 && checkUserThread) {
							numRunningAgent = 0;
							fcpu = dbClient.getCEFreeCPUTotal(2) + dbClient.getCEFreeCPUTotal(3);
							for(int si=2;si <= 3 ; si++){
								numRunningAgent += dbClient.getNumAgent(AGENT_STATUS_RUN,si) + dbClient.getNumAgent(AGENT_STATUS_SUB, si);
							}
							
							
							
							NUM_CREDIT_AGENT = fcpu + numRunningAgent;
						}else{
							NUM_CREDIT_AGENT = 0;
						}
						

						logger.info("[2.SM] GridScout. Agent # is " + NUM_CREDIT_AGENT);
//						try {
//							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 30 * 1000); // 30
//							// secs
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}

						break;
					default:

//						try {
//							Thread.sleep(statusMonitoringHeartbeatPeriod * 1 * 60 * 1000); // 60
//							// secs
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
						checkCEAvailable();
					}

				} catch (Exception e) {
					logger.error(e.toString());
					e.printStackTrace();
					logger.error("[2.SM] Status Manager Error : Sleeping " + THREADWAITTIME + "s to repeat StatusManager...");
//					try {
//						Thread.sleep(THREADWAITTIME);
//					} catch (InterruptedException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
				}

//			}

		}

	}

}
