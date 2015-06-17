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
import java.util.UUID;
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

public class SubmitMGMetaJob {

	final static Logger logger = LoggerFactory.getLogger(SubmitMGMetaJob.class);

	private static JobManager jmClient;
	private static String filename;
	private static String executable;
	private static String exe[];
	private static String dir[];	
	private static String args[];
	private static String directory;
	private static String argument;
	
	private static String arg=null;
	private static String sentence1="";
	private static String sentence2="";
	private static String sentence_dir="";
	
	
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
		options.addOption("a", "arg", true, "Arguments ");
		options.addOption("t", true, "A Estimated Max Job Time(sec)");



		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			HelpFormatter formatter = new HelpFormatter();

			// help message
			if (cmd.hasOption("h")|| cmd.hasOption("help") ) {
//				formatter.printHelp("\t Submit MadGraph5 Jobs", options);
				String help= "htcaas-mgjob-submit [OPTIONS] ";
				String syn= "\nhtcaas-mgjob-submit [-d or --dir <working directory> ] [-e or --exec <executable(s)>] [-a <arguments>] ";
				syn += "\nhtcaas-mgjob-submit -d {working dir | dir_1;dir_2} -e {executable | exe_1;exe_2} -a {arguments | args1;args2}";
				syn += "\n\nReport bugs to <htcaas-admin@kisti.re.kr>.";
				formatter.printHelp(250, help, "Submit a metajob for MadGraph5 (https://launchpad.net/mg5amcnlo)\n", options, syn);
				System.exit(0);
			} else {

				if(cmd.hasOption("e")||cmd.hasOption("exec")) { 
					executable = cmd.getOptionValue("e");					
				}
				if(cmd.hasOption("d")||cmd.hasOption("dir")) {
					directory = cmd.getOptionValue("d");
				}
				if(cmd.hasOption("a")||cmd.hasOption("arg")) {
					argument = cmd.getOptionValue("a");
				}
				if (cmd.hasOption("t")) {
					aMaxJobTimeMin = cmd.getOptionValue("t");
				}

			}
			
			if(application == null) {
				application="madgraph";
			}
			
			// if exe is null
			if (executable == null || executable.startsWith("$") || executable.isEmpty()) {
				System.out.println("-e Need executable name ");
				System.exit(1);
			}else if (executable.matches(".*:.*")) { 
				exe= executable.trim().split(":");
//				for (int i=0; i <exe.length; i++){
//					System.out.println( exe[i]);
//				}
			}
			
			// if output is null
			// if input is null
			if ((directory == null || directory.startsWith("$") || directory.isEmpty())) {
				System.out.println("Need Directory name for -d ");
				System.exit(1);
			}else if (directory.matches(".*:.*")) {
				dir= directory.trim().split(":");
			}
			
			if ((argument == null || argument.startsWith("$") || argument.isEmpty() || argument.matches("''"))) {
				argument=null;
			}else if (argument.matches(".*:.*")) {
				args = argument.trim().split(":");
			}
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
					e.printStackTrace();
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
		
		
		SubmitMGMetaJob sm = new SubmitMGMetaJob();
		String loc = System.getenv("HTCaaS_Client")+"/etc/";
//		String loc = "/usr/local/htc/HTCaaS_ext/client/etc/";
		int cnt = 1;


		String name_a;
		String name_b;
		// To get unique file name 
		String uuid = UUID.randomUUID().toString().replace("-", "");

// EXECUTABLE		
		if(executable.matches(".*:.*")) {
			for (int i=0; i < exe.length; i++){
				sentence1+="\n\t\t<ns3:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\">" + exe[i] +"</ns3:Value>";
				//System.out.println(sentence);
			}
		}else {
			sentence1="\n\t\t<ns3:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\">" + executable +"</ns3:Value>";
			//System.out.println(sentence);
		}
		
// DIRECTORY		
		if(directory.matches(".*:.*")) {
			for (int i=0; i < exe.length; i++){
				sentence_dir+="\n\t\t<ns3:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\">" + dir[i] +"</ns3:Value>";
			}
		}else {
			sentence_dir="\n\t\t<ns3:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\">" + directory +"</ns3:Value>";
		}
		
// ARGUMENT
		if(argument != null ) {
			
			arg="\n\t\t<ns2:Argument>ARG</ns2:Argument>"; 
			sentence2+="\n\t<ns3:Assignment>\n\t   <ns3:DocumentNode> <ns3:Match>ARG</ns3:Match></ns3:DocumentNode> \n\t     <ns3:Values>";
			
				if (argument.matches(".*:.*")){
					
					for (String a : args){
						a= a.replace("=", " ");
						sentence2+="\n\t\t<ns3:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\">" + a +"</ns3:Value>";				
					}
					sentence2+=" \n\t</ns3:Values> \n\t</ns3:Assignment>";
					
				} else {
					argument= argument.replace("=", " ");
	
					sentence2+="\n\t\t<ns3:Value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:string\">" + argument +"</ns3:Value>";
					sentence2+=" \n\t</ns3:Values> \n\t</ns3:Assignment>";
				}
				
			
		}  else {
			arg=" ";
			sentence2 =" ";
			//sentence2 =null;
		}
		
		String app;
		app=application+"2";
		application=application+"2";  //madgraph2
		application=application+"."+uuid;
		
		if(sm.FileContentCoverter(loc+app+".jsdl.temp", "%TEMPDIR%", sentence_dir, loc+application+".jsdl.temp."+cnt)){ 

			    if(input != null) { name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;			    	 
			    					sm.FileContentCoverter(name_a, "%INPUT%", input,name_b) ;}
			    if(output != null){ name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;
			    					sm.FileContentCoverter(name_a, "%OUTPUT%", output, name_b);}
			    if(subjob != null){	name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;
			    					sm.FileContentCoverter(name_a, "%RNUM%", subjob, name_b) ;}	
			    
			   {	name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;
				sm.FileContentCoverter(name_a, "%ARGS1%", arg, name_b) ; // argument definition
			    	name_a= loc+application+".jsdl.temp."+(cnt++); name_b= loc+application+".jsdl.temp."+cnt;
				sm.FileContentCoverter(name_a, "%ARGS2%", sentence2, name_b) ;} // argument
			    
				name_a= loc+application+".jsdl.temp."+cnt; 

				if(sm.FileContentCoverter(name_a, "%TEMPEXE%", sentence1, loc+application+".jsdl")){
							filename = loc+application+".jsdl";
							File f_temp = new File(filename) ;
							f_temp.setExecutable(true);
							f_temp.setReadable(true);
							f_temp.setWritable(true);
							
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
			result = jmClient.submitMetaJob(user, sb.toString(), 36000, projectName, scriptName);
		}
		if (result.containsKey(1)){
			System.out.println(result.get(1));
			// Delete temporary jsdls
			//String s = loc + application +".jsdl";
			for (int n=0; n <=cnt ; n++ ) {
				String s = loc + application +".jsdl";
				if(n!=0){
					s= s+".temp."+n;
				}
		  	  
				File f = new File(s) ;				
				try {
				f.delete();			
				}catch (SecurityException se){	}
			}
		}
		else
			System.out.println("MetaJob submit failed.! submit error:" + result.get(0));
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
