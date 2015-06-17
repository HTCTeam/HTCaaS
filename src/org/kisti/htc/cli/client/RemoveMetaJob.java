package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

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

public class RemoveMetaJob {

	final static Logger logger = LoggerFactory.getLogger(RemoveMetaJob.class);

	private static JobManager jmClient;
	private static String user;
	private static String JobManagerURL;
	private static String metaJobId;

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

		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("m", "meta",true, "MetaJob ID");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
//				formatter.printHelp("CancelMetaJob", options);
				String help= "htcaas-job-remove [OPTIONS] ";
				String syn= "\nhtcaas-job-remove [-m <metajob id>]";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Removes a metajob.\n", options, syn);				
				System.exit(0);
			} else {
				if (cmd.hasOption("m")) {
					metaJobId = cmd.getOptionValue("m");
				}
			}

			if (metaJobId == null || metaJobId.startsWith("$") || metaJobId.isEmpty()) {
				logger.error("need -m option and value(metaJobId)");
				System.exit(1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}

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
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		boolean ret = jmClient.removeMetaJob(user, Integer.parseInt(metaJobId));
		if (ret){
			System.out.println("Finished");
		}
		else
			System.out.println("The removal of the metajob is failed.! MetaJob ID:" + metaJobId);
	}

}
