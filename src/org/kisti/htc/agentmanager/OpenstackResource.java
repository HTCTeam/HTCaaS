package org.kisti.htc.agentmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.kisti.htc.dbmanager.server.Database;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
//import org.openstack4j.model.compute.ActionResponse;
import org.openstack4j.model.compute.*;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.model.compute.Action;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;



public class OpenstackResource  extends BackendResource{

	private Logger logger = Logger.getLogger(this.getClass());
	
	private static OSClient os;
	private String ReservationId;
	private String instanceId;
	private Keypair keyPair;
	private Flavor insFlavor;
	private String insType;
	private String userData;
	private List<String> ceList;
	
	// static preferences for about OpenStack Host Information
	private static String defaultEndpointURL = "http://fccont.kisti.re.kr:5000/v2.0"; //"http://203.252.195.71:5000/v2.0";
	private static String defaultCredentialID = "admin";
	private static String defaultCredentialPassword = "admin"; //controller
	private static String defaultTenantName = "admin";
	private static String defaultSecurityGroupID = "";
	private static String defaultKeypairName = "mykey"; //key2
	private static String defaultFlavorName = "m1.myflavor";
	private static String defaultFlavorID = "6";
	private static String defaultUbuntuCloudImageName = "snapshot";
	private static String defaultUbuntuCloudImageID = "1f94d94d-7d84-4824-8b1c-7ea0fbf1b210";
	private static String defaultInstancePrefix = "Openstack_vm";
	private static String defaultVmId = "ubuntu";
	
	public static String OPENSTACKNAME = "fccont.kisti.re.kr";  //203.252.195.71
	public static String OPENSTACKID = "root";  //controller
	public static int OPENSTACKPORT = 6980; //22
	
	static String resumeInstanceIp;
	static String suspendInstanceIp;
	static int suspendvm;
	static int runningvm;
	static List<? extends Server> suspend_sl;

	public OpenstackResource(String voName) {
		Properties prop = new Properties();

		try {
			prop.load(new FileInputStream(AgentManager.configPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {  //using openstack4j access openstack 
			os = OSFactory.builder()
					.endpoint(defaultEndpointURL)
					.credentials(defaultCredentialID, defaultCredentialPassword)
					.tenantName(defaultTenantName)
					.authenticate();

			
		} catch (AuthenticationException ose) {
			System.out.println("Caught Exception: " + ose.getMessage());
			System.out.println("Reponse Status Code: " + ose.getStatus());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		OPENSTACKNAME = prop.getProperty("OPENSTACK_NAME");

		this.name = voName;

//		getSuspendInstanceInfo();
		
	}


	@Override	
	public void updateCEInfo() throws Exception {

	    logger.info("+ Updating Openstack Resource Info using 'ssh'");
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		

	    List<String> ceList = new ArrayList<String>();

	    SshExecReturn result1 = null;
		
		SshClient sc = new SshClient();

		try {
			Session ss = sc.getSession(OpenstackResource.OPENSTACKNAME, OpenstackResource.OPENSTACKID, "fedcloud", OpenstackResource.OPENSTACKPORT);
		result1 = sc.Exec2("source /usr/local/sbin/osmonitor", ss, true);
			if (result1.getExitValue() == 0) {
		
				if (!result1.getStdOutput().isEmpty() && result1.getStdError().isEmpty()) {
						String out = result1.getStdOutput();
						
						logger.info(out);
						
						String cluster = null;
						String availableCPU = null;
						String numRunningJobs = null;
						String numRunningCores = null;
						String waitingJobs = "0";
						String totalCPU = null;
						String suspendedVM = null;
						String runningVM = null;
						String nodes = "controller";
						

						os = OSFactory.builder()
								.endpoint(defaultEndpointURL)
								.credentials(defaultCredentialID, defaultCredentialPassword)
								.tenantName(defaultTenantName)
								.authenticate();
						
						String[] out1 = out.split("\n");
						for(String line : out1){
							if(line.contains("TOTAL")) {
								String patternStr = "(\\d+)";
								Pattern pattern = Pattern.compile(patternStr);
								Matcher matcher = pattern.matcher(line);
								
								cluster = OpenstackResource.OPENSTACKNAME;
								int i = 0;
								while(matcher.find()){
									switch (i) {
										case 0 : //totalCPU = matcher.group(0); 
											totalCPU = "750";
											break;											
										case 1 :  numRunningCores = Integer.toString(Integer.parseInt(matcher.group(0)));
											break;
										case 2 :// availableCPU = matcher.group(0); 
											break;	
										case 3 : suspendedVM = matcher.group(0);
											break;
										case 4 : runningVM = matcher.group(0);
											break;
									}
									
									i+=1; 
									if(i>4)break;
								}
								availableCPU = Integer.toString(Integer.parseInt(totalCPU) - Integer.parseInt(runningVM)) ;
							} 

						}
						
						logger.info("| [cluster]:" + cluster + " [nodes]:" + nodes + " [runningCPU]:" + runningVM + " [availableCPU]:" + availableCPU + " [totalCPU]:" + totalCPU);
						//System.out.println(cluster + " " + totalCPU + " " + availableCPU + " " + numRunningCores + " " + waitingJobs);
						ceList.add(cluster + " " + totalCPU + " " + availableCPU + " " + numRunningCores + " " + waitingJobs);
						
				} else {
					throw new SubmitException(result1.getStdError());
				}
				
			} else {
			  throw new SSHException(result1.getStdError());
			}

		} catch (SSHException e1) {
			logger.error("Cluster Monitoring Error:1. Failed to update ceinfo", e1);

			try{
				
			}catch(Exception e){
				logger.error("SSH Inner Exception1");
				e.printStackTrace();
			}

		} catch (Exception e2) {
			logger.error("Cluster Monitoring Error:2. Failed to update ceinfo", e2);

			try{

			}catch(Exception e){
				logger.error("Inner Exception2");
				e.printStackTrace();
			}
			

		}
		
		// update SCCE info
	    AgentManager.dbClient.updateSCCEInfo("openstack", ceList);
	    logger.info("| ServiceInfra: " + name + " " + ceList.size() + " Cluster CEs updated to Database");
		
	  }
	public String getVoName() {
		return name;
	} 
	

	@Override
	public int getCEList(int ceMetric) {

		if (ceMetric == AgentManager.freeCPU || ceMetric == AgentManager.roundrobin) {

			ceList = AgentManager.dbClient.getCENameList(name, true, false);
			// _debug("ceList = ", ceList);

			// ceList = AgentManager.dbClient.getAvailableCEObjectList(voName);

		} else if (ceMetric == AgentManager.intelligent) {
			// ceList = AgentManager.dbClient.getIntelligentCEList(voName, 100,
			// 0, 5, 1000);
		}

		logger.info("ceList size:" + ceList.size());

		return ceList.size();
	}
	
	public String getNextCEName(int ceMetric) {

		String currentCE = null;

		ceList = AgentManager.dbClient.getCENameList(name, true, false);

		if (ceList == null || ceList.isEmpty()) {
			return null;
		} else {
			switch (ceMetric) {
			case AgentManager.freeCPU:
				currentCE = getHighFreeCPUCE();
				break;
			case AgentManager.priority:
				currentCE = getHighPriorityCE();
				break;
			default:
				currentCE = ceList.get(0);
			}

			// if(getDBClient().increaseCESubmitCount(currentCE, 1)){
			// return currentCE;
			// }else{
			// return null;
			// }
		}

		return currentCE;
	}
	
	public String getHighPriorityCE() {

		int pri = -9999;
		String ceName = null;
		logger.info("ceList size:" + ceList.size());
		for (String ce : ceList) {
			int pri_temp = getDBClient().getCEPriority(ce);
			int fCPU = getDBClient().getCEFreeCPU(ce);
			if (pri <= pri_temp && fCPU > 0) {
				pri = pri_temp;
				ceName = ce;
				logger.info("ce priority : " + ceName + " " + pri);
			}
		}

		return ceName;
	}

	public String getHighFreeCPUCE() {

		int cpu = 0;
		String ceName = null;
		logger.info("ceList size:" + ceList.size());
		for (String ce : ceList) {
			int cpu_temp = getDBClient().getCEFreeCPU(ce);
			if (cpu < cpu_temp) {
				cpu = cpu_temp;
				ceName = ce;
				logger.info("CE FREECPU : " + ceName + " " + cpu);
			}
		}

		return ceName;

	}
	@Override
	
	void cancelZombieJob() {

		 logger.debug("===Openstack cancelZombieJob===");
		    
//		    int port = 6980;
//		    SshExecReturn result2 = null;
//		    SshClient sc = new SshClient();
		    int siId = getDBClient().getServiceInfraId(AgentManager.OPENSTACK);
		    List<Integer> list = getDBClient().getAgentSubmittedZombieList(siId);
		    for (Integer aid : list) {
		      logger.error("Submitted Zombie Agent ID :  " + aid);

		      String userId = getDBClient().getAgentUserId(aid);
		      String ce = getDBClient().getAgentCEName(aid);
		      String submitId = getDBClient().getAgentSubmitId(aid);

		  		os = OSFactory.builder()
						.endpoint(defaultEndpointURL)
						.credentials(defaultCredentialID, defaultCredentialPassword)
						.tenantName(defaultTenantName)
						.authenticate();

		      try {

		    	 Server server = os.compute().servers().get(submitId);
		    	 if (server.getStatus().equals(Server.Status.ACTIVE)){
		    	     logger.info("| Successfully canceled, submitID: " + submitId);
		    		 vmSuspend(submitId);
		    		 server = os.compute().servers().get(submitId);
		    		 if (server.getStatus().equals(Server.Status.ACTIVE)){
		    			 vmSuspend(submitId); 
		    		 }
			         getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
		    	 } else {
		    		 logger.info("| Successfully canceled, submitID: " + submitId);
		    		 getDBClient().setAgentStatus(aid, AgentManager.AGENT_STATUS_CANCEL);
		    	 }
		  		
		      } catch (Exception e) {
		        logger.info("| Cancel is failed, submitId " + submitId);
		      }
		    }
		
	}
	
	public Database getDBClient() {
		return AgentManager.dbClient;
	}
	
	
	/////////////////////////
	// VM Mgmt Functions 
	/////////////////////////
	
	
	public List<? extends Server> getSuspendInstanceInfo() {
		
	    logger.info("+ getVMInstance Info : SUSPEND");
	    
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		
		//
		
		List <? extends Server> servers = os.compute().servers().list();
		List <Server> suspend_servers = new ArrayList<Server>();
		
		for (Server s : servers) {
			if(s.getStatus().equals(Server.Status.SUSPENDED)) {
				suspend_servers.add(s);
			}
		}
		
		logger.info("+ getVMInstance [SUSPEND] size: " + suspend_servers.size());
		
		return suspend_servers;
	}
	
	public List<? extends Server> getVMInstanceInfo(Server.Status status) {
		
	    logger.info("+ getVMInstance Info " + status );
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();

		List <? extends Server> servers = os.compute().servers().list();
		List <Server> output_servers = new ArrayList<Server>();
		
		for (Server s : servers) {
			if(s.getStatus().equals(status)) {
				output_servers.add(s);
			}
		}
		
		return output_servers;
	}
	
//	public void setSuspendInstanceList(List<Server> sv) {
//		
//		this.suspend_sl = sv;
//	}
//	

	public void vmResume(String suspendInstanceId){

		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		os.compute().servers().action(suspendInstanceId, Action.RESUME);
	
	}
	
	public void vmSuspend (String InstanceId){
		
		logger.info("Try to suspend vm : " + InstanceId);
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		os.compute().servers().action(InstanceId, Action.SUSPEND);
		
	}
	
	public List<Server> vmCreate(int cnt) {
		
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		
		List<Server> servers = new ArrayList<Server>();
		
		for (int i=0; i < cnt ; i++){
			Server vm = createOSInstances(defaultUbuntuCloudImageName, defaultKeypairName, defaultFlavorName, "");
			servers.add(vm);
		}
		
		return servers;
	}
	
	public void vmDelete (String vmId) {
		
		logger.info("Trying to delete vm .. [vmId]: "+ vmId);
		
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		
		os.compute().servers().delete(vmId);
		
	}
	
	
	// delete vm_setting.sh
	
	public boolean checkVMboot (Server sv) {
		
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		
		boolean result = false;
		
		String id = sv.getId();
		String ip;
		Server.Status status;
		SshClient sc = new SshClient();
		
		status = os.compute().servers().get(id).getStatus();
		
		try {
				while (!status.equals(Server.Status.ACTIVE)) {
					logger.info("[New VM Status] : " + status.toString());
					if (status.equals(Server.Status.ERROR)) {
						result = false;
						return false;
					}
					Thread.sleep(5000);
					status = os.compute().servers().get(id).getStatus();
				}
				if (status.equals(Server.Status.ERROR)) {
					result = false;
					return false;
				}
				logger.info("[New VM Status] : " + status.toString());
				
		} catch (InterruptedException e) {
				e.printStackTrace();
		}
			
		try {
			ip =os.compute().servers().get(id).getAddresses().toString().split("=")[3].split(",")[0];
			logger.info("[NEW VM's IP ]: "+ip);
			Session ss = sc.getSession(OpenstackResource.OPENSTACKNAME, OpenstackResource.OPENSTACKID, "fedcloud", OpenstackResource.OPENSTACKPORT);
			
			SshExecReturn result1;
			
			String cmd = "ssh -o StrictHostKeyChecking=no -i mykey ubuntu@"+ip+" hostname";
			result1 = sc.Exec2(cmd, ss, false);
		
			while(result1.getStdOutput().isEmpty() || !result1.getStdError().isEmpty()){
				logger.info("[VM Connection Error]: "+result1.getStdError());
				Thread.sleep(5000);
				result1 = sc.Exec2(cmd, ss, false);
			} 
			
			if(!result1.getStdOutput().isEmpty()){  // If Connection is Success 

				String out = result1.getStdOutput();
				logger.info("[VM Connected! Result]: "+out);
//				cmd = "/bin/bash /home/controller/HTCaaS_config/vm_setting.sh "+ip  ;
//				logger.info("| cmd : " + cmd);
//				result1 = sc.Exec2(cmd, ss, true);
//				logger.info(result1);
				result = true;
			} else{
				result = false;
			}
			
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (Exception e) {
		
			e.printStackTrace();
		}
			
		
		return result;
	}
	

	
	public Server createOSInstances(String imageName, String keyPairName, String insType, String availabilityZone){

		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		
		String reqImageID = "";
		String reqFlavorID = "";
		Server server = null;
		try {

			if(defaultFlavorName.equals(insType))
				reqFlavorID = defaultFlavorID;

			if(defaultUbuntuCloudImageName.equals(imageName))
				reqImageID = defaultUbuntuCloudImageID;

			// TODO: read the user_data
			// Create a Server Model Object
			ServerCreate sc = Builders.server()
					.name(generateName())
					.flavor(reqFlavorID)
					.image(reqImageID)
					.keypairName(keyPairName).build();

			// Boot the Server
			server = os.compute().servers().boot(sc);
//			server = os.compute().servers().bootAndWaitActive(sc, 20000);

		} catch (Exception e){
			e.printStackTrace();
		}

		return server;
	}
	
	
	private String generateName() {
		int result = (int)(Math.random() * 100000) + 100000;
		if ( result >= 100000)
			result -= 100000;

		return defaultInstancePrefix + result;
	}

	public static String getVmIp (String vmId) {
		
		os = OSFactory.builder()
				.endpoint(defaultEndpointURL)
				.credentials(defaultCredentialID, defaultCredentialPassword)
				.tenantName(defaultTenantName)
				.authenticate();
		
		String vmIp = os.compute().servers().get(vmId).getAddresses().toString().split("=")[3].split(",")[0];
	
		return vmIp;
	}
	

	
	
	public static void main(String[] arg) throws Exception {


		OpenstackResource or = new OpenstackResource("openstack");
		
//		List<? extends Server> sl = or.vmCreate(35);
////		List<? extends Server> sl = null;
//		List<Server> sr = new ArrayList<Server>();
//		
//		for (Server v : sl) {
//			boolean rst = or.checkVMboot(v);
//			System.out.println(rst);
//			
////			while (!rst){
////				or.vmDelete(v.getId());
////				Thread.sleep(5000);
////				v = or.vmCreate(1).get(0);
////				rst = or.checkVMboot(v);
////			}
//			sr.add(v);
//		}

		
//		------------------------------------------------
//		1.  To suspend vms that are in ACTIVE
//      -----------------------------------------------		
		List<? extends Server> sl = null;
		sl = or.getVMInstanceInfo(Server.Status.ACTIVE);
		for (Server v : sl){
			or.vmSuspend(v.getId());
		}
//        ------------------------------------------------
		

		
//      ------------------------------------------------
//		2. To delete vms on a specific node 
//      -----------------------------------------------			
//		List<? extends Server> sl = null;
//		int c =0;
//		sl = or.getVMInstanceInfo(Server.Status.SUSPENDED);
//		for (Server v : sl ) {
//			if(v.getHost().equals("fccomp2.kisti.re.kr"))
//			{ 	c++; 
//				or.vmDelete(v.getId());
//			}
//		}				
//		
//		System.out.println(c);
//      ------------------------------------------------
		
		
		
//      ------------------------------------------------
//		3. To delete vms are in Suspended
//      -----------------------------------------------				
//		sl = or.getVMInstanceInfo(Server.Status.SUSPENDED);
//		for (Server v : sl){
//			or.vmDelete(v.getId());
//		}
//      ------------------------------------------------s	
		
	}

}









