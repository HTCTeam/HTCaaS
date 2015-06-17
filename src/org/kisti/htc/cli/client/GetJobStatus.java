
package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
//import org.apache.cxf.transports.http.configuration.ClientCacheControlType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.constant.JobConstant;
import org.kisti.htc.dbmanager.beans.Job;
import org.kisti.htc.dbmanager.beans.MetaJob;
import org.kisti.htc.monitoring.server.Monitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetJobStatus {

	final static Logger logger = LoggerFactory.getLogger(GetJobStatus.class);

	private static Monitoring monitoring;
	private static String MonitoringURL;
	private static String MetaJobId = null;
	private static String SubJobId = null;
	private static boolean MetaJobInfo = false;
	private static boolean Progress = false;
	private static boolean SubJobInfo = false;
	private static boolean MetaJobsInfo = false;
	private static String User = null;
	private static String Status = null;
	private static MetaJob MetaJob = null;
	private static String MetaRange = null;
	private static String metajobs[];
//	private static int MetaJobnum = -1;
	private static int meta_Start = -1;
	private static int meta_End = -1;
	private static int limit = -1;
	private static boolean getActive = false;

	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			
			MonitoringURL = prop.getProperty("Monitoring.Address");
			//logger.info("MonitoringURL: {}", MonitoringURL);
			
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}

		try {
			if (User == null) {
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
							User = line;
						}
						br.close();

						if (User == null) {
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
			logger.error(e.toString());
		}
	}

	public static void main(String[] args) {


		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("a", false, "print all MetaJob Info (with limit)");
		options.addOption("m", true, "MetaJob id");
		options.addOption("i", false, "print MetaJob Info");
		options.addOption("p", false, "print Progress");
		options.addOption("s", false, "print SubJob Info, Put nothing or Sequence of subjob(e.g., 1, 2, ..)");
		options.addOption("st", true, "print SubJob Info by Status");
		options.addOption("c", "const",true, "Metajob Id range(e.g., 1-100); Return status of the recent MetaJobs within constraint");
		options.addOption("ac", "active", false, "[Used with -c option ]print all the status of active metajobs except 'done',");
		options.addOption("u", false, "user ID");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
				String help= "htcaas-job-status [OPTIONS] ";
				String syn= "\nhtcaas-job-status [-m <metajob id> ] [-s <sequence>] [custom options]   ";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Get a status of metajob(subjobs) \n", options, syn);
				System.exit(0);
			}
			else if(cmd.hasOption("a")){
				
				String tmp = null;
				for(String arg : args){
					tmp += " ";
					tmp += arg;
				}
				String[] value = tmp.split("-a");
				
				if(value.length > 1){
					limit = Integer.parseInt(value[1].trim());
				}
				
				MetaJobsInfo = true;
				
				
								
			} else if(cmd.hasOption("c")){
				MetaRange = cmd.getOptionValue("c");
				if(MetaRange.matches(".*-.*")){
					metajobs = MetaRange.trim().split("-");
					meta_Start = Integer.parseInt(metajobs[0]);
					meta_End = Integer.parseInt(metajobs[1]);
					
				}else {
					logger.error("Need constraint with -c option ");
					System.exit(1);
				}
				
				if (cmd.hasOption("ac")) { 
					getActive = true;					
				}
				
			} 	else {
				if(cmd.hasOption("m")) {
					MetaJobId = cmd.getOptionValue("m");

					if (MetaJobId == null || MetaJobId.startsWith("$") || MetaJobId.isEmpty()){
						logger.error("need -m option and value(metaJob id)");
						System.exit(1);
					}
				} 
				if (cmd.hasOption("i")){
					MetaJobInfo = true;
				}else if(cmd.hasOption("p")){
					Progress = true;
				}else if (cmd.hasOption("s")){
						
					String tmp = null;
					for(String arg : args){
						tmp += " ";
						tmp += arg;
					}
					String[] value = tmp.split("-s");
					
					if(value.length > 1){
						SubJobId = value[1].trim();
					}else{
						SubJobInfo = true;
					}
				}else if(cmd.hasOption("st")){
					Status = cmd.getOptionValue("st");
					if(!(Status.equals(JobConstant.JOB_STATUS_WAIT) || Status.equals(JobConstant.JOB_STATUS_CANCEL) || Status.equals(JobConstant.JOB_STATUS_DONE) || Status.equals(JobConstant.JOB_STATUS_FAIL) || Status.equals(JobConstant.JOB_STATUS_PRE) || Status.equals(JobConstant.JOB_STATUS_RUN))){
						logger.error("Wrong job status");
						System.exit(1);
					}
				}
				
				if (cmd.hasOption("u")){
					User = cmd.getOptionValue("u");
					
				}
			}

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Monitoring.class);
		factory.setAddress(MonitoringURL);
		factory.setDataBinding(new AegisDatabinding());
		monitoring = (Monitoring) factory.create();
		
		
		HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(monitoring).getConduit();
		
		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(600000);
		httpClientPolicy.setReceiveTimeout(0);
//		httpClientPolicy.setCacheControl(ClientCacheControlType.NO_CACHE);
		httpConduit.setClient(httpClientPolicy);
		
		if(MetaJobsInfo){
			List<MetaJob> list = null;
			if(limit > 0){
				list = monitoring.getMetaJobObjectListLimit(User, limit);
			}else{
				list = monitoring.getMetaJobObjectList(User);
			}
			
			for(MetaJob mj : list){
				System.out.println(mj.toString());
			}
		}else if(MetaRange != null && !MetaRange.isEmpty()){ 

						List<Job> pair = monitoring.getMetaJobProgressinRange(User, meta_Start, meta_End);
						int tot_sub_cnt =0;
						
						tot_sub_cnt = monitoring.getMetaJobListSubTotal(meta_Start, meta_End);

						Job job = new Job(); 
						if(getActive){ // only active
								for (int i = 0; i < pair.size(); i++) {
											job =pair.get(i);
										 if(!job.getStatus().equalsIgnoreCase(JobConstant.JOB_STATUS_DONE)){
											 System.out.println(job.getId()+"\t\t"+job.getSeq()+"\t\t"+ job.getStatus()); }
									}	
								
								if (tot_sub_cnt != pair.size()){
										for (int i = pair.size(); i<tot_sub_cnt; i++  ){
											System.out.println("0"+"\t\t"+"0"+"\t\t"+ "null");											
										}
								}
									
						} else {  // all jobs  
								for (int i = 0; i <  pair.size(); i++) {
									 job =pair.get(i);
									 System.out.println(job.getId()+"\t\t"+job.getSeq()+"\t\t"+ job.getStatus()); 	}
								
								if (tot_sub_cnt != pair.size()){ 
									if (tot_sub_cnt != pair.size()){
										for (int i = pair.size(); i<tot_sub_cnt; i++  ){
											System.out.println("0"+"\t\t"+"0"+"\t\t"+ "null");		}	
										}			
								}
							}
				
			
		}else if(MetaJobInfo){
			MetaJob = monitoring.getMetaJobObject(Integer.parseInt(MetaJobId));
			System.out.println(MetaJob.toStringJSDL());
		} else if(Progress){
			MetaJob = monitoring.getMetaJobObject(Integer.parseInt(MetaJobId));
			Map<String, Integer> pair = monitoring.getMetaJobProgress(Integer.parseInt(MetaJobId));
			System.out.println("=============================");
			System.out.println("MetaJob ID: " + MetaJob.getId());
			System.out.println("Total Jobs: " + MetaJob.getTotal());
			if(pair.containsKey("waiting")){
				System.out.println("Waiting Jobs : "+ pair.get("waiting"));
			} else {
				System.out.println("Waiting Jobs : 0");
			}
			if(pair.containsKey("preparing")){
				System.out.println("Preparing Jobs : "+ pair.get("preparing"));
			} else {
				System.out.println("Preparing Jobs : 0");
			}
			if(pair.containsKey("running")){
				System.out.println("Running Jobs : "+ pair.get("running"));
			} else {
				System.out.println("Running Jobs : 0");
			}
			if(pair.containsKey("done")){
				System.out.println("Done Jobs : "+ pair.get("done"));
			} else {
				System.out.println("Done Jobs : 0");
			}
			if(pair.containsKey("failed")){
				System.out.println("Failed Jobs : "+ pair.get("failed"));
			} else {
				System.out.println("Failed Jobs : 0");
			}
			if(pair.containsKey("canceled")){
				System.out.println("canceled Jobs : "+ pair.get("canceled"));
			} else {
				System.out.println("canceled Jobs : 0");
			}
//			System.out.println("=============================");
		} else if (SubJobId != null && !SubJobId.isEmpty()){
			Job subJob = monitoring.getJobObject(Integer.parseInt(MetaJobId),Integer.parseInt(SubJobId));
			System.out.println(subJob.toString());
			
		} else if (SubJobInfo){
			List<Job> list = monitoring.getJobObjectList(Integer.parseInt(MetaJobId));
			for(Job job : list){
				
				System.out.println(job.toString());
				
			}
		} else if (Status != null && !Status.isEmpty()){
			List<Integer> list = monitoring.getJobIdListByStatus(Integer.parseInt(MetaJobId), Status);
			for(Integer id : list){
				System.out.println(monitoring.getJobObject(id));
			}
		} else{
			HelpFormatter formatter = new HelpFormatter();
//			formatter.printHelp("GetJobStatus", options);
			String help= "htcaas-job-status [OPTIONS] ";
			String syn= "\nhtcaas-job-status [-m <metajob id> ] [-s <sequence>] [custom options]   ";
			syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
			formatter.printHelp(250, help, "Get a status of metajob(subjobs) \n", options, syn);
			System.exit(0);
		}
		

		
	}

}
