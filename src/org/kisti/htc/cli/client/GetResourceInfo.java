
package org.kisti.htc.cli.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.kisti.htc.dbmanager.beans.CE;
import org.kisti.htc.dbmanager.beans.ServiceInfra;
import org.kisti.htc.dbmanager.beans.User;
import org.kisti.htc.monitoring.server.Monitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GetResourceInfo {

	final static Logger logger = LoggerFactory.getLogger(GetResourceInfo.class);

	private static Monitoring monitoring;
	private static String MonitoringURL;
	private static String userId = null;
	private static String si = null;

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
			if (userId == null) {
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
							userId = line;
						}
						br.close();

						if (userId == null) {
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

	private static void printCEInfo(String sid){
		List<CE> ceList = monitoring.getCEObjectList(Integer.parseInt(sid), true, false);
		System.out.printf("%83s\n","=====================================================================================");
		System.out.printf("%3s %55s | %10s | %10s \n", "NO", "NAME", "FREE CPU", "TOTAL CPU" );
		System.out.println("-------------------------------------------------------------------------------------");
		int fCPU = 0;
		int tCPU = 0;
		int id = 1;
		for(CE ce : ceList){
			System.out.printf("%3d %55s | %10d | %10d \n",id, ce.getName(), ce.getFreeCPU(),ce.getTotalCPU());
			fCPU += ce.getFreeCPU();
			tCPU += ce.getTotalCPU();
			id++;
		}
		System.out.println("-------------------------------------------------------------------------------------");
		System.out.printf(" %58s | %10d | %10d \n", "SUM", fCPU, tCPU );
		System.out.println("-------------------------------------------------------------------------------------");
	}
	
	public static void main(String[] args) {


		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("a", false, "Show all the resource informatin of service infras that a user has ");
		options.addOption("s", true, "Set service infra ");
		options.addOption("i", true, "Set user info");
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;

		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		factory.setServiceClass(Monitoring.class);
		factory.setAddress(MonitoringURL);
		factory.setDataBinding(new AegisDatabinding());
		monitoring = (Monitoring) factory.create();
		
		try {
			
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			if (cmd.hasOption("h")) {
//				formatter.printHelp("GetJobStatus", options);
				String help= "htcaas-resource-info [OPTIONS] ";
				String syn= "\nhtcaas-resource-info [-a] [-s <service infra name>] [-i <user id> ] ";
				String tmp = "";
				for(ServiceInfra si : monitoring.getServiceInfraObjects()){
					if(si.isAvailable()){
						tmp += " " + si.getName();
					}
				}
				syn += "\n *** Available Service Infras on HTCaaS :" + tmp + "\n";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Get the resource info \n", options, syn);
				System.exit(0);
			} 
			
			if(cmd.hasOption("i")) {
				userId = cmd.getOptionValue("i");
			}

			User user = monitoring.getUserInfo(userId);
			
			if(cmd.hasOption("a")){
				for(String sid : user.getServiceInfraID().split(",")){
					System.out.println("<" + monitoring.getServiceInfraName(Integer.parseInt(sid)) + ">");
					printCEInfo(sid);
				}
				
			} else if(cmd.hasOption("s")) {
				si = cmd.getOptionValue("s");
				String sid = ""+monitoring.getServiceInfraId(si);
				printCEInfo(sid);

			} else {
				String help= "htcaas-resource-info [OPTIONS] ";
				String syn= "\nhtcaas-resource-info [-a] [-s <service infra name>] [-i <user id> ] ";
				String tmp = "";
				for(ServiceInfra si : monitoring.getServiceInfraObjects()){
					if(si.isAvailable()){
						tmp += " <" + si.getName() + ">";
					}
				}
				syn += "\n *** Available Service Infras on HTCaaS :" + tmp + "\n";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Get the resource info \n", options, syn);
				System.exit(0);
			}

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

}
