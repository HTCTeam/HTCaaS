package org.kisti.htc.agentmanager;

//import java.io.BufferedReader;
//import java.io.File;
//import java.io.InputStreamReader;
//import java.io.PrintStream;
//import java.net.InetAddress;
//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;
//import java.util.UUID;

//import org.apache.commons.net.ftp.parser.OS2FTPEntryParser;
import org.apache.log4j.Logger;
import org.kisti.htc.message.MetaDTO;
//import org.openstack4j.api.OSClient;
//import org.openstack4j.openstack.OSFactory;
import org.openstack4j.model.compute.Server;

//import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import util.mLogger;
import util.mLoggerFactory;

public class OpenstackJob {

	protected Logger logger = Logger.getLogger(this.getClass());
	// final static mLogger logger = mLoggerFactory.getLogger("AM");
	private static String AGENT_SCRIPT_FN = "runAgentOpenstack.sh";
	private static int SuspendedVM = 1;
	private static int NewVM = 2;

	private OpenstackResource or;
	private int agentId;
	private String dns;
	private String ceName;
	private String userId;
	private String type;
	private MetaDTO mDTO;
	private int vmType;
	private int jobCnt;
	private int vmCnt;

	private String vmIp; // openstackresource ip
	private String vmId;
	private int num = 0;
	private String mainId = "root"; // controller
	private List<Server> sr = new ArrayList<Server>();

	// private List<String> sv_ids = null;

	public OpenstackJob(OpenstackResource or, String type) {
		this.or = or;
		this.type = type;
	}

	public OpenstackJob(OpenstackResource or, String ceName, String type, String userId, MetaDTO mDTO) {
		this.or = or;
		this.ceName = ceName;
		this.type = type;
		this.userId = userId;
		this.mDTO = mDTO;
	}

	public OpenstackJob(OpenstackResource or, String ceName, String type, String userId, MetaDTO mDTO, int vmType, int vmCnt) {
		this.or = or;
		this.ceName = ceName;
		this.type = type;
		this.userId = userId;
		this.mDTO = mDTO;
		this.vmType = vmType;
		this.vmCnt = vmCnt;
	}

//	public OpenstackJob(OpenstackResource or, String ceName, String type, String userId, MetaDTO mDTO, int vmType, int jobCnt) {
//		this.or = or;
//		this.ceName = ceName;
//		this.type = type;
//		this.userId = userId;
//		this.mDTO = mDTO;
//		this.vmType = vmType;
//		this.jobCnt = jobCnt;
//	}

	public boolean submit() {
		if (num == 0) {
			return submitDirectly();
		} else {
			logger.error("Unknown Submission Type: " + type);
			return false;

		}
	}

	public boolean submitDirectly() {

		SshClient sc = new SshClient();

		SshExecReturn result1 = null;

		try {

			// agentId = or.getDBClient().addAgent(userId);
			// logger.info("| New Agent added, AgentID : " + agentId);
			// or.getDBClient().setAgentCE(agentId, ceName);

			Server s;
			List<? extends Server> sl;
			int failed = 0;

			if (vmType == SuspendedVM) {
//				sl = or.getSuspendInstanceInfo();
				sl = or.suspend_sl;

				// for (int i = 0; i < jobCnt; i ++) {

				// if (sl.size() ==0) {
				// logger.info("There's no Suspended VM. Will be deployed on New VM");
				// vmType = NewVM;
				// break; }
				logger.info("| Vm# tobe Resumed : "+vmCnt); //willbe deleted

				String vId = sl.get(vmCnt).getId();
				Server v = sl.get(vmCnt);
				or.vmResume(vId);
				sr.add(v);

				// }

				Thread.sleep(20000);

				// for ( Server v : sr) {

				Session ss = sc.getSession(OpenstackResource.OPENSTACKNAME, mainId, "fedcloud", OpenstackResource.OPENSTACKPORT);

				agentId = or.getDBClient().addAgent(userId);
				logger.info("| New Agent added, AgentID : " + agentId);
				or.getDBClient().setAgentCE(agentId, ceName);

				vmId = v.getId();
				vmIp = or.getVmIp(vmId);
				logger.info("vmIp: " + vmIp);

				String cmd = "/bin/bash /root/HTCaaS_config/submitJob.sh " + agentId + " " + userId + " " + vmId + " " + vmIp;
				
				cmd = cmd + " & ";
				result1 = sc.Exec2(cmd, ss, true); // userId
				logger.info("| exe cmd : " + cmd);
				logger.info("| exe result1 : " + result1.getStdOutput());
				logger.info("| exe result1 _err: " + result1.getStdError());

				if (!result1.getStdOutput().isEmpty() && result1.getStdError().isEmpty()) {
					String out = result1.getStdOutput();
					logger.info("| Successfully submitted, submitID: " + out);

					or.getDBClient().increaseCESubmitCount(ceName, 1);
					or.getDBClient().setAgentSubmitId(agentId, vmId);
				} else {
					// throw new SubmitException(result1.getStdError());
					logger.info("Submission(suspended vm) error!");
					failed++;
				}

				// }

			} else if (vmType == NewVM) {

				logger.info("New vm cnt: " + jobCnt);
				// sl = or.vmCreate(jobCnt);

				// for ( Server v : sl) {
				// for (int i =1; i<= jobCnt; i++) {

				agentId = or.getDBClient().addAgent(userId);
				logger.info("| New Agent added, AgentID : " + agentId);
				or.getDBClient().setAgentCE(agentId, ceName);

				// logger.info(Thread.currentThread().getName() + "| "+ i
				// +"th vm ");
				// logger.info("| "+ i +"th vm ");
				sl = or.vmCreate(1);
				Server v = sl.get(0);
				Session ss = sc.getSession(OpenstackResource.OPENSTACKNAME, mainId, "fedcloud", OpenstackResource.OPENSTACKPORT);
				boolean rst = or.checkVMboot(v);
				logger.info("[New vm status]: " + rst);

				while (!rst) {
					logger.info("[VM state is in ERROR and The vm will be deleted");
					or.vmDelete(v.getId());
					Thread.sleep(5000);
					v = or.vmCreate(1).get(0);
					rst = or.checkVMboot(v);
				}

				vmId = v.getId();
				vmIp = or.getVmIp(vmId);
				logger.info("vmIp: " + vmIp);

				String cmd = "/bin/bash /root/HTCaaS_config/submitJob.sh " + agentId + " " + userId + " " + vmId + " " + vmIp + " & "; // +
																																		// " > /dev/null &";
																																		// //JE.
																																		// 2015_01_02

				result1 = sc.Exec2(cmd, ss, true); // userId
				logger.info("| exe cmd : " + cmd);
				logger.info("| exe result1 : " + result1.getStdOutput());
				logger.info("| exe result1 _err: " + result1.getStdError());

				if (!result1.getStdOutput().isEmpty() && result1.getStdError().isEmpty()) {
					String out = result1.getStdOutput();
					logger.info("| Successfully submitted, submitID: " + out);

					or.getDBClient().increaseCESubmitCount(ceName, 1);
					or.getDBClient().setAgentSubmitId(agentId, vmId);
				} else {
					// throw new SubmitException(result1.getStdError());
					logger.info("Submission(suspended vm) error!");
					failed++;
				}

				// } //// end of For loop

			} else {

				logger.info("Undefined Vm Type ! " + vmType);
			}

			// for ( Server v : sr) {
			//
			// vmId = v.getId();
			// vmIp = or.getVmIp(vmId);
			// logger.info("vmIp: " + vmIp);
			//
			//
			// /*2015.01.28 JE*/
			// agentId = or.getDBClient().addAgent(userId);
			// logger.info("| New Agent added, AgentID : " + agentId);
			// or.getDBClient().setAgentCE(agentId, ceName);
			//
			// String cmd =
			// "/bin/bash /root/HTCaaS_config/submitJob.sh "+agentId+ " " +
			// userId+ " "+vmId+ " "+vmIp + " & "; // + " > /dev/null &"; //JE.
			// 2015_01_02
			//
			// result1 = sc.Exec2(cmd ,ss, false); //userId
			// logger.info("| exe cmd : " +cmd);
			// logger.info("| exe result1 : " +result1.getStdOutput());
			//
			// // if (!result1.getStdOutput().isEmpty() &&
			// result1.getStdError().isEmpty()) {
			// String out = result1.getStdOutput();
			// logger.info("| Successfully submitted, submitID: " + out);
			//
			// or.getDBClient().increaseCESubmitCount(ceName, 1);
			// or.getDBClient().setAgentSubmitId(agentId, out);
			// // } else {
			// // // throw new SubmitException(result1.getStdError());
			// // logger.info("error!");
			// // failed ++;
			// //
			// // }
			// }

			logger.info("# of FAILED VM : " + failed);
			// failed processing willbe added

		} catch (Exception e) {
			logger.error("error!!");
			e.printStackTrace();
		}

		return true;
	}

	public static void main(String arg[]) {
		OpenstackResource os = new OpenstackResource("openstack"); // openstack
		// resource first create job create.
		MetaDTO md;
		// OpenstackJob cj = new OpenstackJob(os, "203.252.195.71",
		// "openstack","controller",null, 1,14);

		// OpenstackJob cj = new OpenstackJob(os, "150.183.250.170",
		// "openstack", "p260ksy", null, 1, 2);
		OpenstackJob cj = new OpenstackJob(os, "150.183.250.170", "openstack", "p260ksy", null, 2, 5);

		cj.submit();
	}

}
