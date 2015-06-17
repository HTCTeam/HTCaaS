package org.kisti.htc.dbmanager.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.cxf.transport.http_jetty.ThreadingParameters;
import org.apache.cxf.transports.http_jetty.configuration.ThreadingParametersIdentifiedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.mLogger;
import util.mLoggerFactory;

/**
* Database Manager Server
*/
public class Server {

  // for logging
  final static Logger logger = LoggerFactory.getLogger(Server.class);
//  final static mLogger logger = mLoggerFactory.getLogger("DB");

  private static String DBManagerURL;

  // SSL
  private static String SSLServerPath;
  private static String SSLServerPassword;
  private static String SSLCAPath;
  private static String SSLCAPassword;
  private static boolean SSL = false;

  protected Server() throws Exception {

    System.out.println("Server .......................... logger : " + logger);
    //System.out.println(logger);

    try {
      // 설정 파일 읽기
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Server.conf"));

      DBManagerURL = prop.getProperty("DBManager.Service");

      if (prop.getProperty("SSL.Authentication").equals("true")) {
        SSL = true;
        DBManagerURL = DBManagerURL.replace("http", "https");
        SSLServerPath = prop.getProperty("SSL.Server.Keystore.Path");
        SSLServerPassword = prop.getProperty("SSL.Server.Keystore.Password");
        SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
        SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
      }
      System.out.println("DBManagerURL: " + DBManagerURL);

    } catch (Exception e) {
      System.out.println("Failed to load config file: " + e.getMessage());
      System.exit(1);
    }

    // 서비스 팩토리 빈
    ServerFactoryBean svrFactory = new ServerFactoryBean();

    // Database는 인터페이스 클래스
    svrFactory.setServiceClass(Database.class);

    // 서비스 주소를 설정
    svrFactory.setAddress(DBManagerURL);

    // 서비스 구현 클래스 = DatabaseImpl
    svrFactory.setServiceBean(new DatabaseImpl());

    // 바인딩 설정 (이건 왜하는지 알수 없지만..)
    svrFactory.setDataBinding(new AegisDatabinding());

    if (SSL) {
      int port = Integer.parseInt(DBManagerURL.split(":")[2].split("/")[0]);
      svrFactory = configureSSLOnTheServer(svrFactory, port);
    }

    svrFactory.create();
  }

  
  /**
   * Thread 관련 설정
   */
  private ServerFactoryBean configureThreadOnTheServer(ServerFactoryBean sf){
//    logger.info("setting threading");
//    svrFactory = configureThreadOnTheServer(svrFactory);
//    ThreadingParameters tp = new ThreadingParameters();
//    tp.setMaxThreads(1000);
//    tp.setMinThreads(10);
//    Map<String, ThreadingParameters> tpMap = new HashMap<String, ThreadingParameters>();
//    tpMap.put(DBManagerURL, tp);
//    JettyHTTPServerEngineFactory jettyFactory = new JettyHTTPServerEngineFactory();
//    jettyFactory.setThreadingParametersMap(tpMap);
    
    return sf;
  }
  
  
  /**
  * SSL 관련 설정
  */
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


  /**
  * Database Manager main 함수
  */
  public static void main(String[] args) throws Exception {

    // start dbmanager
    new Server();

    logger.info("DBManager Server ready...");

    try {
      while (true) {
        Thread.sleep(60 * 60 * 1000);
      }
    } catch (InterruptedException e) {
    }

    logger.info("DBManager Server exiting");

    System.exit(0);
  }

}
