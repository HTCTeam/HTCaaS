package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kisti.htc.message.MetaDTO;

import util.mLogger;
import util.mLoggerFactory;

public class GliteJob {

	protected Logger logger = Logger.getLogger(this.getClass());
	// final static mLogger logger = mLoggerFactory.getLogger("AM");

	private GliteResource gr;
	private String wmsName;
	private String ceName;
	private File submitScript;
	private File submitJDL;
	private int agentId;
	private String userId;
	private MetaDTO mDTO;

	private String type;

	public GliteJob(GliteResource gr, String wmsName, String ceName, String type, String userId, MetaDTO mDTO) {
		this.gr = gr;
		this.wmsName = wmsName;
		this.ceName = ceName;
		this.type = type;
		this.userId = userId;
		this.mDTO = mDTO;
	}

	public GliteJob(GliteResource gr, String wmsName, String ceName, String type) {
		this.gr = gr;
		this.wmsName = wmsName;
		this.ceName = ceName;
		this.type = type;
	}

	// Ganga Version
	private void generateSubmitScript() {

		StringBuffer content = new StringBuffer();
		content.append("config['LCG']['WMS']='" + wmsName + "'\n");
		content.append("config['LCG']['AllowedCEs']='" + ceName + "'\n");
		content.append("j=Job()\n");
		if (AgentManager.AGENT_SCALING_METRIC == AgentManager.samplingNagents) {
			content.append("j.application=Executable(exe=File('" + AgentManager.scriptDir.getAbsolutePath() + "/runSamplingAgent.sh'),args=['" + agentId + "'])\n");
		} else {
			content.append("j.application=Executable(exe=File('" + AgentManager.scriptDir.getAbsolutePath() + "/runAgels" + "nt.sh'),args=['" + agentId + "'])\n");
		}
		content.append("j.backend=LCG()\n");
		content.append("j.submit()\n");
		content.append("print j.id\n");

		submitScript = new File(AgentManager.tempDir, UUID.randomUUID() + ".py");
		try {
			PrintStream ps = new PrintStream(submitScript);
			ps.println(content);
			ps.close();
		} catch (Exception e) {
			logger.error("Failed to Generate Ganga Submit Script: " + e.getMessage());
		}
	}

	public boolean submit() {
		if (type.equals("Glite")) {
			return submitDirectly();
		} else if (type.equals("Ganga")) {
			return submitUsingGanga();
		} else if (type.equals("Glite-CREAM")) {
			return submitUsingCreamCE();
		} else {
			logger.error("Unknown Submission Type: " + type);
			return false;
		}
	}

	public boolean submitUsingGanga() {

		agentId = gr.getDBClient().addAgent();
		logger.info("| New Agent added, AgentID : " + agentId);

		gr.getDBClient().setAgentCE(agentId, ceName);

		generateSubmitScript();

		String errorMsg = "";

		try {
			List<String> command = new ArrayList<String>();
			command.add("ganga");
			command.add("--config=" + GliteResource.gangaConfig.getAbsolutePath());
			// command.add("--very-quiet");
			command.add(submitScript.getName());

			logger.info(command);

			ProcessBuilder builder = new ProcessBuilder(command);
			Map<String, String> envs = builder.environment();
			envs.put("X509_USER_PROXY", gr.getProxyFile().getAbsolutePath());
			builder.directory(AgentManager.tempDir);

			Process p = builder.start();
			int exitValue = p.waitFor();

			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = br.readLine();
				int gangaID = Integer.parseInt(line);
				logger.info("| Successfully submitted, gangaID: " + gangaID);
				gr.getDBClient().setAgentGangaId(agentId, gangaID);
				br.close();
			} else {
				StringBuffer sb = new StringBuffer();

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

				logger.error("Exit Value: " + exitValue);
				logger.error("WMS: " + wmsName + ", CE:" + ceName);
				logger.error("| [ErrorStream]");
				logger.error("| " + sb.toString());

				errorMsg = sb.toString();

				throw new Exception("Ganga Submission Error");
			}
		} catch (Exception e) {
			logger.error("Failed to submit a new agent", e);

			gr.getDBClient().reportSubmitError(agentId, mDTO.getMetaJobId(), wmsName, ceName, errorMsg);
			submitScript.delete();

			return false;
		}

		submitScript.delete();

		return true;
	}

	// Glite Command Version

	private void generateSubmitJDL() {

		StringBuffer content = new StringBuffer();
		content.append("Type = \"Job\";\n");
		content.append("JobType = \"Normal\";\n");
		if (AgentManager.AGENT_SCALING_METRIC == AgentManager.samplingNagents) {
			content.append("Executable = \"runSamplingAgent.sh\";\n");
			content.append("InputSandbox = {\"" + AgentManager.scriptDir.getAbsolutePath() + "/runSamplingAgent.sh\"};\n");
			content.append("Arguments = \"" + agentId + "\";\n");
		} else {
			content.append("Executable = \"runAgent.sh\";\n");
			content.append("InputSandbox = {\"" + AgentManager.scriptDir.getAbsolutePath() + "/runAgent.sh\"};\n");
			content.append("Arguments = \"" + agentId + " " + userId + "\";\n");
		}
		content.append("StdOutput = \"" + agentId + ".out\";\n");
		content.append("StdError = \"" + agentId + ".err\";\n");
		content.append("OutputSandbox = {\"" + agentId + ".out\", \"" + agentId + ".err\"};\n");

		submitJDL = new File(AgentManager.tempDir, UUID.randomUUID() + ".jdl");
		try {
			PrintStream ps = new PrintStream(submitJDL);
			ps.println(content);
			ps.close();
		} catch (Exception e) {
			logger.error("Failed to Generate Submit JDL: " + e.getMessage());
		}
	}

	public boolean submitDirectly() {

		agentId = gr.getDBClient().addAgent();
		logger.info("| New Agent added, AgentID : " + agentId);

		gr.getDBClient().setAgentCE(agentId, ceName);

		generateSubmitJDL();

		String errorMsg = "";

		try {
			List<String> command = new ArrayList<String>();
			command.add("glite-wms-job-submit");
			command.add("-a");
			command.add("-e");
			command.add(wmsName);
			command.add("-r");
			command.add(ceName);
			command.add(submitJDL.getName());

			logger.info(command);

			ProcessBuilder builder = new ProcessBuilder(command);
			Map<String, String> envs = builder.environment();
			envs.put("X509_USER_PROXY", gr.getProxyFile().getAbsolutePath());
			builder.directory(AgentManager.tempDir);

			String submitId = null;

			Process p = builder.start();
			int exitValue = p.waitFor();

			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					Pattern pattern = Pattern.compile("^(https:.*:\\d+/.+)");
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						submitId = matcher.group(1);
						logger.info("| Successfully submitted, submitID: " + submitId);
						gr.getDBClient().setAgentSubmitId(agentId, submitId);
						break;
					}
				}
				br.close();

				if (submitId == null) {
					logger.error("ExitValue is 0 but gLite jobID not found");
					throw new Exception("Glite Submission Error");
				}

			} else {
				StringBuffer sb = new StringBuffer();

				logger.error("Exit Value: " + exitValue);
				logger.error("WMS: " + wmsName + ", CE:" + ceName);
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

				throw new Exception("Glite Submission Error");
			}
		} catch (Exception e) {
			logger.error("Failed to submit a new agent", e);

			gr.getDBClient().reportSubmitError(agentId, mDTO.getMetaJobId(), wmsName, ceName, errorMsg);
			submitJDL.delete();

			return false;
		}

		submitJDL.delete();

		return true;
	}

	// Glite Command Version

	private void generateSubmitJDL_CreamCE() {

		StringBuffer content = new StringBuffer();
		content.append("[\n");
		content.append("Type = \"Job\";\n");
		content.append("JobType = \"Normal\";\n");
		if (AgentManager.AGENT_SCALING_METRIC == AgentManager.samplingNagents) {
			content.append("Arguments = \"" + agentId + "\";\n");
			content.append("Executable = \"runSamplingAgent.sh\";\n");
			content.append("InputSandbox = {\"" + AgentManager.scriptDir.getAbsolutePath() + "/runSamplingAgent.sh\"};\n");
		} else {
			content.append("Arguments = \"" + agentId + " " + userId + "\";\n");
			content.append("Executable = \"runAgent.sh\";\n");
			content.append("InputSandbox = {\"" + AgentManager.scriptDir.getAbsolutePath() + "/runAgent.sh\"};\n");
			content.append("StdOutput = \"" + agentId + ".out\";\n");
			content.append("StdError = \"" + agentId + ".err\";\n");
			content.append("OutputSandbox = {\"" + agentId + ".out\", \"" + agentId + ".err\"};\n");
			content.append("OutputSandboxBaseDestUri = \"gsiftp://localhost\";\n");

		}

		// content.append("Type = \"Job\";\n");
		// content.append("JobType = \"Normal\";\n");
		// content.append("Executable = \"runAgent.sh\";\n");
		// content.append("Executable = \"/bin/hostname\";\n");
		// content.append("Arguments = \"" + agentId + "\";\n");
		// content.append("Arguments = \"-f\";\n");
		// content.append("StdOutput = \"" + agentId + ".out\";\n");
		// content.append("StdError = \"" + agentId + ".err\";\n");
		// content.append("InputSandbox = {\"runAgent.sh\"};\n");
		// content.append("InputSandbox = {\"runAgent.sh\"};\n");
		// content.append("OutputSandbox = {\"" + agentId + ".out\", \"" +
		// agentId + ".err\"};\n");
		// content.append("OutputSandbox = {\"" + agentId + ".out\", \"" +
		// agentId + ".err\"};\n");
		// content.append("OutputSandboxBaseDestUri = \"gsiftp://localhost\";\n");

		content.append("]");

		// System.out.println(content.toString());

		submitJDL = new File(AgentManager.tempDir, UUID.randomUUID() + ".jdl");
		try {
			PrintStream ps = new PrintStream(submitJDL);
			ps.println(content);
			ps.close();
		} catch (Exception e) {
			logger.error("Failed to Generate Submit JDL: " + e.getMessage());
		}
	}

	public boolean submitUsingCreamCE() {

		agentId = gr.getDBClient().addAgent(userId);
		logger.info("| New Agent added, AgentID : " + agentId);

		gr.getDBClient().setAgentCE(agentId, ceName);

		generateSubmitJDL_CreamCE();

		String errorMsg = "";

		try {
			List<String> command = new ArrayList<String>();
			command.add("glite-ce-job-submit");
			command.add("-a");
			command.add("-r");
			command.add(ceName);
			command.add(submitJDL.getName());

			logger.info(command);

			ProcessBuilder builder = new ProcessBuilder(command);
			Map<String, String> envs = builder.environment();
			envs.put("X509_USER_PROXY", gr.getProxyFile().getAbsolutePath());
			builder.directory(AgentManager.tempDir);

			String submitId = null;

			Process p = builder.start();
			int exitValue = p.waitFor();

			// exitValue = 1;
			if (exitValue == 0) {
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {

					// https://kek2-ce06.cc.kek.jp:8443/CREAM686173306
					Pattern pattern = Pattern.compile("^(https:.*:\\d+/CREAM.+)");
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						submitId = matcher.group(1);
						logger.info("| Successfully submitted, submitID: " + submitId);
						gr.getDBClient().setAgentSubmitId(agentId, submitId);
						gr.getDBClient().increaseCESubmitCount(ceName, 1);
						break;
					}
				}
				br.close();

				if (submitId == null) {
					logger.error("ExitValue is 0 but gLite jobID not found");
					throw new Exception("Glite Submission Error");
				}

			} else {
				StringBuffer sb = new StringBuffer();

				logger.error("Exit Value: " + exitValue);
				logger.error("WMS: " + wmsName + ", CE:" + ceName);
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

				throw new Exception("Glite Submission Error");
			}
		} catch (Exception e) {
			logger.error("Failed to submit a new agent", e);

			if (mDTO != null) {
				gr.getDBClient().reportSubmitError(agentId, mDTO.getMetaJobId(), wmsName, ceName, errorMsg);
			}

			submitJDL.delete();
			
			if(e.getMessage().contains("EOF")){
				logger.error("EOF Error : Resubmit Glite Cream Job.");
				AgentManager.getSubmitQueue().addJob(new GliteJob(gr, wmsName, ceName, "Glite-CREAM", userId, mDTO));
			}else{
				logger.error("Passing Glite Cream Job.");
			}
			

			return false;
		}

		submitJDL.delete();

		return true;
	}

	public static void main(String[] args) throws Exception {

		GliteResource gm = new GliteResource("biomed");
		// GliteResource gm = new GliteResource("vo.france-asia.org");
		if (gm.getVomsProxyTimeLeft() < 3600 * 2) {
			gm.initVomsProxy();
		}

		// String wmsName =
		// "https://wms03.egee-see.org:7443/glite_wms_wmproxy_server";
		String wmsName = "CREAM";
		// String ceName = "darthvader.kisti.re.kr:8443/cream-pbs-vofa";
		// String wmsName =
		// "https://marwms.in2p3.fr:7443/glite_wms_wmproxy_server";
		// String ceName = "cccreamceli09.in2p3.fr:8443/cream-sge-long";
		// String ceName = "darthvader.kisti.re.kr:8443/cream-pbs-vofa";
		// String ceName = "ce02.eela.if.ufrj.br:8443/cream-pbs-biomeda";
		// String ceName = "ce.scope.unina.it:8443/cream-pbs-egee_short";
		String ceName = "cygnus.grid.rug.nl:8443/cream-pbs-medium";
		// GliteJob job = new GliteJob(gm, wmsName, ceName, "Glite-CREAM");
		GliteJob job = new GliteJob(gm, wmsName, ceName, "Glite-CREAM", "seungwoo", new MetaDTO());

		job.submit();
	}
}
