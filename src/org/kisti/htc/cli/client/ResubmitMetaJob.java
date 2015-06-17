package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.ClientCacheControlType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.jobmanager.server.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ResubmitMetaJob {

	final static Logger logger = LoggerFactory.getLogger(ResubmitMetaJob.class);

	private static JobManager jmClient;
	private static String user;
	private static String JobManagerURL;
	private static String metaJobId;
	private static String subJobId;
	private static Set<Integer> subJobSet = new HashSet<Integer>();;
	private static String subJob[];
	private static String status;
	
	public static final int BOTH = 0;
	public static final int ONLYSubID = 1;
	public static final int ONLYStatus = 2;
	public static final int ERROR = -1; 
			
	private static int OPTION;
	
	


	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			
			JobManagerURL = prop.getProperty("JobManager.Address");

			
		} catch (Exception e) {
			System.out.println("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		/*
		| Usage: ./htcaas-job-resubmit 
		| -m <arg>   Metajob ID
		| -s <arg>   Seq of Subjob
		| -c <arg>   Status(waiting ||failed || done ....)		 
		| -h         Print help
		*/
		Options options = new Options();
		options.addOption("h", "help", false, "Print help");
		options.addOption("m", "meta", true, "Metajob ID");
		options.addOption("s", "seq",  true, "Sequence of Subjob(e.g., 1,2,..). One or multiple (e.g. 1 or 1-100 or 1,3,5-7)");
		options.addOption("st", true, "Status constraint to be resubmitted (waiting|failed|preparing|running|done|canceled) ");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		
		try {
			
			cmd = parser.parse(options, args);
			HelpFormatter formatter = new HelpFormatter();

			// help message
			if (cmd.hasOption("h")|| cmd.hasOption("help")) {
//				formatter.printHelp("htcaas-job-resubmit [OPTION] ...[CONDITION]...  \n :Resubmit HTCaaS Jobs \t htcaas-job-resubmit -m {metajobID} [-s {seq}] [-c {status}]", options);
				String help = "htcaas-job-resubmit [OPTION] ...[CONDITION]...";
				String syn = "\nhtcaas-job-resubmit [-m <metajobID>] [-s <seq>] [-st <status>]";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Resubmit HTCaaS Jobs\n", options, syn);

				

				System.exit(0);
			} else {

				if (cmd.hasOption("m")) { 
					metaJobId = cmd.getOptionValue("m");
				}
				if (cmd.hasOption("s")) {
					subJobId = cmd.getOptionValue("s");
				}
				if (cmd.hasOption("st")) {
					status = cmd.getOptionValue("st");
				}
			}
	
			
			if (metaJobId == null || metaJobId.startsWith("$") || metaJobId.isEmpty()) {
				logger.error("need -m option and value(Metajob ID)");
				System.exit(1);
			}
			
			if ( subJobId != null ){ 
				
				    if (subJobId.matches(".*;.*") && subJobId.matches(".*-.*")){
				    		subJob=subJobId.trim().split(";");
				    		for ( String s: subJob){	
				    			if (s.matches(".*-.*")) {
				    				int start = Integer.parseInt(s.trim().split("-")[0]);
				    				int end = Integer.parseInt(s.trim().split("-")[1]);
				    				for (int i = start; i <= end; i ++){
				    						subJobSet.add(i); 		}
				    			} else {				    				
				    				subJobSet.add(Integer.parseInt(s.trim()));
				    			}
				    		}
				    	
				    } else if (subJobId.matches(".*;.*")){
							subJob=subJobId.trim().split(";");
							for ( String s: subJob) {
								subJobSet.add(Integer.parseInt(s)); 
							}
					} else if (subJobId.matches(".*-.*")){
							subJob=subJobId.trim().split("-");
							int start = Integer.parseInt(subJob[0]);
							int end = Integer.parseInt(subJob[1]);
							for (int i = start; i <= end; i ++){
								subJobSet.add(i);
							}
					} else {
							subJobSet.add(Integer.parseInt(subJobId));
					}
				    
			} 
			
			
			
			if (status != null) {
				
				String status_temp = status.trim().toLowerCase();
				
				if (status_temp.contains("fail") || status_temp.contains("f") ) {
							status="failed";	
				} else if (status_temp.contains("wait") || status_temp.contains("wa")){
							status="waiting";	
				} else if (status_temp.contains("preparing")|| status_temp.contains("pre")){
							status="preparing";	
				} else if (status_temp.contains("run") || status_temp.contains("ru")){
							status="running";	
				} else if (status_temp.contains("done") || status_temp.contains("do")){
							status="done";	
				} else if (status_temp.contains("canceled") || status_temp.contains("ca")){
							status="canceled";
				} else {
							System.out.println("Error : -st {waiting|failed|preparing|running|done|canceled}");
							System.exit(1);
				}
					
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}

		

		// prepare a job manager client
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(JobManager.class);
		factory.setAddress(JobManagerURL);
		factory.setDataBinding(new AegisDatabinding());
		jmClient = (JobManager) factory.create();
		
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(jmClient).getConduit();
		
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(600000);
		httpClientPolicy.setReceiveTimeout(0);
//		httpClientPolicy.setCacheControl(ClientCacheControlType.NO_CACHE);
		httpConduit.setClient(httpClientPolicy);
		
		try {
			if (user == null) {
				String command = "whoami";
				ProcessBuilder builder = new ProcessBuilder(command);

				Process p;
				try {
					p = builder.start();
					int exitValue = p.waitFor();

					if (exitValue == 0) {
						BufferedReader br = new BufferedReader(
								new InputStreamReader(p.getInputStream()));
						String line;
						while ((line = br.readLine()) != null) {
							user = line;
						}
						br.close();

						if (user == null) {
							logger.error("host not found");
							throw new Exception("host error");
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}


		
		if (subJobId != null && status != null) {
			OPTION = BOTH;
		} else if (subJobId != null) { 
			OPTION = ONLYSubID;
		} else if (status != null) {
			OPTION = ONLYStatus;
		} else {
			OPTION = ERROR;
		}
		
		int result=-1;
		int result2=-1;		
		
		
		if (OPTION == BOTH) { 
			result = jmClient.resubmitSubJobByStatus(user, Integer.parseInt(metaJobId), status);
			if (result >=0){ 
				result2 = jmClient.resubmitSubJobSet(user, Integer.parseInt(metaJobId), subJobSet);
			}
			
		} else if (OPTION == ONLYSubID){
			result = jmClient.resubmitSubJobSet(user, Integer.parseInt(metaJobId), subJobSet);
		} else if (OPTION == ONLYStatus) {
			result = jmClient.resubmitSubJobByStatus(user, Integer.parseInt(metaJobId), status);
		} else {
			logger.error("Need Constraints :-s, -st");
			System.exit(1);
		}
		
		
		if (OPTION == BOTH){
			System.out.println(result+"\n"+result2);
		} else if (OPTION == ONLYSubID || OPTION == ONLYStatus){
			System.out.println(result); 
		} else {
			System.out.println("Error on Resubmission.:" +result +", " + result2);
			System.exit(1);
		}
		



	}

}
