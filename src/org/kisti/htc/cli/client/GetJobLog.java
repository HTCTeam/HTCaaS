
package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.UUID;

import javax.activation.DataHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.kisti.htc.monitoring.server.Monitoring;
import org.kisti.htc.udmanager.client.UDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetJobLog {

	final static Logger logger = LoggerFactory.getLogger(GetJobLog.class);

	private static Monitoring monitoring;
	private static String MonitoringURL;
	private static UDClient udc = new UDClient();;
	private static String MetaJobId = null;
	private static String jobSeq = null;
	private static String User = null;
	private static String Pw = null;
	private static String log = null;
	
	private static String FTPAddress = null;
	
 

	/**
	 * @param args
	 */
	static{
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
			
			MonitoringURL = prop.getProperty("Monitoring.Address");
			FTPAddress = prop.getProperty("FTP.Address");
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
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
	
	

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("h","help", false, "print help");
		options.addOption("m", "M", true, "Metajob id");  // MetaJobId 
		options.addOption("s", "S", true, "Job sequence for the subjob"); //jobSeq

		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
//				formatter.printHelp("htcaas-job-log -m {METAID} -s {SEQ}\t Get a log(stdout&stderr) of a subjob", options);
				String help= "htcaas-job-log [OPTIONS] ";
				String syn= "\nhtcaas-job-log [-m <metajob id> ] [-s <sequence>] ";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Get a log(stdout&stderr) of a subjob\n", options, syn);
				System.exit(0);
			} else {
				if(cmd.hasOption("m")) {
					MetaJobId = cmd.getOptionValue("m");
//					System.out.println("metaJobId:"+MetaJobId);
				} 
				if(cmd.hasOption("s")) {
					jobSeq = cmd.getOptionValue("s");

				} 

			}

			if (MetaJobId == null || MetaJobId.startsWith("$") || MetaJobId.isEmpty()){
				logger.error("-m : Need Argument(MetaJob id)");
				System.exit(1);
			}
			
			if (jobSeq == null || jobSeq.startsWith("$") || jobSeq.isEmpty()){
				logger.error("-s: Need Argument(Job seq), e.g., -s 1");
				System.exit(1);
			}
			
		} catch (Exception e) {
			
//			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(1);
		}
		
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Monitoring.class);
		factory.setAddress(MonitoringURL);
		factory.setDataBinding(new AegisDatabinding());
		monitoring = (Monitoring) factory.create();
		
		
		///////////////////
		//FIXME 
		User = "htcaas";
		Pw = "htcaas";
		//////////////////
		
		String logPath = monitoring.getJobLog(Integer.parseInt(MetaJobId), Integer.parseInt(jobSeq));
		

//System.out.println(logPath+"\n");
//System.out.println(FTPAddress+"\n"+User+"\n"+Pw+"\n");
		
		
		UUID uid= null;
		
		try{
			uid = udc.udclient.login(FTPAddress, User, Pw);
			
			//log = udc.readFile(uid, logPath);
			
			String [] file = logPath.split("/");
			String fname= file[file.length - 1];
			String remotedir = logPath.substring(0,logPath.length() - fname.length());
			
			
			DataHandler dh = null ; 
			
			dh = udc.udclient.getFileData(uid, fname, remotedir, 0);
			
			if(dh != null ) {
				StringBuffer out = new StringBuffer();
				byte[] b = new byte[4096];
				for (int n; (n = dh.getInputStream().read(b)) != -1;) {
					out.append(new String(b, 0, n));
				}
				
				log = out.toString();
			}
			
			if(log != null){
				System.out.println(log);
			}else{
				logger.error("Log is empty");
			}
		} catch (NullPointerException e) {
			logger.error("Log is empty");
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}finally{
			if(uid !=null) {
				udc.udclient.logout(uid);
			
		}
		
		
		
		}
		

		
	}

}
