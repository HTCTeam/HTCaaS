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
import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

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

public class SubmitMGMetaJob2 {

	final static Logger logger = LoggerFactory.getLogger(SubmitMGMetaJob2.class);

	private static JobManager jmClient;
	private static String filename;
	private static String executable;
	private static String directory;
	
	private static String input;
	private static String output;
	private static String user;
	private static String subjob;
	private static String JobManagerURL;
	private static String aMaxJobTimeMin ;
	private static String projectName;
	private static String scriptName;
	private static String application;

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
		| Usage: ./htcaas-mgjob-submit 
		| -h         Print help
		| -e <arg>   Executable File Name
		| -i <arg>   input File Name
		| -u <arg>   Set user id 
		*/
		Options options = new Options();
		
		options.addOption("h", "help", false, "Print help");
		options.addOption("e", "exec", true,  "Excutable File Name");
//		options.addOption("i", "in"  , true,  "Input name, <Default> is madgraph.zip");
//		options.addOption("o", "out" , true,  "Output name, <Default> is madevent.tar.gz");
		options.addOption("d", "dir", true, "Init Directory ");
//		options.addOption("u", "usr" , true,  "Set User id");
//		options.addOption("v", "var" , true,  "Arguement (e.g.# of Sub-job(count)) ");
//		options.addOption("t", true,  "A Estimated Max Job Time(sec)");
//		options.addOption("a", "app", true, "Choose application (general/madgraph)");
		


		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			// help message
			if (cmd.hasOption("h")) {
				formatter.printHelp("htcaas-mgjob-submit -d {working dir} -e {executable} \t Submit MadGraph5 Jobs", options);
				System.exit(0);
			} else {
				// jsdl file path
//				if (cmd.hasOption("f")) {
//					filename = cmd.getOptionValue("f");
//				}
				if(cmd.hasOption("e")||cmd.hasOption("exec")) { 
					executable = cmd.getOptionValue("e");
					
				}
				if(cmd.hasOption("d")||cmd.hasOption("dir")) {
					directory = cmd.getOptionValue("d");
				}

			}
			
			if(application == null) {
				application="madgraph";
			}
			
			// if exe is null
			if (executable == null || executable.startsWith("$") || executable.isEmpty()) {
				logger.error("Need -e option and value(executable name) ");
				System.exit(1);
			}
			
			// if output is null
			// if input is null
			if ((directory == null || directory.startsWith("$") || directory.isEmpty())) {
//				System.out.println("-i Need executable name ");
//				System.exit(1);
				//Default 
//				directory = "madevent.tar.gz";
				logger.error("Need Directory name for -d ");
				System.exit(1);
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
		

		Map<Integer, String> result = null;
		
		
		// Configure JSDL according to option user put
		
		
		SubmitMGMetaJob2 sm = new SubmitMGMetaJob2();
//		String loc = "$HTCaaS_Client/etc/";
		String loc = "/usr/local/htc/HTCaaS_ext/client/etc/";
		int cnt = 1;

		//System.out.println(executable);
		String name_a;
		String name_b;
		
		
		if(sm.FileContentCoverter(loc+application+".jsdl.temp", "%TEMPDIR%", directory, loc+application+".jsdl.temp."+cnt)){ 
//System.out.println(cnt);
//			    if(input != null) { name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;			    	 
//			    					sm.FileContentCoverter(name_a, "%INPUT%", input,name_b) ;}
//			    if(output != null){ name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;
//			    					sm.FileContentCoverter(name_a, "%OUTPUT%", output, name_b);}
//			    if(subjob != null){	name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;
//			    					sm.FileContentCoverter(name_a, "%RNUM%", subjob, name_b) ;}
				name_a= loc+application+".jsdl.temp."+cnt; 

				if(sm.FileContentCoverter(name_a, "%TEMPEXE%", executable, loc+application+".jsdl")){
							filename = loc+application+".jsdl";
							
						}								 
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
		
		
		// submit meta job using jmClient
		if(aMaxJobTimeMin!=null){
			
			result = jmClient.submitMetaJob(user, sb.toString(), Integer.parseInt(aMaxJobTimeMin), projectName, scriptName);
		}else{
			//default estimated job runTime = 100sec  
			result = jmClient.submitMetaJob(user, sb.toString(), 100,projectName, scriptName);
		}
		if (result.containsKey(1)){
			System.out.println(result.get(1));
		}
		else
			logger.error("MetaJob submit failed.! submit error:" + result.get(0));
	}
	
	
	// Substitution for JSDL 
	public  boolean FileContentCoverter(String fileName, String origin, String replace, String out_file ){
		
				File inputFile = new File(fileName);
				File outputFile = new File(out_file); 
				FileInputStream fileInputStream = null;
				
				BufferedReader bufferedReader = null;
				FileOutputStream fileOutputStream = null;
				BufferedWriter bufferedWriter = null;
				boolean result = false;
				
				try {
					   // FileInputStream,FileOutputStream, BufferdReader, BufferedWriter
					
					   fileInputStream = new FileInputStream(inputFile);
					   fileOutputStream = new FileOutputStream(outputFile);
					   bufferedReader = new BufferedReader(new InputStreamReader(
					     fileInputStream));
					   bufferedWriter = new BufferedWriter(new OutputStreamWriter(
					     fileOutputStream));
					  
					   String line;			  
					   String repLine;
					  
					   String originalString = origin;
					   String replaceString = replace;
					 
					   while ((line = bufferedReader.readLine()) != null) {
					  
					    repLine = line.replaceAll(originalString, replaceString);
					    
					    bufferedWriter.write(repLine, 0, repLine.length());
					    bufferedWriter.newLine();
					   }
					  
					   result = true;
					  } catch (IOException ex) {
					   ex.printStackTrace();
					  } finally {
					 
					   try {
					    bufferedReader.close();
					   } catch (IOException ex1) {
					    ex1.printStackTrace();
					   }
					   try {
					    bufferedWriter.close();
					   } catch (IOException ex2) {
					    ex2.printStackTrace();
					   }
					   
					   if (result) {
						  //System.out.println("result:"+result);
					      // inputFile.delete();
						   inputFile.setExecutable(true);
						   inputFile.setReadable(true);
						   inputFile.setWritable(true);
						   outputFile.setExecutable(true);
						   outputFile.setReadable(true);
						   outputFile.setWritable(true);
					   // outputFile.renameTo(new File(fileName));
					   }
					  }
					 
			
		 return result;
			
		 
	}

}
