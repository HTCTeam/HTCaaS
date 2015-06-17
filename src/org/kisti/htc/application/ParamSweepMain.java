package org.kisti.htc.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
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
import org.kisti.htc.udmanager.client.UDClient;

import org.kisti.htc.dbmanager.server.Database;
import org.kisti.htc.jobmanager.server.JobManager;

import org.ogf.schemas.jsdl._2009._03.sweep.Assignment;
import org.ogf.schemas.jsdl._2009._03.sweep.DocumentNodeType;
import org.ogf.schemas.jsdl._2009._03.sweep.LoopIntegerType;
import org.ogf.schemas.jsdl._2009._03.sweep.SweepType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamSweepMain {

	private static final Logger log = LoggerFactory
			.getLogger(AutodockMain.class);

	private static final String userName = "seungwoo";
//	private static final String appName = "optimization";
//	private static final String projectName = "test-20101019";

	private static UDClient udc;
//	private static Database dbclient;
	private static JobManager jmClient;
	private static String DBManagerURL;
	private static String JobManagerURL;
	private static String FTPAddress;

	public ParamSweepMain() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

//			DBManagerURL = prop.getProperty("DBManager.Address");
//			log.info("DBManagerURL: {}", DBManagerURL);

			JobManagerURL = prop.getProperty("JobManager.Address");
			log.info("JobManagerURL: {}", JobManagerURL);

			FTPAddress = prop.getProperty("FTP.Address");
			log.info("FTP Address: {}", FTPAddress);
		} catch (Exception e) {
			log.error("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}

		// UDManager client (CXF)
		udc = new UDClient();

		
//		ClientProxyFactoryBean dbFactory = new ClientProxyFactoryBean();
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

	public void uploadFiles() {
		
		UUID uid = null;
		try{
			uid = udc.udclient.login(FTPAddress, "seungwoo", "shtmddn", 0);
		}catch(SocketTimeoutException e){
			log.error(e.toString());
			return ;
		}
				

		// InputData
		try {
			udc.putFile(uid, "/usr/local/jeongmj_input/5d-hp-new.para",
					"/home/seungwoo/optimization", 0);
		} catch (Exception e) {
			log.error("upload error " + e.getMessage());
		}

		// Application
		File appDir = new File("/usr/local/jeongmj");
		File[] appFiles = appDir.listFiles();
		for (File file : appFiles) {
			try {
				udc.putFile(uid, file.getAbsolutePath(),
						"/home/seungwoo/optimization", 0);
			} catch (Exception e) {
				log.error("upload error " + e.getMessage());
			}
		}
	}

	public static void main(String[] args) {

		ParamSweepMain psm = new ParamSweepMain();

		psm.uploadFiles();

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
		appl.setApplicationName("optimization");

		POSIXApplicationType papp = of_POSIX.createPOSIXApplicationType();

		FileNameType fileName = of_POSIX.createFileNameType();
		fileName.setValue("run.sh");
		papp.setExecutable(fileName);

		ArgumentType arg1 = of_POSIX.createArgumentType();
		arg1.setValue("5d-hp-new.para");
		papp.getArgument().add(arg1);

		ArgumentType arg2 = of_POSIX.createArgumentType();
		arg2.setValue("EXPNUM");
		papp.getArgument().add(arg2);

		appl.setPOSIXApplication(papp);

		jdesc.setApplication(appl);

		// DataStaging
		DataStagingType ds = of.createDataStagingType();
		ds.setFileName("5d-hp-new.para");
		ds.setCreationFlag(CreationFlagEnumeration.OVERWRITE);
		SourceTargetType src = of.createSourceTargetType();
		src.setURI("/home/seungwoo/optimization/5d-hp-new.para");
		ds.setSource(src);
		jdesc.getDataStaging().add(ds);

		// Application
		File appDir = new File("/usr/local/jeongmj");
		File[] appFiles = appDir.listFiles();
		for (File file : appFiles) {
			ds = of.createDataStagingType();
			ds.setFileName(file.getName());
			src = of.createSourceTargetType();
			src.setURI("/home/seungwoo/optimization/" + file.getName());
			ds.setSource(src);
			jdesc.getDataStaging().add(ds);
		}

		ds = of.createDataStagingType();
		ds.setFileName("5d-hp-new.para.kEXPNUM");
		SourceTargetType target = of.createSourceTargetType();
		target.setURI("/home/seungwoo/optimization/5d-hp-new.para.kEXPNUM");
		ds.setTarget(target);
		jdesc.getDataStaging().add(ds);

		jdef.setJobDescription(jdesc);

		SweepType sweep = of_SWEEP.createSweepType();
		Assignment asm = of_SWEEP.createAssignment();

		DocumentNodeType dn1 = of_SWEEP.createDocumentNodeType();
		dn1.setMatch("EXPNUM");

		asm.getParameter().add(of_SWEEP.createDocumentNode(dn1));

		LoopIntegerType li = of_SWEEP.createLoopIntegerType();
		li.setStart(BigInteger.valueOf(1));
		li.setEnd(BigInteger.valueOf(5));
		li.setStep(BigInteger.valueOf(1));

		asm.setFunction(of_SWEEP.createLoopInteger(li));

		sweep.getAssignment().add(asm);

		jdef.setSweep(sweep);

		// create an element for marshalling
		JAXBElement<JobDefinitionType> jdefElement = of
				.createJobDefinition(jdef);

		// create a Marshaller and marshal to File
		JAXB.marshal(jdefElement, new File("optimization.jsdl"));

		// Submit a MetaJob
		StringBuffer sb = new StringBuffer();

		try {
			BufferedReader br = new BufferedReader(new FileReader(
					"optimization.jsdl"));
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		jmClient.submitMetaJob(userName, sb.toString(),0, "", "", "");

	}
}