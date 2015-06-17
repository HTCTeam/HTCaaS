package org.kisti.htc.agentmanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import util.mLogger;
import util.mLoggerFactory;

public class MatchWorkQueue extends WorkQueue {
		
//  final static mLogger logger = mLoggerFactory.getLogger("AM");
  private Logger logger = Logger.getLogger(this.getClass());
  
	private GliteResource gr;
	
	public MatchWorkQueue(GliteResource gr, String queueName, int nThreads) {
		super(queueName, nThreads);
		this.gr = gr;
//		logger = Logger.getLogger(MatchWorkQueue.class);
		startWorkers();
	}
    
	@Override
	public void doWork(String workerName, Object object) {
		String wmsName = (String) object;
		logger.debug("| " + queueName + " Thread_" + workerName + " Retrieving matched CEs for WMS: " + wmsName);
		
		List<String> ceList = new ArrayList<String>();
		try {
			int start = Integer.parseInt(getCurrentTimeSecond(new Date()));

			List<String> command = new ArrayList<String>();
			command.add("glite-wms-job-list-match");
			command.add("-e");
			command.add(wmsName);
			command.add("-a");
			command.add(gr.getMatchJDL().getAbsolutePath());
	        
			logger.debug(command);
	        
			ProcessBuilder builder = new ProcessBuilder(command); 
			Map<String, String> envs = builder.environment();
			envs.put("X509_USER_PROXY", gr.getProxyFile().getAbsolutePath());
        
			Process p = builder.start();
	        
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			
			// " - kek2-ce06.cc.kek.jp:8443/cream-pbs-fkppl"
			// " - cclcgceli01.in2p3.fr:2119/jobmanager-bqs-long"
			String regExpr = "\\s*-\\s+(.+(jobmanager|cream).+)";
			Pattern pattern = Pattern.compile(regExpr);
			while ((line = in.readLine()) != null) {
				// logger.info(line);
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String ceName = matcher.group(1).trim();
					ceList.add(ceName);
					logger.info("| " + ceName);
				}
			}

			int end = Integer.parseInt(getCurrentTimeSecond(new Date()));
			int responseTime = end - start;

			logger.info(wmsName + " | responseTime:" + responseTime + " numCE:" + ceList.size());
	        
			// CXF cannot handle an argument of an empty list 
			if (ceList.isEmpty()) {
				ceList.add("EMPTY");
			}

			gr.getDBClient().setWMSInfo(gr.getVoName(), wmsName, responseTime, ceList);

		} catch (Exception e) {
			e.printStackTrace();
			logger.fatal("Failed to Find Available CEs: " + e.getMessage());
		}

		logger.debug("| " + queueName + " Thread_" + workerName + " Retrieving matched CEs for WMS: " + wmsName + " Done");

	}
  
	public static String getCurrentTimeSecond(Date date) {
		String time = "" + date.getTime();
		return time.substring(0, time.length() - 3);
	}
}
