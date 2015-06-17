package org.kisti.htc.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.UUID;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBElement;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.ggf.schemas.jsdl._2005._11.jsdl.ApplicationType;
import org.ggf.schemas.jsdl._2005._11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl._2005._11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl._2005._11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl._2005._11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl._2005._11.jsdl.ObjectFactory;
import org.ggf.schemas.jsdl._2005._11.jsdl.SourceTargetType;
import org.ggf.schemas.jsdl._2005._11.jsdl_posix.ArgumentType;
import org.ggf.schemas.jsdl._2005._11.jsdl_posix.FileNameType;
import org.ggf.schemas.jsdl._2005._11.jsdl_posix.POSIXApplicationType;
import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.jobmanager.server.JobManager;
import org.kisti.htc.udmanager.client.UDClient;


import org.ogf.schemas.jsdl._2009._03.sweep.Assignment;
import org.ogf.schemas.jsdl._2009._03.sweep.DirectoryType;
import org.ogf.schemas.jsdl._2009._03.sweep.DocumentNodeType;
import org.ogf.schemas.jsdl._2009._03.sweep.SweepType;
import org.ogf.schemas.jsdl._2009._03.sweep.ValuesType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AutodockMain {

	private static final Logger logger = LoggerFactory.getLogger(AutodockMain.class);
	
	private static final String userName = "seungwoo";
//	private static final String appName = "autodock3";
//	private static final String projectName = "ScalabilityTest-110530";
	
	private static UDClient udc;
	private static JobManager jmClient;
	private static String JobManagerURL;
	private static String FTPAddress;
	
	public AutodockMain() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

//			DBManagerURL = prop.getProperty("DBManager.Address");
//			logger.info("DBManagerURL: {}", DBManagerURL);
			
			JobManagerURL = prop.getProperty("JobManager.Address");
			logger.info("JobManagerURL: {}", JobManagerURL);
			
			FTPAddress = prop.getProperty("FTP.Address");
			logger.info("FTP Address: {}", FTPAddress);
		} catch (Exception e) {
			logger.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
		// UDManager client (CXF)
		udc = new UDClient();
         
		// DBManager client (CXF)
//        ClientProxyFactoryBean dbFactory = new ClientProxyFactoryBean();
//        dbFactory.setServiceClass(Database.class);
//        dbFactory.setAddress(DBManagerURL);
//        dbclient = (Database)dbFactory.create();

        // JobManager client (CXF)
        ClientProxyFactoryBean jobFactory = new ClientProxyFactoryBean();
        jobFactory.setServiceClass(JobManager.class);
        jobFactory.setAddress(JobManagerURL);
        jobFactory.setDataBinding(new AegisDatabinding());
        jmClient = (JobManager)jobFactory.create();
	}
	
	public void uploadFiles(int arg) {
		UUID uid = null;
		try{
			uid = udc.udclient.login(FTPAddress, "seungwoo", "shtmddn", 0);
		}catch(SocketTimeoutException e){
			logger.error(e.toString());
			return ;
		}
		
	    // Protein	    
	    try {
			udc.putFile(uid, "/usr/local/proteins/2QMJ_new.tar.bz2", "/home/seungwoo/proteins", 0);
		} catch (Exception e) {
			logger.error("upload error " + e.getMessage());
		}

	    File ligandDir = null;
	    // Ligands
	    if(arg == 5)
	    	ligandDir = new File("/usr/local/ligands/pdb.5");
	    else if(arg == 10)
	    	ligandDir = new File("/usr/local/ligands/pdb.10");
	    
	    else if(arg == 105)
	    	ligandDir = new File("/usr/local/ligands/pdb.105");
	    
	    else if(arg == 3000)
	    	ligandDir = new File("/usr/local/ligands/pdb.3000");
	    
	    else if(arg == 15000)
	    	ligandDir = new File("/usr/local/ligands/pdb.15000");
	    else {
	    	System.out.println("wrong number!");
	    	System.exit(1);
	    }
	    
	    for(File file : ligandDir.listFiles()) {	    	
	    	try {
				udc.putFile(uid, file.getAbsolutePath(), "/home/seungwoo/ligands", 0);				
			} catch (Exception e) {
				logger.error("upload error " + e.getMessage());
			}		
	    }
	    
	    // Application
	    File appDir = new File("/usr/local/autodock3_test");
	    File[] appFiles = appDir.listFiles();
	    for(File file : appFiles) {
	    	try {	    		
	    		udc.putFile(uid, file.getAbsolutePath(), "/home/seungwoo/autodock3", 0);
			} catch (Exception e) {
				logger.error("upload error " + e.getMessage());
			}
	    }	
	    
	}
		
//	public void getProgress() {
//		int metaJobID = 1;
//		Date startTimestamp = dbclient.getMetaJobStartTime(metaJobID);
//		
//		System.out.println(dbclient.getMetaJobProgress(metaJobID));
//		
//		System.out.println(startTimestamp);
//		System.out.println(progress);
//	}
	
	public static void main(String[] args) {
	   				
		AutodockMain am = new AutodockMain();
		
//		am.getProgress();
//		System.exit(1);
		
//		int arg = Integer.parseInt(args[0]);
		int arg = 5;
        am.uploadFiles(arg);

        // Create MetaJob Document (XML)
		ObjectFactory of = new ObjectFactory();
		org.ggf.schemas.jsdl._2005._11.jsdl_posix.ObjectFactory of_POSIX = new org.ggf.schemas.jsdl._2005._11.jsdl_posix.ObjectFactory();	
    	org.ogf.schemas.jsdl._2009._03.sweep.ObjectFactory of_SWEEP = new org.ogf.schemas.jsdl._2009._03.sweep.ObjectFactory();

    	// JobDefinition
    	JobDefinitionType jdef = of.createJobDefinitionType();
    	
    	// JobDescription
    	JobDescriptionType jdesc = of.createJobDescriptionType();
    	
    	// Application
    	ApplicationType appl = of.createApplicationType();
    	appl.setApplicationName("autodock3");
    	
    	POSIXApplicationType papp = of_POSIX.createPOSIXApplicationType();
    	
    	FileNameType fileName = of_POSIX.createFileNameType();
    	fileName.setValue("autodock3.sh");
    	papp.setExecutable(fileName);
    	
    	ArgumentType arg1 = of_POSIX.createArgumentType();
    	arg1.setValue("TARGET");
    	papp.getArgument().add(arg1);
    	
    	ArgumentType arg2 = of_POSIX.createArgumentType();
    	arg2.setValue("LIGAND");
    	papp.getArgument().add(arg2);
    	
    	appl.setPOSIXApplication(papp);
    	
    	jdesc.setApplication(appl);
    	
    	// DataStaging
    	DataStagingType ds = of.createDataStagingType();
    	ds.setFileName("TARGET.tar.bz2");
    	ds.setCreationFlag(CreationFlagEnumeration.OVERWRITE);
    	SourceTargetType src = of.createSourceTargetType();
    	src.setURI("/home/seungwoo/proteins/TARGET.tar.bz2");
    	ds.setSource(src);
    	jdesc.getDataStaging().add(ds);
    	
    	
    	ds = of.createDataStagingType();
    	ds.setFileName("LIGAND.pdbq");
    	src = of.createSourceTargetType();
    	src.setURI("/home/seungwoo/ligands/LIGAND.pdbq");
    	ds.setSource(src);
    	jdesc.getDataStaging().add(ds);
    	    	
    	
	    // Application
	    File appDir = new File("/usr/local/autodock3_test");
	    File[] appFiles = appDir.listFiles();
	    for(File file : appFiles) {
	    	ds = of.createDataStagingType();
	    	ds.setFileName(file.getName());
	    	src = of.createSourceTargetType();
	    	src.setURI("/home/seungwoo/autodock3/" + file.getName());
	    	ds.setSource(src);	    		
	    	jdesc.getDataStaging().add(ds);
	    }	
    	

    	ds = of.createDataStagingType();
    	ds.setFileName("LIGAND_TARGET.dlg");
    	SourceTargetType target = of.createSourceTargetType();
    	target.setURI("/home/seungwoo/output/LIGAND.TARGET.dlg");
    	ds.setTarget(target);
    	jdesc.getDataStaging().add(ds);
    	
    	ds = of.createDataStagingType();
    	ds.setFileName("LIGAND_TARGET.coordinates");
    	target = of.createSourceTargetType();
    	target.setURI("/home/seungwoo/output/LIGAND.pdbq");
    	ds.setTarget(target);
    	jdesc.getDataStaging().add(ds);
    	
    	jdef.setJobDescription(jdesc);
    	
    	
    	SweepType sweep = of_SWEEP.createSweepType();
    	Assignment asm = of_SWEEP.createAssignment();
    	
    	DocumentNodeType dn1 = of_SWEEP.createDocumentNodeType();
    	dn1.setMatch("TARGET");
    	    	
    	asm.getParameter().add(of_SWEEP.createDocumentNode(dn1));

    	ValuesType values = of_SWEEP.createValuesType();
    	values.getValue().add("2QMJ_new");
    	    	
    	asm.setFunction(of_SWEEP.createValues(values));
    	
    	sweep.getAssignment().add(asm);
    	
    	
    	SweepType sweep1 = of_SWEEP.createSweepType();
    	
    	Assignment asm1 = of_SWEEP.createAssignment();
    	
    	DocumentNodeType dn2 = of_SWEEP.createDocumentNodeType();
    	dn2.setMatch("LIGAND");
    	    	
    	asm1.getParameter().add(of_SWEEP.createDocumentNode(dn2));

    	DirectoryType dir = of_SWEEP.createDirectoryType();
    	dir.setFilenameonly(true);
    	dir.setName("/home/seungwoo/ligands");
    	
    	asm1.setFunction(of_SWEEP.createDirectory(dir));
    
    	sweep1.getAssignment().add(asm1);
    	    	
    	sweep.getSweep().add(sweep1);
    	
    	jdef.setSweep(sweep);
    	       
        // create an element for marshalling
        JAXBElement<JobDefinitionType> jdefElement = of.createJobDefinition(jdef);

        // create a Marshaller and marshal to File
        JAXB.marshal( jdefElement, new File("tmp/autodock3.jsdl") );
        
        
        
        // Submit a MetaJob
        StringBuffer sb = new StringBuffer();
        
		try {
			BufferedReader br = new BufferedReader(new FileReader("tmp/autodock3.jsdl"));
	        String line = "";
	        while((line = br.readLine()) != null ) {
	        	sb.append(line + "\n");
	        }
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
               
		jmClient.submitMetaJob(userName, sb.toString(),0,"","", "");


	}
	
}