package org.kisti.htc.monitoring.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.dbmanager.beans.AgentInfo;
import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.Job;
import org.kisti.htc.dbmanager.beans.MetaJob;
import org.kisti.htc.dbmanager.beans.ServiceInfra;
import org.kisti.htc.dbmanager.beans.User;
import org.kisti.htc.dbmanager.server.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitoringImpl implements Monitoring {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringImpl.class);

	private String DBManagerURL;
	private Database dbClient; // DBManager client
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;

	public MonitoringImpl() {

		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));

			DBManagerURL = prop.getProperty("DBManager.Address");

			if (prop.getProperty("SSL.Authentication").equals("true")) {
				SSL = true;
				DBManagerURL = DBManagerURL.replace("http", "https");
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}

			logger.info("DBManagerURL: {}", DBManagerURL);

		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}

		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Database.class);
		factory.setAddress(DBManagerURL);

		factory.getServiceFactory().setDataBinding(new AegisDatabinding());
		dbClient = (Database) factory.create();

		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(dbClient).getConduit();
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(300000);
		httpClientPolicy.setReceiveTimeout(300000);
		httpConduit.setClient(httpClientPolicy);

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

	}

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

	@Override
	public String getMetaJobUserId(int metaJobId) {
		logger.info("getMetaJobUserId " + metaJobId);

		return dbClient.getMetaJobUserId(metaJobId);
	}

	@Override
	public String getMetaJobJSDL(int metaJobId) {
		logger.info("getMetaJobJSDL " + metaJobId);

		return dbClient.getMetaJobJSDL(metaJobId);
	}

	@Override
	public Map<String, Integer> getMetaJobProgress(int metaJobId) {
		logger.info("getMetaJobProgress " + metaJobId);

		return dbClient.getMetaJobProgress(metaJobId);
	}

	@Override
	public MetaJob getMetaJobObject(int metaJobId) {
		logger.info("getMetaJobObject " + metaJobId);

		return dbClient.getMetaJobObject(metaJobId);
	}

	@Override
	public List<MetaJob> getMetaJobObjectList(String user) {
		logger.info("getMetaJobObjectList " + user);

		return dbClient.getMetaJobObjectList(user);
	}

	@Override
	public List<MetaJob> getMetaJobObjectListLimit(String user, int num) {
		logger.info("getMetaJobObjectList " + user + ", " + num);

		return dbClient.getMetaJobObjectListLimit(user, num);
	}

	@Override
	public List<Integer> getMetaJobIdList(String user) {
		logger.info("getMetaJobIdList " + user);

		return dbClient.getMetaJobIdList(user);
	}
	
	@Override
	public int getMetaJobListSubTotal(int startMetaId, int endMetaId) {
		logger.info("getMetaJobListSubTotal " + startMetaId + "- " + endMetaId);

		return dbClient.getMetaJobListSubTotal(startMetaId, endMetaId);
	}

	@Override
	public Map<String, Integer> getMetaJobProgressAll() {
		logger.info("getMetaJobProgressAll ");

		return dbClient.getMetaJobProgressAll();
	}

	@Override
	public Job getJobObject(int jobId) {
		logger.info("getJobObject " + jobId);

		return dbClient.getJobObject(jobId);
	}

	@Override
	public Job getJobObject(int metaJobId, int jobSeq) {
		logger.info("getJobObject M:" + metaJobId + " S:" + jobSeq);

		return dbClient.getJobObject(metaJobId, jobSeq);
	}

	@Override
	public List<Job> getJobObjectList(int metaJobId) {
		logger.info("getJobObjectList " + metaJobId);

		return dbClient.getJobObjectList(metaJobId);
	}
	
	@Override
	public List<Job> getMetaJobProgressinRange(String user, int start, int end) {
		logger.info("getMetaJobProgressinRange " + user + " from "+start+ " to " + end );

		return dbClient.getMetaJobProgressinRange(user, start, end);
	}

	@Override
	public List<Integer> getJobIdList(int metaJobId) {
		logger.info("getJobIdList " + metaJobId);

		return dbClient.getJobIdList(metaJobId);
	}

	@Override
	public List<Integer> getJobIdListByStatus(int metaJobId, String status) {
		logger.info("getJobIdListByStatus " + metaJobId + ", " + status);

		return dbClient.getJobIdListByStatus(metaJobId, status);
	}

	@Override
	public int getJobMetaJobId(int jobId) {
		logger.info("getJobMetaJobId " + jobId);

		return dbClient.getJobMetaJobId(jobId);
	}

	@Override
	public int getJobId(int metaJobId, int jobSeq) {
		logger.info("getJobId " + metaJobId + " , " + jobSeq);

		return dbClient.getJobId(metaJobId, jobSeq);
	}

	@Override
	public String getJobLog(int metaJobId, int jobSeq) {
		logger.info("getJobLog " + metaJobId + " , " + jobSeq);

		return dbClient.getJobLog(metaJobId, jobSeq);
	}

	@Override
	public String getJobLog(int jobId) {
		logger.info("getJobLog " + jobId);

		return dbClient.getJobLog(jobId);
	}

	@Override
	public List<Integer> getFailedJobIdList(int metaJobId) {
		logger.info("getFailedJobIdList " + metaJobId);

		return dbClient.getJobIdListByStatus(metaJobId, "failed");
	}

	@Override
	public List<Integer> getJobIdListAutodockEL(int metaJobId, int energyLvLow, int energyLvHigh) {
		logger.info("getJobIdListAutodockEL " + metaJobId);

		return dbClient.getJobIdListAutodockEL(metaJobId, energyLvLow, energyLvHigh);
	}

	@Override
	public String getAgentStatus(int agentId) {
		logger.info("getAgentStatus " + agentId);

		return dbClient.getAgentStatus(agentId);
	}

	@Override
	public String getAgentHost(int agentId) {
		logger.info("getAgentHost " + agentId);

		return dbClient.getAgentHost(agentId);
	}

	@Override
	public int getNumAliveAgent(int timelimit) {
		logger.info("getNumAliveAgent timelimit: " + timelimit);

		return dbClient.getNumAliveAgent(timelimit);
	}

	// @Override
	// public boolean needToSubmitAgentKSC() {
	// logger.info("needToSubmitAgentKSC");
	//
	// return dbClient.needToSubmitAgentKSC();
	// }
	@Override
	public List<String> getResults(int jobId) {
		logger.info("getResults " + jobId);

		return dbClient.getResults(jobId);
	}

	@Override
	public List<String> getAvailableWMSList(String voName) {
		logger.info("getAvailableWMSList VO: " + voName);

		return dbClient.getAvailableWMSList(voName);
	}

	@Override
	public String nextWMS(String ceName) {
		logger.info("nextWMS ceName:" + ceName);

		return dbClient.nextWMS(ceName);
	}

	@Override
	public String nextCE(String voName) {
		logger.info("nextCE voName:" + voName);

		return dbClient.nextCE(voName);
	}

	@Override
	public List<String> getAvailableCEList(String voName) {
		logger.info("getAvailableCEList VO: " + voName);

		return dbClient.getCENameList(voName, true, false);
	}

	@Override
	public List<String> getIntelligentCEList(String voName, int waitingTime, int numAgentRunning, int numAgentSubmitFailure, int waitingJob) {
		logger.info("getIntelligentCEList VO: " + voName + "  waitingTime:" + waitingTime + "  numAgentRunning:" + numAgentRunning + "  numAgentSubmitFailure:" + numAgentSubmitFailure
				+ "  waitingJob:" + waitingJob);

		return dbClient.getIntelligentCEList(voName, waitingTime, numAgentRunning, numAgentSubmitFailure, waitingJob);
	}

	@Override
	public List<String> getAvailableWMSListForCE(String ceName) {
		logger.info("getAvailableWMSListForCE CE: " + ceName);

		return dbClient.getAvailableWMSListForCE(ceName);
	}

	@Override
	public boolean checkAgentSleep(int agentId) {
		logger.info("checkAgentSleep {}", agentId);

		return dbClient.checkAgentSleep(agentId);
	}

	@Override
	public boolean checkAgentQuit(int agentId) {
		logger.info("checkAgentQuit {}", agentId);

		return dbClient.checkAgentQuit(agentId);
	}

	@Override
	public String getServerEnvValue(String name) {
		logger.info("getServerEnvValue " + name);

		return dbClient.getServerEnvValue(name);
	}

	@Override
	public String getServerEnvContent(String name) {
		logger.info("getServerEnvContents " + name);

		return dbClient.getServerEnvContent(name);
	}

	@Override
	public List<CE> getCEObjectList(int serviceInfra, boolean avail, boolean banned) {
		logger.info("getCEObjectList serviceInfra " + serviceInfra + ", avail " + avail + ", banned " + banned);

		return dbClient.getCEObjectList(serviceInfra, avail, banned);
	}

	@Override
	public int getNumUserAgentRunning(String userId) {
		logger.info("getUserAgentNum " + userId);

		return dbClient.getNumUserAgentRunning(userId);
	}

	@Override
	public String getNoticeContent(String div, String version) {
		logger.info("getNoticeContent " + div + ", " + version);

		return dbClient.getNoticeContent(div, version);
	}

	@Override
	public Map<String, Integer> getAgentNumMapFromMetaJob(int metaJobId) {
		logger.info("getAgentNumMapFromMetaJob " + metaJobId);

		return dbClient.getAgentNumMapFromMetaJob(metaJobId);
	}

	@Override
	public Map<String, Integer> getAgentTaskMapFromMetaJob(int metaJobId) {
		logger.info("getAgentTaskMapFromMetaJob " + metaJobId);

		return dbClient.getAgentTaskMapFromMetaJob(metaJobId);
	}

	@Override
	public Set<AgentInfo> getAgentInfoSetFromMetaJob(int metaJobId) {
		logger.info("getAgentInfoSetFromMetaJob " + metaJobId);

		return dbClient.getAgentInfoSetFromMetaJob(metaJobId);
	}

	private boolean ready(Reader in, long timeout) throws IOException {

		while (true) {
			long now = System.currentTimeMillis();

			try {
				while (in.ready() == false && timeout > 0) {
					Thread.sleep(100);
					timeout -= 100;
				}
				return in.ready();

			} catch (IOException e) {
				throw e;

			} catch (Exception e) {
				// ignore

			} finally {
				// adjust timer by length of last nap
				timeout -= System.currentTimeMillis() - now;

			}

		}
	}

	@Override
	public List<CE> getPLSICEInfo() throws Exception {
		logger.info("getPLSICEInfo SuperComputer Resource Info using 'llmcst'");

		long timeout = 3000;

		List<String> plsiList = new ArrayList<String>();

		String errorMsg = "";
		List<CE> ceList = new ArrayList<CE>();
		try {
			List<String> command = new ArrayList<String>();
			command.add("llmcst");

			logger.info(command.toString());

			ProcessBuilder builder = new ProcessBuilder(command);

			Process p = builder.start();
			int exitValue = p.waitFor();

			// exitValue = 1;
			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while (true) {
					if (ready(br, timeout)) {
						line = br.readLine();

						Pattern pattern = Pattern.compile("\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)");
						Matcher matcher = pattern.matcher(line);

						if (matcher.find()) {
							CE ce = new CE();
							ce.setName(matcher.group(1));
							ce.setNode(Integer.parseInt(matcher.group(2)));
							ce.setFreeCPU(Integer.parseInt(matcher.group(3).split("/")[0]));
							ce.setTotalCPU(Integer.parseInt(matcher.group(4)));

							ce.setWaitingJob(Integer.parseInt(matcher.group(5).split("/")[0]));
							ce.setRunningJob(Integer.parseInt(matcher.group(7).split("/")[0]));

							// logger.info("| cluster:" + ce.getName() +
							// " nodes:" + ce.getNode() + " availableCPU:" +
							// ce.getFreeCPU() + " totalCPU:" +
							// ce.getTotalCPU());
							if (ce.getName().equals("Total")) {
								continue;
							}
							// cluster, total cpu, available cpu, running jobs,
							// waiting jobs
							ceList.add(ce);
						}
					} else {
						logger.info("getPLSIINFO timeout : " + timeout);
						break;
					}
				}
				br.close();

			} else {
				StringBuffer sb = new StringBuffer();

				logger.error("Exit Value: " + exitValue);
				logger.error("| [ErrorStream]");

				BufferedReader brE = new BufferedReader(new InputStreamReader(p.getErrorStream()));

				if (brE.readLine() == null) {
					BufferedReader brI = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = brI.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
					brI.close();
				} else {
					String line;
					while ((line = brE.readLine()) != null) {
						logger.error("| " + line);
						sb.append(line + "\n");
					}
				}

				brE.close();

				errorMsg = sb.toString();

				new Exception(errorMsg);
			}

		} catch (Exception e) {
			logger.error("Failed to getPLSIINFO", e);
		}

		return ceList;

	}

	@Override
	public Map<String, Integer> getNumUserAgentCE(String agentStatus, int userId) {
		logger.info("getNumUserAgentCE " + agentStatus + ", UserID : " + userId);

		return dbClient.getNumUserAgentCE(agentStatus, userId);
	}

	@Override
	public User getUserInfo(String userId) {
		logger.info("getUserInfo " + userId);
		
		return dbClient.getUserInfo(userId);
	}
	
	@Override
	public List<ServiceInfra> getServiceInfraObjects(){
		logger.info("getServiceInfraObjects ");

		return dbClient.getServiceInfraObjects();
	}
	
	@Override
	public String getServiceInfraName(int id){
		logger.info("getServiceInfraName ");

		return dbClient.getServiceInfraName(id);
	}

	@Override
	public int getServiceInfraId(String name){
		logger.info("getServiceInfraId ");

		return dbClient.getServiceInfraId(name);
	}

	
	public static void main(String[] args) {
		MonitoringImpl dbi = new MonitoringImpl();

		int num=dbi.getMetaJobListSubTotal(97839, 97872);
		System.out.println(num);
//		 List<Job> list =dbi.getMetaJobProgressinRange("p260ksy", 78937, 78981);
////			stat = dbi.getMetaJobProgressinRange("p260ksy", 78937, 78981);
//			
//			for (int i = 0; i < 78981-78937; i++) {
//				 Job job = new Job();
//				 job =list.get(i);
//				 System.out.println(job.getId()+"\t\t"+job.getSeq()+"\t\t"+ job.getStatus());
//				
//			}
		
		
		
//		try {
//			for (CE ce : dbi.getPLSICEInfo()) {
//				System.out.println(ce.toString());
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		// dbi.getMetaJobUserName(3);
		// System.out.println(dbi.getMetaJobProgressAll());
		// System.out.println(dbi.getMetaJobObject(33).getProjectName());
		// System.out.println(dbi.getMetaJobObject(33).getScriptName());

		// System.out.println(dbi.getMetaJobStatusInfo(2));
		// System.out.println(dbi.getMetaJobStatusInfo("test").toString());

	}
}
