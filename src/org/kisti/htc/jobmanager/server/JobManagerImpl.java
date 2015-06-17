package org.kisti.htc.jobmanager.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
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
import java.util.UUID;

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
import javax.xml.bind.JAXB;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.ggf.schemas.jsdl._2005._11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl._2005._11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl._2005._11.jsdl.ObjectFactory;
import org.ggf.schemas.jsdl._2005._11.jsdl_posix.ArgumentType;
import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.MetaJob;
import org.kisti.htc.dbmanager.beans.ServiceInfra;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.message.DTO;
import org.kisti.htc.message.MessageCommander;
import org.kisti.htc.message.MessageSender;
import org.kisti.htc.message.MetaDTO;
import org.kisti.htc.message.MetaDirectConsumer;
import org.kisti.htc.message.MetaMessageSender;
import org.kisti.htc.udmanager.client.UDClient;
import org.ogf.schemas.jsdl._2009._03.sweep.Assignment;
import org.ogf.schemas.jsdl._2009._03.sweep.DirectoryType;
import org.ogf.schemas.jsdl._2009._03.sweep.DocumentNodeType;
import org.ogf.schemas.jsdl._2009._03.sweep.LoopIntegerType;
import org.ogf.schemas.jsdl._2009._03.sweep.SweepType;
import org.ogf.schemas.jsdl._2009._03.sweep.ValuesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobManagerImpl implements JobManager {
	/*
	 * The LoggerFactory is a utility class producing Loggers for various
	 * logging APIs, most notably for log4j, logback and JDK 1.4 logging
	 */

	private static final Logger logger = LoggerFactory.getLogger(JobManagerImpl.class);
	// private final static mLogger logger = mLoggerFactory.getLogger("JM");

	private String DBManagerURL; // DBManager Address: URL of DBManager which
									// provides the Web Services
	private Database dbClient; // DBManager client
	// private String DBAddress; // Database Address: The address of MySQL
	// Database
	// itself

	private static String host; // JobManager Server HostName

	private MetaMessageSender metaMessageSender; // MetaJob MessageSender
	private static String FTPAddress; // UserDataManager FTP Address
	// private String JMXServiceURL;
	// private String JMXObjectName;
	// BrokerViewMBean mbean;
	// MBeanServerConnection connection;
	// private QueueViewMBean iqueue = null;
	private String metaJobQueue = null; // MetaJob Queue Name: Maybe this
										// variable
										// is deprecated?
	private UDClient udc; // UserDataManager Client

	/* SSL Configuration Fields */
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;
	private volatile static Thread sThread;

	boolean tempFileDeletion = true;

	// boolean tempFileDeletion = false;

	/* Constructor */
	// JobManagerImpl::JobManagerImpl
	public JobManagerImpl() {

		// set_logger_prefix("[JobMgrImpl] "); // debug message prefix

		try {
			/*
			 * The Properties class represents a persistent set of properties.
			 * The Properties can be saved to a stream or loaded from a stream.
			 * Each key and its corresponding value in the property list is a
			 * string.
			 */
			Properties prop = new Properties();
			/* Loading HTCaaS_Server.Conf File */
			prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));

			/*
			 * Reading in the URL of the DBManager in the configuration file
			 * e.g., DBManager.Address=http://150.183.158.172:9000/Database
			 */
			DBManagerURL = prop.getProperty("DBManager.Address");

			/*
			 * Setting up the SSL authentication mechanism for the communication
			 * through https web services
			 */
			if (prop.getProperty("SSL.Authentication").equals("true")) {
				SSL = true;
				DBManagerURL = DBManagerURL.replace("http", "https");
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}

			/* Logging the DBManager URL */
			logger.info("DBManagerURL: " + DBManagerURL);

			/*
			 * Reading in the FTPAddress of the UserDataManager and log that
			 * information. e.g., FTP.Address=150.183.158.172
			 */
			FTPAddress = prop.getProperty("FTP.Address");
			logger.info("FTP.Address : " + FTPAddress);

			// /*
			// * Reading in the DBAddress for dumping some of the contents in
			// the MySQL
			// * Database e.g., htcaas_db.url =
			// * jdbc:mysql://amga.kisti.re.kr/htcaas_server
			//
			// DBAddress = prop.getProperty("htcaas_db.url");*/

			/*
			 * Reading in the name of MetaJob Queue e.g.,
			 * ActiveMQ.MetaJobQueue=MetaJobQueue.ActiveMQ
			 */
			metaJobQueue = prop.getProperty("ActiveMQ.MetaJobQueue");
			logger.info("metaJobQueue : " + metaJobQueue);

			// JMXServiceURL =
			// prop.getProperty("ActiveMQ.Broker.JMXServiceURL");
			// logger.info("JMXServiceURL: " + JMXServiceURL);
			//
			// JMXObjectName = prop.getProperty("ActiveMQ.Broker.ObjectName");
			// logger.info("JMXObjectName: " + JMXObjectName);

		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		try {
			host = InetAddress.getLocalHost().getHostName(); // Get Server local
																// hostname
			logger.info("host : " + host);
		} catch (UnknownHostException e) {
			host = "UnknownHost";
		}

		/*
		 * Configure DBManager Client. ClientProxyFactoryBean creates a Java
		 * proxy for you from your interface which will invoke the service
		 * (Apache CXF). Simple Frontend (map Java APIs to and from WSDL models
		 * for web services) + Aegis databinding (map Java objects to XML
		 * documents described by XML schema, and vica-versa)
		 */
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Database.class);
		factory.setAddress(DBManagerURL);
		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		dbClient = (Database) factory.create();

		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(dbClient).getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(600000);
		httpClientPolicy.setReceiveTimeout(0);
		httpConduit.setClient(httpClientPolicy);

		/* Setup DBManager Client SSL */
		if (SSL) {
			try {
				setupTLS(dbClient);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/*
		 * UDManager Client: creates a proxy that can invoke the UserDataManager
		 * web services through Apache CXF and assign it to the public member
		 * variable udclient
		 */
		logger.info("make a userdata client object");
		udc = new UDClient();

		/* Create MetaMessageSender Instance */
		logger.info("make a meta message sender object");
		metaMessageSender = new MetaMessageSender();

		logger.info("meta message sender set Verbose true");
		metaMessageSender.setVerbose(false);

		runSplitter();

		/*
		 * // ActivqMQ Broker Statistics try { JMXServiceURL url = new
		 * JMXServiceURL(JMXServiceURL); JMXConnector connector =
		 * JMXConnectorFactory.connect(url); connector.connect(); connection =
		 * connector.getMBeanServerConnection(); // MBeanServerConnection //
		 * connection; ObjectName name = new ObjectName(JMXObjectName); //
		 * import // javax.management.ObjectName; mbean =
		 * MBeanServerInvocationHandler.newProxyInstance(connection, name,
		 * BrokerViewMBean.class, true);
		 * 
		 * logger.info("ActiveMQ broker id=" + mbean.getBrokerId());
		 * logger.info("ActiveMQ broker name=" + mbean.getBrokerName()); for
		 * (ObjectName queueName : mbean.getQueues()) { QueueViewMBean
		 * queueMbean =
		 * MBeanServerInvocationHandler.newProxyInstance(connection, queueName,
		 * QueueViewMBean.class, true); if
		 * (queueMbean.getName().equals(metaJobQueue)) { iqueue = queueMbean;
		 * break; } } if (iqueue == null) {
		 * logger.error("ActiveMQ Queue does not exist"); throw new
		 * Exception("Queue not exist: " + metaJobQueue); }
		 * 
		 * } catch (Exception e) { logger.error("Cannot access ActiveMQ Broker"
		 * + e.getMessage()); }
		 */

	}// The end of JobManagerImpl Constructor

	// create splitter thread and run splitter
	public void runSplitter() {
		if (sThread == null) {
			synchronized (JobManagerImpl.class) {
				if (sThread == null) {
					sThread = new Splitter();
					sThread.setDaemon(true);
					sThread.start();
				}
			}
		}
	}

	/* Configure CXF SSL setup */
	// JobManagerImpl::setupTLS
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

	}// The end of setupTLS function

	// JobManagerImpl::getTrustManagers
	private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
		fac.init(trustStore);
		return fac.getTrustManagers();
	}

	// JobManagerImpl::getKeyManagers
	private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
		String alg = KeyManagerFactory.getDefaultAlgorithm();
		char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
		KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
		fac.init(keyStore, keyPass);
		return fac.getKeyManagers();
	}

	// for database manager calling
	// dbClient 객체를 이용하여 Database 서비스를 호출함
	// 사용법
	// 리턴변수 = (리턴타입)call_dbmgr("서비스메서드", arg1, arg2, arg3 .... );
	// call_dbmgr("서비스메서드", arg1, arg2, arg3 .... );
	public Object call_dbmgr(String name, Object... args) {

		Method method = null;
		Object ret = null;
		Class S = String.class;
		Class I = Integer.class;
		Class C = dbClient.getClass();

		logger.debug("calling service -----> Database :: " + name);
		try {

			if (false)
				;
			else if (name.equals("insertMetaJob"))
				method = C.getMethod("insertMetaJob", S, S, S, I, S, S);

			else if (name.equals("setMetaJobStatus"))
				method = C.getMethod(name, I, S);
			else if (name.equals("getMetaJobStatus"))
				method = C.getMethod(name, I);

			else if (name.equals("setJobCancel"))
				method = C.getMethod(name, I);
			else if (name.equals("setJobName"))
				method = C.getMethod(name, I, S);
			else if (name.equals("setJobDetail"))
				method = C.getMethod(name, I, S);

			else if (name.equals("reportSubmitError"))
				method = C.getMethod(name, I, I, S, S, S);

			else if (name.equals("setMetaJobError"))
				method = C.getMethod(name, I, S);
			else if (name.equals("setMetaJobStatus"))
				method = C.getMethod(name, I, S);
			else if (name.equals("getMetaJobUserId"))
				method = C.getMethod(name, I);

			else if (name.equals("removeResults"))
				method = C.getMethod(name, I);

			else if (name.equals("addJob"))
				method = C.getMethod(name, I, I);

			else if (name.equals("removeJobs"))
				method = C.getMethod(name, I);
			else if (name.equals("removeMetaJob"))
				method = C.getMethod(name, I);

			else {
				logger.error("Invalid method call");
				System.exit(1);
			}

		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}

		// _debug("method : " + method );

		try {
			ret = method.invoke(dbClient, args);
		} catch (Exception e) {
		}
		return ret;
	}

	// JobManagerImpl::main
	public static void main(String arg[]) {
		JobManagerImpl jm = new JobManagerImpl();

		System.out.println("host name:" + host);
		String filename = "jsdl/job-1.xml";
		// String filename = "jsdl/autodock_vina.jsdl";
		// String filename = "jsdl/threekaonomega2.jsdl";
		// String filename = "jsdl/test.jsdl";
		// String filename = "jsdl/hello.jsdl";
		// String filename = "jsdl/bioknowledgeviewer.jsdl";

		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (FileNotFoundException e1) {
			logger.error(e1.toString());
			e1.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			logger.error(e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		String user = "seungwoo";

		jm.submitMetaJob(user, sb.toString(), 0, "", "");

		// Set<Integer> ss = new HashSet<Integer>();
		// ss.add(1);

		// int a = jm.resubmitSubJobSet(user, 724, ss);

		// System.out.println(a);

	}

	@Override
	public Map<Integer, String> submitMetaJob(String userId, String metaJobDocument, int aMaxJobTimeMin, String pName, String sName) {
		return submitMetaJob(userId, metaJobDocument, aMaxJobTimeMin, pName, sName, "");
	}

	/*
	 * @param userid 사용자 아이디
	 * 
	 * @param metaJobDocument 메타작업 JSDL 문서
	 * 
	 * @return (resultCode, Comment), resultCode: Integer 0-fail, 1-success,
	 * Comment: 1-metajobID, 0-message
	 */

	// JobManagerImpl::submitMetaJob
	@Override
	public Map<Integer, String> submitMetaJob(String userId, String metaJobDocument, int aMaxJobTimeMin, String pName, String sName, String ceNames) {
		logger.info("submitMetaJob " + userId + " A Estimated Max JobTime(m):" + aMaxJobTimeMin);

		String mdstr;

		logger.info("========= submit a new job =============");
		logger.info("submitMetaJob userId:" + userId + " A Estimated Max JobTime(m):" + aMaxJobTimeMin);
		logger.info("pName :" + pName + " sName :" + sName);

		// JSDL 에 #USER# 라고 쓰면 사용자 아이디로 변환
		mdstr = metaJobDocument.replaceAll("#USER#", userId);
		logger.info(mdstr);

		int metaJobID = -1;

		// write metaJob jsdl string to a temp file
		Map<Integer, String> resultMap = new HashMap<Integer, String>(1);
		File jsdl = new File("tmp/" + UUID.randomUUID() + ".jsdl");
		try {
			PrintStream ps = new PrintStream(jsdl);
			ps.println(mdstr);
			ps.close();
		} catch (Exception e) {
			logger.error("Failed to write JSDL: " + e.getMessage());
			e.printStackTrace();
		}

		// xml unmarshal using JAXB.unmarshal
		JobDefinitionType jdef = JAXB.unmarshal(jsdl, JobDefinitionType.class);

		if (tempFileDeletion)
			jsdl.delete(); // temp jsdl deletion

		// get the application name from jsdl
		String appName = jdef.getJobDescription().getApplication().getApplicationName();
		logger.info("appName : " + appName);

		/*
		 * Insert metajob information into database table `metajob` with
		 * additional parameters aMaxJobTimeMin, pName and sName. pName
		 * corresponds to the Project Name, and sName is the Script Name which
		 * will be provided by the GUI client (pure meta information). This is
		 * the only different part of codes from submitMetaJob(String userId,
		 * String metaJobDocument) function
		 */

		int appId = dbClient.getApplicationId(appName);

		logger.info("APP ID :" + appId);

		if (appId < 1) {
			appName = "general";
			appId = 3;
		}
		
		String tmp = null;
		if ((ceNames != null) && !ceNames.isEmpty()) {

			/*
			 * Compare the selected ces and user serviceInfra then, if it is
			 * wrong, the metajob is canceled.
			 */
			String[] ces = ceNames.split(",");
			Set<Integer> siSetFromMetaJob = new HashSet<Integer>();
			for (String ceName : ces) {
				siSetFromMetaJob.add(dbClient.getCEObject(ceName).getServiceInfraId());
			}

			for (Integer si : siSetFromMetaJob) {
				List<ServiceInfra> siFromUser = dbClient.getUserServiceInfra(userId);
				boolean check = false;
				for (ServiceInfra serviceInfra : siFromUser) {
					if ((serviceInfra.getId() == si)) {
						check = true;
						break;
					}
				}

				if (!check) {
					logger.error("Not matched serviceinfra from metajob and user :" + userId + ", " + si + "metajob is being canceled");
					// cancelMetaJob(userId, metaJobID);
					// dbClient.setMetaJobError(metaJobID,
					// "Not matched serviceinfra");
					resultMap.put(0, "Not matched serviceinfra : " + si + ", check the ce name!");
					return resultMap;
				}
			}
			// Compare end

			Set<Integer> ceset = new HashSet<Integer>();

			for (String name : ces) {
				CE ce = dbClient.getCEObject(name);
				ceset.add(ce.getId());
			}
			tmp = ceset.toString();
		}
		metaJobID = dbClient.insertMetaJob(userId, appName, mdstr, aMaxJobTimeMin, pName, sName, tmp != null ? tmp.substring(1, tmp.length() - 1) : "");

		logger.info("MetaJob # :" + metaJobID);

		if (metaJobID < 0) {
			logger.error("Job submisstion(insertMetaJob) fail :  " + metaJobID);
			resultMap.put(0, "failed to insert MetaJob. Contact the htcaas adminstrator!");
			return resultMap;
		}

		resultMap.put(1, Integer.toString(metaJobID));

		/*
		 * Insert the metajob message into the MetaJobQueue
		 */
		/* Create a message to be inserted into the MetaJobQueue */
		MetaDTO mdto = new MetaDTO();
		mdto.setMetaJobId(metaJobID);
		mdto.setUserId(userId);
		// mdto.setNumSubJob(numSubJob);
		mdto.setApp(appName);

		// Insert the message into the MetaJobQueue
		try {
			metaMessageSender.sendMessage(mdto);
		} catch (Exception e) {
			logger.error(e.toString());
		}

		return resultMap;
	}

	// class Splitter {{{
	private class Splitter extends Thread {

		int metaJobId;
		String metaJobDocument; // JSDL document
		String userId;
		String appName; // Application Name
		int numSubJob = 1;
		MessageSender messageSender; // SubJob MessageSender
		String ligandFilter = null;
		String targetFilter = null;
		boolean SUBDIR_LIGAND = false;
		boolean SUBDIR_TARGET = false;
		private MetaDirectConsumer metaConsumer = null;
		private MetaDTO mDTO;

		// JobManagerImpl::Splitter::Splitter
		private Splitter(int metaJobId, String metaJobDocument, String userId, String appName) {
			this.metaJobId = metaJobId;
			this.metaJobDocument = metaJobDocument;
			this.userId = userId;
			this.appName = appName;

			/* Create SubMessageSender Instance */
			logger.info("make a message sender object");
			messageSender = new MessageSender();

			logger.info("message sender set Verbose true");
			messageSender.setVerbose(false);

			// change destination queue(userid)
			messageSender.changeDestiQueue(userId);
			metaConsumer = new MetaDirectConsumer(metaJobQueue);

		}

		private Splitter() {

			/* Create SubMessageSender Instance */
			logger.info("make a message sender object");
			messageSender = new MessageSender();

			logger.info("message sender set Verbose true");
			messageSender.setVerbose(false);

			metaConsumer = new MetaDirectConsumer(metaJobQueue);
		}

		// JobManagerImpl::Splitter::run
		@Override
		public void run() {

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			logger.info("+ JobSplitter started");

			int time = 1;
			while (true) {
				try {
					logger.info("Getting Metajob mesagge");
					mDTO = metaConsumer.getMessage();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.info("| No jobs in InputQueue,  Current Queue WaitingTime : " + time + " s");
					try {
						Thread.sleep(1000 * time);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					if (time < 32) {
						time = time * 2;
					} else {
						// break;
					}
					continue;
				}

				if (mDTO != null) {
					time = 1;
					metaJobId = mDTO.getMetaJobId();
					metaJobDocument = dbClient.getMetaJobJSDL(metaJobId);
					userId = mDTO.getUserId();
					appName = mDTO.getApp();

					// change destination queue(userid)
					messageSender.changeDestiQueue(userId);

					// Change the status of the metajob into splitting
					dbClient.setMetaJobStatus(metaJobId, Constant.METAJOB_STATUS_SPLITTING);

					try {
						/*
						 * Call the processJSDL function : If there is no
						 * parameter sweep is defined in the metaJobDocument, a
						 * single task will be submitted. : If there are defined
						 * parameter sweeps, multiple tasks of the metaJob will
						 * be submitted.
						 */
						// call : run --> processJSDL
						boolean success = processJSDL(this.metaJobId, this.metaJobDocument, this.userId);

						if (!success) {
							// TODO: Following logging message seems to be
							// incorrect
							// ! -
							// JSK

							boolean ret = dbClient.getMetaJobStatus(metaJobId).equals(Constant.METAJOB_STATUS_SPLITTING);
							logger.info("getMetaJobStatus ret : " + metaJobId + " " + ret);
							if (ret) {
								logger.info("setMetaJobStatus " + metaJobId + " " + Constant.METAJOB_STATUS_SPLITFAIL); // split-failed
								dbClient.setMetaJobStatus(this.metaJobId, Constant.METAJOB_STATUS_SPLITFAIL);
							} else {
								logger.info("setMetaJobStatus " + metaJobId + " " + Constant.METAJOB_STATUS_FAIL);
								dbClient.setMetaJobStatus(this.metaJobId, Constant.METAJOB_STATUS_FAIL);
							}
							logger.error("Failed to process JSDL. Check JSDL file & server log");
						} else {
							logger.info("setMetaJobStatus " + metaJobId + " " + Constant.METAJOB_STATUS_SPLIT);
							dbClient.setMetaJobStatus(metaJobId, Constant.METAJOB_STATUS_SPLIT);
						}

					} catch (Exception e) {
						logger.error("SubmitMetaJob Exception : " + e);
						e.printStackTrace();

						String status = dbClient.getMetaJobStatus(metaJobId);
						logger.info("getMetaJobStatus : " + metaJobId + ", " + status);

						if (status.equals(Constant.METAJOB_STATUS_CANCEL)) {

						} else if (status.equals(Constant.METAJOB_STATUS_SPLITTING)) {

							logger.info("setMetaJobStatus " + Constant.METAJOB_STATUS_SPLITFAIL + ", " + metaJobId);
							dbClient.setMetaJobStatus(this.metaJobId, Constant.METAJOB_STATUS_SPLITFAIL);

						} else if (status.equals(Constant.METAJOB_STATUS_SPLIT)) {
							logger.info("setMetaJobStatus " + Constant.METAJOB_STATUS_FAIL + ", " + metaJobId);
							dbClient.setMetaJobStatus(this.metaJobId, Constant.METAJOB_STATUS_FAIL);
						}

						logger.info("setJobCancel : " + metaJobId);
						dbClient.setJobCancel(this.metaJobId);

						/*
						 * Remove ActiveMQ messages in the user queue (with the
						 * given userId): This will result in deleting all
						 * subjobs (tasks) of the given MetaJob (with metaJobId)
						 * from the user queue.
						 */
						MessageCommander mc = new MessageCommander();
						int num = mc.removeMessage(this.metaJobId, this.userId);
						logger.error(num + " Subjobs are canceld");

					}finally{
						numSubJob = 1;
					}
				}

			}

			// logger.info("+ JobSplitter finished");
		}

		// JobManagerImpl::Splitter::top
		private boolean stop(int metaJobId, String userId) {

			logger.info("+ JobSplitter canceled");

			currentThread().interrupt();

			return true;
		}

		// Separate and process JSDL Sweep part
		// JobManagerImpl::Splitter::processJSDL
		private boolean processJSDL(int metaJobID, String metaJobDocument, String userId) throws Exception {
			logger.info("+ Process JSDL : " + metaJobID);

			File jsdl = null;
			String jsdlFile = null;
			File jsdlFileNoSweep = null;

			try {

				boolean result = true;
				/*
				 * MetaJob JSDL script without Sweep functions which is
				 * basically the core description of the MetaJob. It can also be
				 * used for the base JSDL document where the parameter sweeps
				 * are applied to (especially in the processSweep function).
				 */
				jsdlFile = UUID.randomUUID().toString(); // Create temporary
															// JSDL file name
				String path = "tmp/" + jsdlFile + ".jsdl";
				logger.debug("create temporary JSDL file :" + path);
				jsdl = new File("tmp/" + jsdlFile + ".jsdl"); // Create
																// temporary
																// JSDL file

				// write metaJob jsdl string to a temp file
				try {
					PrintStream ps = new PrintStream(jsdl);
					ps.println(metaJobDocument);
					ps.close();
				} catch (Exception e) {
					logger.error("Failed to write JSDL: " + e.getMessage());
					e.printStackTrace();
				}

				// XML unmarshal using JAXB.unmarshal
				JobDefinitionType jdef = JAXB.unmarshal(jsdl, JobDefinitionType.class);

				/* In case without parameter sweeps */
				if (jdef.getSweep() == null) {
					logger.debug("jsdl includes no parameter sweep. it's a simple job");

					logger.info("------------------1-------------------");
					// There is only a single task to be submitted
					logger.debug("submitSubJob just a single job");
					submitSubJob(metaJobDocument, metaJobID, userId, 1);


					/* In case with parameter sweeps are defined in the JSDL */
				} else {
					logger.debug("jsdl includes parameter sweep");

					/* Create a JSDL script file without parameter sweeps */
					JobDefinitionType jdef_copy = JAXB.unmarshal(jsdl, JobDefinitionType.class);
					jdef_copy.setSweep(null);

					/*
					 * Create a JSDL script file using the JAXB.marshal: writes
					 * a Java object tree to XML and store it to the specified
					 * location. Syntax: public static void marshal(Object
					 * jaxbObject, File xml) - jaxbObject: The Java object to be
					 * marshalled into XML. - xml: XML will be written to this
					 * file. If it already exists, it will be overwritten.
					 */
					JAXB.marshal(new ObjectFactory().createJobDefinition(jdef_copy), new File("tmp/" + jsdlFile + ".nosweep.jsdl"));

					/*
					 * Read in the newly generated JSDL file without parameter
					 * sweeps and convert it to the string that can be delivered
					 * to the processSweep function.
					 */
					StringBuffer sb = new StringBuffer();
					jsdlFileNoSweep = new File("tmp/" + jsdlFile + ".nosweep.jsdl");
					try {
						BufferedReader br = new BufferedReader(new FileReader(jsdlFileNoSweep));
						String line = "";
						while ((line = br.readLine()) != null) {
							sb.append(line + "\n");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					String metaJobDocumentNoSweep = null;
					metaJobDocumentNoSweep = sb.toString();

					/*
					 * Call the processSweep function that will actually submit
					 * tasks of a metajob. It first processes the parameter
					 * sweeps and generate match & value pairs and according to
					 * the pairs, it will generate multiple JSDL scripts to be
					 * submitted as tasks. During the generation of JSDL
					 * scripts, metaJobDocumentNoSweep is used as a base JSDL
					 * document where substitutions of match & value pair are
					 * applied to.
					 */

					// call : processJSDL --> processSweep
					result = processSweep(metaJobDocumentNoSweep, jdef.getSweep(), userId, metaJobID, 0, jsdlFileNoSweep);
				}

				return result;

			} finally {
				/* delete temporary files */
				if (tempFileDeletion) {
					if (jsdl != null) {
						jsdl.delete();
					}
					if (jsdlFileNoSweep != null) {
						jsdlFileNoSweep.delete();
					}
				}
			}

		}// The end of processJSDL function

		/* submit subjob */
		// JobManagerImpl::Splitter::submitSubJob
		private void submitSubJob(String mjdoc, int metaJobID, String userId, int jobSeq) throws Exception {
			logger.debug("+ submitSubJob metajob " + metaJobID + ", user " + userId + ", jobseq " + jobSeq);

			/*
			 * If the status of the metajob is "canceled" (via for example,
			 * cancelMetaJob function), then stop the current running thread.
			 * Execution pipelines : thread Splitter --> processJSDL -->
			 * submitSubJob (without parameter sweeps) : thread Splitter -->
			 * processJSDL --> processSweep --> submitSubJob (with parameter
			 * sweeps)
			 */

			logger.info("------------------3-------------------");
			boolean ret = dbClient.getMetaJobStatus(metaJobID).equals(Constant.METAJOB_STATUS_CANCEL);
			logger.debug("getMetaJobStatus return " + ret);
			logger.info("------------------4-------------------");

			if (ret) {
				stop(metaJobID, userId);
				// To make sure that current thread executing this submitSubJob
				// function
				// is "interrupted".
				Thread.sleep(100);
			}

			/*
			 * Create a temporary JSDL file from mjdoc (corresponds a task)
			 * which will be used for JAXB.unmarshal
			 */
			String path = "tmp/" + UUID.randomUUID() + "subjob.jsdl";
			logger.debug("create temporary file : " + path);

			JobDefinitionType jdef = null;
			File subjobJSDL = null;

			try {
				subjobJSDL = new File("tmp/" + UUID.randomUUID() + "subjob.jsdl");
				PrintStream ps = new PrintStream(subjobJSDL);
				ps.println(mjdoc);
				ps.close();
				jdef = JAXB.unmarshal(subjobJSDL, JobDefinitionType.class);

			} catch (Exception e) {
				logger.error("Failed to write JSDL: " + e.getMessage());
				e.printStackTrace();

			} finally {
				if (tempFileDeletion)
					subjobJSDL.delete(); // delete temporary subjob file
			}

			int jobID = dbClient.addJob(metaJobID, jobSeq); // add a job in DB
															// and get the job
															// id

			// parsing the JSDL object

			// application name
			String appName = jdef.getJobDescription().getApplication().getApplicationName();
			if(appName == null || appName.isEmpty()){
				appName = "general";
			}
			// executable object
			logger.info(jdef.getJobDescription() + "");
			logger.info("------------------51-------------------");
			logger.info(jdef.getJobDescription().getApplication() + "");
			logger.info("------------------52-------------------");
			logger.info(jdef.getJobDescription().getApplication().getPOSIXApplication() + "");

			logger.info("------------------53-------------------");
			logger.info(jdef.getJobDescription().getApplication().getPOSIXApplication().getExecutable() + "");
			logger.info("------------------54-------------------");
			String executable = jdef.getJobDescription().getApplication().getPOSIXApplication().getExecutable().getValue();

			logger.info("------------------6-------------------");
			// arguments
			List<String> arguments = new ArrayList<String>();
			List<ArgumentType> args = jdef.getJobDescription().getApplication().getPOSIXApplication().getArgument();
			for (ArgumentType arg : args) {
				arguments.add(arg.getValue());
			}

			// input files
			List<String> inputFiles = new ArrayList<String>();
			logger.info("------------------7-------------------");

			// output files
			List<String> outputFiles = new ArrayList<String>();

			// staging files
			List<DataStagingType> dataList = jdef.getJobDescription().getDataStaging();
			int nameTag = 0;
			for (DataStagingType data : dataList) {
				// Data staging type is source: This is input data
				if (data.getSource() != null) {
					logger.debug("source: " + data.getSource().getURI());
					inputFiles.add(data.getSource().getURI());

					// The name of this job (task) becomes the filename of the
					// first input
					// data
					if (nameTag == 0) {
						String name = data.getSource().getURI();
						dbClient.setJobName(jobID, name.subSequence(name.lastIndexOf("/") + 1, name.length()).toString());
					}
				}
				// Data staging type is target: This is output data
				else if (data.getTarget() != null) {
					logger.debug("target: " + data.getTarget().getURI());
					outputFiles.add(data.getTarget().getURI());

					// The name of this job (task) becomes the filename of the
					// first input
					// data
					if (nameTag == 0) {
						String name = data.getTarget().getURI();
						dbClient.setJobName(jobID, name.subSequence(name.lastIndexOf("/") + 1, name.length()).toString());
					}
				}

				nameTag++;
			}
			logger.info("------------------8-------------------");

			/* Create a message to be sent for submitting this job(task) */
			DTO dto = new DTO();
			dto.setMetaJobId(metaJobID);
			dto.setJobId(jobID);
			dto.setUserId(userId);
			dto.setAppName(appName);
			dto.setExecutable(executable);
			dto.setArguments(arguments);
			dto.setInputFiles(inputFiles);
			dto.setOutputFiles(outputFiles);

			// update job detail in DB
			dbClient.setJobDetail(jobID, dto.toString());

			// Insert this job (task) into the message queue
			try {
				messageSender.sendMessage(dto);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}// The end of submitSubJob function

		/*
		 * processSweep function to process match & value pairs in the parameter
		 * sweep definitions
		 */
		// JobManagerImpl::Splitter::processSweep
		private boolean processSweep(String mjdoc, SweepType sweep, String userId, int metaJobID, int level, File jsdlFileNoSweep) throws Exception {

			boolean result = true;
			// Extract the list of assignments specified in the JSDL
			List<Assignment> assignments = sweep.getAssignment();

			/*
			 * Create AssignmentInfoList: A list of AssignmentInfo class An
			 * AssignmentInfo corresponds to an assignment in the parameter
			 * sweeps, e.g., Values, Directory, LoopInteger
			 */
			AssignmentInfoList aiList = new AssignmentInfoList();

			/*
			 * Example Parameter Sweeps (autodock3): <ns3:Sweep> // First-level
			 * sweep function <ns3:Assignment> <ns3:DocumentNode>
			 * <ns3:Match>TARGET</ns3:Match> </ns3:DocumentNode> <ns3:Values>
			 * <ns3:Value xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			 * xmlns:xs="http://www.w3.org/2001/XMLSchema"
			 * xsi:type="xs:string">2QMJ_new</ns3:Value> </ns3:Values>
			 * </ns3:Assignment> <ns3:Sweep> // Second-level sweep function
			 * <ns3:Assignment> <ns3:DocumentNode> <ns3:Match>LIGAND</ns3:Match>
			 * </ns3:DocumentNode> <ns3:Directory filenameonly="true">
			 * <ns3:Name>/usr/local/ligands/pdb.5</ns3:Name> </ns3:Directory>
			 * </ns3:Assignment> </ns3:Sweep> </ns3:Sweep>
			 */

			/*
			 * Parse the sweeps defined in the JSDL and create a list of
			 * AssignmentInfo
			 */
			for (Assignment assignment : assignments) {
				AssignmentInfo as = new AssignmentInfo();

				String match = null;

				// DocumentNodes (parameters)
				for (int i = 0; i < assignment.getParameter().size(); i++) {
					DocumentNodeType doc = (DocumentNodeType) assignment.getParameter().get(i).getValue();
					match = doc.getMatch();
					// Adding the Match in the assignment, e.g., TARGET, LIGAND
					as.addMatch(match);
				}

				if (match != null && match.equals("LIGAND")) {

					JobDefinitionType jdef = JAXB.unmarshal(jsdlFileNoSweep, JobDefinitionType.class);

					List<DataStagingType> dataList = jdef.getJobDescription().getDataStaging();

					for (DataStagingType data : dataList) {
						// Data staging type is source: This is input data
						if (data.getSource() != null) {
							String uri = data.getSource().getURI();
							if (uri.contains("LIGAND")) {
								String[] tmp = uri.split("\\.");
								ligandFilter = tmp[tmp.length - 1];

								if (uri.contains("SUBDIR")) {
									SUBDIR_LIGAND = true;
								}
							} else if (uri.contains("TARGET")) {
								String[] tmp = uri.split("\\.");
								targetFilter = tmp[tmp.length - 1];

								if (uri.contains("SUBDIR")) {
									SUBDIR_TARGET = true;
								}
							}
						}
					}
				}

				/* Sweep type is Values */
				String functionType = assignment.getFunction().getName().getLocalPart();
				if (functionType.equals("Values")) {
					ValuesType values = (ValuesType) assignment.getFunction().getValue();
					for (Object value : values.getValue()) {
						// Adding the value in the assignment, e.g., 2QMJ_new
						as.addValue((String) value);
					}
				}

				/*
				 * Sweep type is Directory: Directory allows us to get the list
				 * of files in the HTCaaS Server and use them for parameter
				 * sweeps. e.g., using the ligand input files for iterating the
				 * protein docking process in the autodock.
				 */
				else if (functionType.equals("Directory")) {
					// Get the name of directory whose files will be used for
					// sweep, e.g.,
					// /usr/local/ligands/pdb.5
					DirectoryType dir = (DirectoryType) assignment.getFunction().getValue();
					/*
					 * Connect to the UserDataManager (in the HTCaaS Server) to
					 * get the list of files specified in the "Directory" sweep.
					 * If connection fails for some reasons, errors will be
					 * recorded throughout the DBManager.
					 */
					FTPFile[] files = null; // files will contain the names of
											// files in
											// the directory
					UUID uid = null;
					try {
						uid = udc.udclient.login(FTPAddress, userId, dbClient.getUserPasswd(userId), 0);
						// Originally, files will contain the full path names of
						// the files
						// in the directory
						if (ligandFilter != null && match.equals("LIGAND")) {
							if (SUBDIR_LIGAND) {
								logger.info("Getting folder list");
								files = udc.udclient.getFolderList(uid, dir.getName());
							} else {
								logger.info("Getting file list with LigandFilter");
								files = udc.udclient.getFileListFilter(uid, dir.getName(), ligandFilter);
							}

						} else if (targetFilter != null && match.equals("TARGET")) {
							if (SUBDIR_TARGET) {
								logger.info("Getting folder list");
								files = udc.udclient.getFolderList(uid, dir.getName());
							} else {
								logger.info("Getting file list with TargetFilter");
								files = udc.udclient.getFileListFilter(uid, dir.getName(), targetFilter);
							}
						} else {
							logger.info("Getting All list");
							files = udc.udclient.getAllList(uid, dir.getName());
						}

					} catch (Exception e) {
						logger.error("Failed to get file list(" + dir.getName() + "). Check the directory,your id and passwd");
						e.printStackTrace();
						dbClient.reportSubmitError(-1, metaJobID, null, null, "Failed to get file list(" + dir.getName() + "). Check the directory,your id and passwd)!");
						dbClient.setMetaJobError(metaJobID, "Failed to get file list");

						return false;

					} finally {
						if (uid != null) {
							udc.udclient.logout(uid, 0);
						}
					}

					if (files != null) {
						logger.info("dir.getName() :" + dir.getName());
						logger.info("files size:" + files.length);

						boolean fileNameOnly = dir.isFilenameonly();
						for (FTPFile file : files) {
							/*
							 * We will use only the file names (not including
							 * the path) as parameter sweeps, e.g., ligand names
							 * only. This option can be specified in the JSDL
							 * script, e.g., <ns3:Directory filenameonly="true">
							 */

							if (file.isFile()) {
								if (fileNameOnly) {
									as.addValue(file.getName().substring(0, file.getName().lastIndexOf('.')));
									// as.addValue(file.getName().split("\\.")[0]);
								} else {
									as.addValue(file.getName());
								}
							} else if (file.isDirectory()) {

								FTPFile[] subFiles = null; // files will contain
															// the names of
															// files
															// in
								// the directory
								UUID uid2 = null;
								try {
									uid2 = udc.udclient.login(FTPAddress, userId, dbClient.getUserPasswd(userId), 0);
									// Originally, files will contain the full
									// path names of the
									// files
									// in the directory
									logger.debug("Getting file list with targetFilter. SubDir:" + file.getName());

									if (SUBDIR_LIGAND) {
										subFiles = udc.udclient.getFileListFilter(uid2, dir.getName() + "/" + file.getName(), ligandFilter);
									} else if (SUBDIR_TARGET) {
										subFiles = udc.udclient.getFileListFilter(uid2, dir.getName() + "/" + file.getName(), targetFilter);
									}
								} catch (Exception e) {
									logger.error("Failed to get file list(" + dir.getName() + "/" + file.getName() + "). Check the directory,your id and passwd");
									e.printStackTrace();
									dbClient.reportSubmitError(-1, metaJobID, null, null, "Failed to get file list(" + dir.getName() + "/" + file.getName() + "). Check the directory,your id and passwd)!");
									// call_dbmgr("reportSubmitError", -1,
									// metaJobID, null, null,
									// "failed to get file list. Check udmanager(directory / id & passwd)!");
									dbClient.setMetaJobError(metaJobID, "Failed to get file list");
									// call_dbmgr("setMetaJobError", metaJobID,
									// "failed to get file list");
									return false;
								} finally {
									if (uid2 != null) {
										udc.udclient.logout(uid2, 0);
									}
								}

								if (subFiles != null) {
									logger.debug("subFiles.size :" + subFiles.length);

									for (FTPFile subFile : subFiles) { // start
																		// inner
																		// for
										/*
										 * We will use only the file names (not
										 * including the path) as parameter
										 * sweeps, e.g., ligand names only. This
										 * option can be specified in the JSDL
										 * script, e.g., <ns3:Directory
										 * filenameonly="true">
										 */

										if (subFile.isFile()) { // start
																// if(subFile.isFile())

											logger.debug(subFile.getName());
											if (fileNameOnly) { // start
																// if(fileNameOnly)
												as.addValue(file.getName() + "/" + subFile.getName().substring(0, subFile.getName().lastIndexOf('.')));
											} else {
												as.addValue(file.getName() + "/" + subFile.getName());
											} // end start if(fileNameOnly)
										} else {
											// only one recursive file folder is
											// valid
											logger.error("The type of this file : " + file.getName() + " is unKnown. Skipping...");
										} // end if(subFile.isFile())

									}// end inner for
								}

							} else {
								logger.error("The type of this file : " + file.getName() + " is unKnown. Skipping...");
							}
						}

					} else {
						logger.error("File is null :"  + dir.getName());
						dbClient.setMetaJobError(metaJobId, "Failed to get file list" + dir.getName());
						result = false;
					}
				}

				/*
				 * Sweep type is LoopInteger: iterating the integer loop Example
				 * LoopInteger parameter sweeps in the optimization code:
				 * <ns3:Sweep> <ns3:Assignment> <ns3:DocumentNode>
				 * <ns3:Match>EXPNUM</ns3:Match> </ns3:DocumentNode>
				 * <ns3:LoopInteger step="1" end="5" start="1"/>
				 * </ns3:Assignment> </ns3:Sweep>
				 */
				else if (functionType.equals("LoopInteger")) {
					LoopIntegerType li = (LoopIntegerType) assignment.getFunction().getValue();

					int i;
					int j = li.getEnd().intValue(); // End value, e.g., 5
					int k = li.getStep().intValue(); // Step value, e.g., 1
					for (i = li.getStart().intValue(); i <= j; i += k) {
						as.addValue("" + i);
					}
				}

				// Add the parsed assignment into the list
				aiList.addInfo(as);

			}

			List<MatchValue> pairs = null;

			/*
			 * Insert the metajob message into the MetaJobQueue only in the
			 * first level of parameter sweeps to avoid multiple insertions of
			 * the same metajob.
			 */
			/*
			 * if (level == 0) { Create a message to be inserted into the
			 * MetaJobQueue MetaDTO mdto = new MetaDTO();
			 * mdto.setMetaJobId(metaJobID); mdto.setUserId(userId); //
			 * mdto.setNumSubJob(numSubJob); mdto.setApp(appName);
			 * 
			 * // Insert the message into the MetaJobQueue try {
			 * metaMessageSender.changeDestiQueue(metaJobQueue);
			 * metaMessageSender.sendMessage(mdto); } catch (Exception e) {
			 * logger.error(e.toString()); e.printStackTrace(); } }
			 */
			/*
			 * Iterate the assignments in the JSDL script and apply the match &
			 * value pairs to the JSDL script without parameter sweeps (mjdoc).
			 * mjdoc is used as a base JSDL script where match parameter is
			 * substituted with values and then submitted as a subjob (task).
			 */

			while (true) {
				/*
				 * Get the list of pairs in the AssignmentInfoList:
				 * AssignmentInfoList basically consists of multiple
				 * AssignmentInfo's and the aiList.next() will call AI1.next(),
				 * AI2.next(), ... , AIn.next() where AI1, AI2, ..., AIn are the
				 * assignments included in the AssignmentInfoList. Each call of
				 * AIi.next() gets the pairs of (matches, value v) for a
				 * specific value v. For example, if the AIi is the
				 * "LoopInteger" as in the optimization JSDL script, AIi.next()
				 * will subsequently return pairs of (math, value) such as
				 * (EXPNUM, 1), (EXPNUM, 2), (EXPNUM, 3), (EXPNUM, 4) and
				 * (EXPNUM, 5).
				 */
				pairs = aiList.next();
				if (pairs == null)
					break;

				// Apply the match-value substitutions to the base JSDL (mjdoc)
				// and
				// create a new JSDL for a task (newdoc)
				String newdoc = mjdoc;
				for (MatchValue pair : pairs) {
					if (pair.getValue().contains("/")) {
						String[] value = pair.getValue().split("/");
						newdoc = newdoc.replace(pair.getMatch(), value[1]);
						if (SUBDIR_LIGAND) {
							newdoc = newdoc.replace("SUBDIRL", value[0]);
						} else if (SUBDIR_TARGET) {
							newdoc = newdoc.replace("SUBDIRT", value[0]);
						}
					} else {
						newdoc = newdoc.replace(pair.getMatch(), pair.getValue());
					}
				}

				// Only one-level of parameter sweep is defined
				if (sweep.getSweep().isEmpty()) {
					// Submit a task (subjob) into the HTCaaS using the newly
					// created
					// newdoc JSDL
					// call : processSeep --> submitSubJob
					submitSubJob(newdoc, metaJobID, userId, numSubJob);
					numSubJob++;
				} else {
					// Multiple levels of parameter sweeps are defined as in the
					// autodock
					// script
					result = processSweep(newdoc, sweep.getSweep().get(0), userId, metaJobID, level + 1, jsdlFileNoSweep);
				}
			}// The end of while (true)

			return result;
		}// The end of processSweep function

	}

	// class Splitter }}}

	/* cancel the MetaJob */
	// JobManagerImpl::cancelMetaJob
	@Override
	public int cancelMetaJob(String userId, int metaJobId) {
		logger.info("cancelMetaJob " + userId + " " + metaJobId);

		int num = -1;
		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			/*
			 * Update the status of the MetaJob in the DBManager into
			 * "canceled". This changed status will be detected by the
			 * submitSubJob function which will eventually stop the Splitter
			 * thread.
			 */
			// metajob 상태를 canceled 로 바꿈
			dbClient.setMetaJobStatus(metaJobId, Constant.METAJOB_STATUS_CANCEL);
			dbClient.setJobCancel(metaJobId);

			/*
			 * Sleep the current thread (cancelMetaJob) for a moment: This is to
			 * ensure that a separate Splitter thread detects the change of
			 * MetaJob status and stop the process (in the submitSubJob
			 * function). Otherwise, after removing the tasks from the queue,
			 * some of remaining tasks can be submitted through the Splitter
			 * thread.
			 */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/*
			 * Remove ActiveMQ messages in the user queue (with the given
			 * userId): This will result in deleting all subjobs (tasks) of the
			 * given MetaJob (with metaJobId) from the user queue.
			 */
			MessageCommander mc = new MessageCommander();
			num = mc.removeMessage(metaJobId, userId);
			logger.info(num + "jobs are removed");

		} else {
			logger.error("UserID don't match metaJobId");
		}

		return num;
	}// The end of cancelMetaJob function

	// remove a metajob
	// JobManagerImpl::removeMetaJob
	@Override
	public boolean removeMetaJob(String userId, int metaJobId) {
		logger.info("removeMetaJob " + userId + " " + metaJobId);

		boolean result = false;
		String uId = dbClient.getMetaJobUserId(metaJobId);
		if (uId == null) {
			return result;
		}
		File dbScript;
		File dir = new File("tmp");
		String errorMsg = "";

		/* Backup some contents of DB tables to be removed */
		/***********************************************************************************************************************************/
		/*
		 * SimpleDateFormat dateForm = new SimpleDateFormat("yyyyMMdd_HHmm");
		 * StringBuffer content = new StringBuffer();
		 * content.append("#!/bin/sh\n");
		 * content.append("mkdir -p db_backup\n");
		 * content.append("cd db_backup\n"); content.append("mysqldump -h" +
		 * DBAddress.split("/")[2] + " -uroot -pkisti123 " +
		 * DBAddress.split("/")[3] + " job > " + dateForm.format(new
		 * Date())+"_job.sql\n"); content.append("mysqldump -h" +
		 * DBAddress.split("/")[2] + " -uroot -pkisti123 " +
		 * DBAddress.split("/")[3] + " metajob > " + dateForm.format(new
		 * Date())+"_metajob.sql"); content.append("mysqldump -h" +
		 * DBAddress.split("/")[2] + " -uroot -pkisti123 " +
		 * DBAddress.split("/")[3] + " result > " + dateForm.format(new
		 * Date())+"_result.sql");
		 * 
		 * dbScript = new File(dir, UUID.randomUUID() + ".sh"); try {
		 * PrintStream ps = new PrintStream(dbScript); ps.println(content);
		 * ps.close(); } catch (Exception e) {
		 * logger.error("Failed to Generate db-backup Script: " +
		 * e.getMessage()); }
		 * 
		 * try {
		 * 
		 * ProcessBuilder: This class is used to create operating system
		 * processes. Each ProcessBuilder instance manages a collection of
		 * process attributes. The start() method creates a new Process instance
		 * with those attributes. The start() method can be invoked repeatedly
		 * from the same instance to create new subprocesses with identical or
		 * related attributes.
		 * 
		 * List<String> command = new ArrayList<String>(); command.add("chmod");
		 * command.add("+x"); command.add(dbScript.getName());
		 * 
		 * ProcessBuilder builder = new ProcessBuilder(command);
		 * builder.directory(dir); Process p = builder.start();
		 * 
		 * command.clear(); command.add(dbScript.getName());
		 * logger.info(command.toString()); p = builder.start();
		 * 
		 * int exitValue = p.waitFor();
		 * 
		 * if (exitValue == 0) { BufferedReader br = new BufferedReader(new
		 * InputStreamReader(p.getInputStream())); String line; while ((line =
		 * br.readLine()) != null) { logger.info(line); } br.close();
		 * 
		 * } else { StringBuffer sb = new StringBuffer();
		 * 
		 * logger.error("Exit Value: " + exitValue);
		 * logger.error("| [ErrorStream]");
		 * 
		 * BufferedReader brE = new BufferedReader(new
		 * InputStreamReader(p.getErrorStream()));
		 * 
		 * if (brE.readLine() == null) { BufferedReader brI = new
		 * BufferedReader(new InputStreamReader(p.getInputStream())); String
		 * line; while ((line = brI.readLine()) != null) { logger.error("| " +
		 * line); sb.append(line + "\n"); } brI.close(); } else { String line;
		 * while ((line = brE.readLine()) != null) { logger.error("| " + line);
		 * sb.append(line + "\n"); } }
		 * 
		 * brE.close();
		 * 
		 * errorMsg = sb.toString(); logger.error(errorMsg); } }catch(Exception
		 * e){
		 * 
		 * }finally{ dbScript.delete(); }
		 */
		/***********************************************************************************************************************************/

		/*
		 * Remove the MetaJob, job (task) and the results: First, check the
		 * status of the MetaJob. If the MetaJob is currently running the
		 * Splitter thread, cancel the MetaJob first. Second, remove the tasks
		 * and the MetaJob information from the queue and then, remove the
		 * results. Basically, this function should not be called while agents
		 * are processing the MetaJob and its tasks. (instead, cancelMetaJob
		 * should be called) TODO: First check the status of the MetaJob before
		 * dumping the DB contents.
		 */

		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			String status = dbClient.getMetaJobStatus(metaJobId);

			// The status of MetaJob is currently running the Splitter
			if (status.equals(Constant.METAJOB_STATUS_SPLIT) || status.equals(Constant.METAJOB_STATUS_SPLITTING)) {
				// Cancel the MetaJob first
				cancelMetaJob(userId, metaJobId);
				try {
					Thread.sleep(70000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Remove tasks and the MetaJob information from the DBManager
			if (dbClient.removeJobs(metaJobId) && dbClient.removeMetaJob(metaJobId)) {
				result = true;
			}
			// Remove the results of the MetaJob from the DBManager
			dbClient.removeResults(metaJobId);

		} else {
			logger.error("UserID don't match metaJobId");
		}

		return result;
	}// The end of removeMetaJob function

	/* resubmit subjobs */
	// JobManagerImpl::resubmitSubJob
	@Override
	public int resubmitSubJobByStatus(String userId, int metaJobId, String status) {
		logger.info("resubmitSubJobByStatus " + userId + " " + metaJobId + ", " + status);

		List<Integer> list = null;
		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			list = dbClient.getJobIdListByStatus(metaJobId, status);
			for (Integer id : list) {
				dbClient.reEnqueueJob(id);
			}
		} else {
			logger.error("UserID don't match metaJobId");
		}

		return list.size();
	}

	@Override
	public int resubmitSubJobSet(String userId, int metaJobId, Set<Integer> subJobSet) {
		logger.info("resubmitSubJobSet " + userId + " " + metaJobId);

		int ret = 0;
		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			for (Integer seq : subJobSet) {
				int jobId = dbClient.getJobId(metaJobId, seq);
				if (dbClient.reEnqueueJob(jobId)) {
					ret += 1;
				}
			}
		} else {
			logger.error("UserID don't match metaJobId");
		}

		return ret;
	}

	/* get the metajob progress */
	// JobManagerImpl::getMetaJobProgress
	@Override
	public Map<String, Integer> getMetaJobProgress(String userId, int metaJobId) {
		logger.info("getMetaJobProgress " + userId + " " + metaJobId);

		Map<String, Integer> status = null;
		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			status = dbClient.getMetaJobProgress(metaJobId);
		} else {
			logger.error("UserID don't match metaJobId");
		}

		return status;
	}

	/* get metajob object list */
	// JobManagerImpl::getMetaJobObjectList
	@Override
	public List<MetaJob> getMetaJobObjectList(String userId) {
		logger.info("getMetaJobObject " + userId);

		List<MetaJob> list = null;

		list = dbClient.getMetaJobObjectList(userId);

		return list;
	}

	/* get the metajob result */
	// JobManagerImpl::getMetaJobResult
	@Override
	public List<String> getMetaJobResult(String userId, int metaJobId) {
		logger.info("getMetaJobResult " + userId + " " + metaJobId);

		List<String> list = null;
		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			list = new ArrayList<String>();
			List<Integer> idList = dbClient.getJobIdList(metaJobId);

			for (Integer jobId : idList) {
				List<String> results = dbClient.getResults(jobId);
				for (String result : results) {
					list.add(result);
				}
			}

		} else {
			logger.error("UserID don't match metaJobId");
		}

		return list;
	}

	/* get metajob results of autodock application */
	// JobManagerImpl::getMetaJobResultAutodock
	@Override
	public List<String> getMetaJobResultAutodock(String userId, int metaJobId, int energyLvLow, int energyLvHigh) {
		logger.info("getMetaJobResultAutodock " + userId + " " + metaJobId + " " + energyLvLow + " " + energyLvHigh);

		List<String> list = null;
		String u = dbClient.getMetaJobUserId(metaJobId);
		if (userId.equals(u)) {
			list = new ArrayList<String>();
			List<Integer> idList = dbClient.getJobIdListAutodockEL(metaJobId, energyLvLow, energyLvHigh);

			for (Integer jobId : idList) {
				List<String> results = dbClient.getResults(jobId);
				for (String result : results) {
					list.add(result);
				}
			}

		} else {
			logger.error("UserID don't match metaJobId");
		}

		return list;
	}

	/* get the job result */
	// JobManagerImpl::getJobResult
	@Override
	public List<String> getJobResult(String userId, int jobId) {
		logger.info("getJobResult " + userId + " " + jobId);

		List<String> results = null;
		String u = dbClient.getMetaJobUserId(dbClient.getJobMetaJobId(jobId));
		if (userId.equals(u)) {
			results = dbClient.getResults(jobId);
		} else {
			logger.error("UserID don't match metaJobId");
		}

		return results;
	}

	@Override
	public List<String> getJobIdListResult(String userId, List<Integer> jobIdList) {
		logger.info("getJobListResult " + userId);

		List<String> results = null;

		int i = 0;
		for (Integer jobId : jobIdList) {
			if (i == 0) {
				String u = dbClient.getMetaJobUserId(dbClient.getJobMetaJobId(jobId));
				if (userId.equals(u)) {
					results = dbClient.getResults(jobId);
				} else {
					logger.error("UserID don't match metaJobId");
					break;
				}
			} else {
				for (String re : dbClient.getResults(jobId)) {
					results.add(re);
				}
			}

			i++;
		}

		return results;
	}
}
