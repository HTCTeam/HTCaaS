package org.kisti.htc.jobmanager.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

// http://cxf.apache.org/docs/aegis-databinding-20x.html
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;

public class Server {

  //final static Logger logger = LoggerFactory.getLogger(Server.class);
  final static mLogger logger = mLoggerFactory.getLogger("JM");

  private static String JobManagerURL;
  private static String SSLServerPath;
  private static String SSLServerPassword;
  private static String SSLCAPath;
  private static String SSLCAPassword;
  private static boolean SSL = false;

  // constuctor
  protected Server() throws Exception {

    try {
    Properties prop = new Properties();

    // Loading the HTCaaS Server Configuration file
    prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));

    /*
     *  Set the JobManagerURL where the service will be running on.
     *  e.g., JobManager.Service=http://0.0.0.0:9001/JobManager
     */
    JobManagerURL = prop.getProperty("JobManager.Service");

    // Setup the SSL Authentication Mechanism if needed
    if (prop.getProperty("SSL.Authentication").equals("true")){
        SSL = true;
        JobManagerURL = JobManagerURL.replace("http", "https");
        SSLServerPath = prop.getProperty("SSL.Server.Keystore.Path");
        SSLServerPassword = prop.getProperty("SSL.Server.Keystore.Password");
        SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
        SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
      }

      logger.debug("JobManagerURL: "+ JobManagerURL);

    } catch (Exception e) {
      logger.error("Failed to load config file: " + e.getMessage());
      System.exit(1);
    }
    
    System.setProperty("jdk.xml.entityExpansionLimit", "0");

    /* Create a JobManager instance and bind it to the web service */
    JobManager jobManagerImpl = new JobManagerImpl();
    /*
     * ServerFactoryBean – Making a Service Endpoint (Apache CXF):
     * The ServerFactoryBean produces a Server instance for you.
     * It requires a service class and an address to publish the service on.
     * By creating a Server you'll have started your service and made it available to the outside world.
     */
    ServerFactoryBean svrFactory = new ServerFactoryBean();
    svrFactory.setServiceClass(JobManager.class);
    svrFactory.setAddress(JobManagerURL);
    svrFactory.setServiceBean(jobManagerImpl);
    svrFactory.setDataBinding(new AegisDatabinding());

    if (SSL) {
      int port = Integer.parseInt(JobManagerURL.split(":")[2].split("/")[0]);
      svrFactory = configureSSLOnTheServer(svrFactory, port);
    }

    svrFactory.create();
    
  }

/// SSL {{{
  private ServerFactoryBean configureSSLOnTheServer(ServerFactoryBean sf, int port) {
    try {
      TLSServerParameters tlsParams = new TLSServerParameters();
      KeyStore keyStore = KeyStore.getInstance("JKS");
      File truststore = new File(SSLServerPath);
      keyStore.load(new FileInputStream(truststore), SSLServerPassword.toCharArray());
      KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyFactory.init(keyStore, SSLServerPassword.toCharArray());
      KeyManager[] km = keyFactory.getKeyManagers();
      tlsParams.setKeyManagers(km);
      truststore = new File(SSLCAPath);
      keyStore.load(new FileInputStream(truststore), SSLCAPassword.toCharArray());
      TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustFactory.init(keyStore);
      TrustManager[] tm = trustFactory.getTrustManagers();
      tlsParams.setTrustManagers(tm);
      // FiltersType filter = new FiltersType();
      // filter.getInclude().add(".*_EXPORT_.*");
      // filter.getInclude().add(".*_EXPORT1024_.*");
      // filter.getInclude().add(".*_128_.*");
      // filter.getInclude().add(".*_WITH_DES_.*");
      // filter.getInclude().add(".*_WITH_NULL_.*");
      // filter.getExclude().add(".*_DH_anon_.*");
      // tlsParams.setCipherSuitesFilter(filter);
      // ClientAuthentication ca = new ClientAuthentication();
      // ca.setRequired(true);
      // ca.setWant(true);
      // tlsParams.setClientAuthentication(ca);
      JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
      factory.setTLSServerParametersForPort(port, tlsParams);

    } catch (KeyStoreException kse) {
      System.out.println("KeyStoreSecurity configuration failed with the following: " + kse.getCause());

    } catch (NoSuchAlgorithmException nsa) {
      System.out.println("NoSuchAlgoSecurity configuration failed with the following: " + nsa.getCause());

    } catch (FileNotFoundException fnfe) {
      System.out.println("FileNotSecurity configuration failed with the following: " + fnfe.getCause());

    } catch (UnrecoverableKeyException uke) {
      System.out.println("UnrecoverSecurity configuration failed with the following: " + uke.getCause());

    } catch (GeneralSecurityException gse) {
      System.out.println("GeneralSecurity configuration failed with the following: " + gse.getCause());

    } catch (IOException ioe) {
      System.out.println("IOSecurity configuration failed with the following: " + ioe.getCause());

    }

    return sf;
  }
/// SSL }}}

  // jobmanager run main
  public static void main(String[] args) throws Exception {

    // start jobmanager
    new Server();
    System.out.println("JobManager Server ready...");
    

    try {
      while(true) {
        Thread.sleep(60 * 60 * 1000);
      }
    } catch (InterruptedException e) {
    }

    System.out.println("JobManager Server exiting");
    System.exit(0);
  }

}
