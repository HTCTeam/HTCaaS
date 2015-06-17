package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.kisti.htc.jobmanager.server.JobManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubmitMetaJob {

	final static Logger logger = LoggerFactory.getLogger(SubmitMetaJob.class);

	private static JobManager jmClient;
	private static String filename;
	private static String user;
	private static String JobManagerURL;
	private static String aMaxJobTimeMin ;
	private static String projectName;
	private static String scriptName;
	private static String resourceNames;

	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			
			JobManagerURL = prop.getProperty("JobManager.Address");
			//logger.info("JobManagerURL: {}", JobManagerURL);
			
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		/*
		| Usage: ./htcaas-job-submit
		| -h         Print help 
		| -f <arg>   JSDL File Name
		| -u <arg>   Set user id 
		| -r <arg>	 Set resources
		*/
		
		Options options = new Options();
		options.addOption("h", "help", false, "Print help");
		options.addOption("f", true, "JSDL File Name");
		options.addOption("t", true, "A Estimated Max Job Time(sec)");
		options.addOption("u", true, "Set User id");
		options.addOption("r", true, "Set resources");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			// help message
			if (cmd.hasOption("h")) {
				String help = "htcaas-job-submit [OPTION] ... [CONDITION] ...";
				String syn = "\nhtcaas-job-submit [-f <JSDL>] [-r <resource name>] [-t <wall time(sec)>] [-u <user ID>]";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Submit HTCaaS MetaJob\n", options, syn);
//				formatter.printHelp("SubmitMetaJob -f {jsdl} \t Submit HTCaaS Jobs", options);
				
				System.exit(0);
			} else {
				// jsdl file path
				if (cmd.hasOption("f")) {
					filename = cmd.getOptionValue("f");
				}
				if (cmd.hasOption("t")) {
					aMaxJobTimeMin = cmd.getOptionValue("t");
				}
				if (cmd.hasOption("u")) {
					user = cmd.getOptionValue("u");
				}
				
				if (cmd.hasOption("r")) {
					resourceNames = cmd.getOptionValue("r");
				}
			}

		//filename= "/usr/local/htc/HTCaaS/client/script/hello.jsdl";	
			if (filename == null || filename.startsWith("$") || filename.isEmpty()) {
				logger.error("need -f option and value(JSDL file)");
				System.exit(1);
			}
			
			System.out.println(filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}

		// read the jsdl file
		// filename : jsdl file
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			logger.error(e1.toString());
			System.exit(1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.toString());
			System.exit(1);
		}
		

		// prepare a job manager client
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(JobManager.class);
		factory.setAddress(JobManagerURL);
		factory.setDataBinding(new AegisDatabinding());
		jmClient = (JobManager) factory.create();
		
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
			logger.error(e.toString());
		}

		// Set project name
		Calendar calendar = new GregorianCalendar(Locale.KOREA);
		java.util.Date trialTime = new java.util.Date();
		calendar.setTime(trialTime);
		String mon = String.valueOf(calendar.get(Calendar.MONTH)+1);
		String date = String.valueOf(calendar.get(Calendar.DATE)+1);
		String year = String.valueOf(calendar.get(Calendar.YEAR));
		
		projectName=user+"-"+year+mon+date;
		
		
		// Set Script name 
		Date sdate= new Date();
		scriptName =user+"-cli-"+sdate;
		
//String s = "whoami = " + user;
//System.out.println(s);
		Map<Integer, String> result = null;
		// submit meta job using jmClient
		if(aMaxJobTimeMin!=null){
			
			result = jmClient.submitMetaJob(user, sb.toString(), Integer.parseInt(aMaxJobTimeMin), projectName, scriptName, resourceNames);
		}else{
			result = jmClient.submitMetaJob(user, sb.toString(), 0, projectName, scriptName, resourceNames);
		}
		if (result.containsKey(1)){
			System.out.println(result.get(1));
		}
		else
			logger.error("The metaJob submission is failed.! submit error:" + result.get(0));
	}

}
