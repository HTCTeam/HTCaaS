package org.kisti.htc.udmanager.client;

import java.io.FileInputStream;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.kisti.htc.udmanager.server.UserDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UDTest {

  final static Logger logger = LoggerFactory.getLogger(UDTest.class);
  private static UserDataManager udclient;
  private static String UDManagerURL;
  private static String FTPAddress;
  private static ClientProxyFactoryBean udFactory;
  
  
  private  String  id = "p258rsw";
  private String pw = "kisti4001!@#";
  
//  private String id = "seungwoo";
//  private String pw = "shtmddn";
  
  private static int no = 1;
  /**
   * @param args
   */
  
  public UDTest(){
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));
      UDManagerURL = prop.getProperty("UDManager.Address");
      FTPAddress = prop.getProperty("FTP.Address");
    } catch (Exception e) {
      logger.error("Failed to load config file: " + e.getMessage());
    }
    Map<String, Object> props = new HashMap<String, Object>();
    props.put("mtom-enabled", Boolean.TRUE);

    udFactory = new ClientProxyFactoryBean();
    udFactory.setProperties(props);
    udFactory.setServiceClass(UserDataManager.class);
    udFactory.setAddress(UDManagerURL);

    udclient = (UserDataManager) udFactory.create();
    
    HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(udclient).getConduit();
    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
    httpClientPolicy.setConnectionTimeout(120000);
    httpClientPolicy.setReceiveTimeout(180000);
    httpConduit.setClient(httpClientPolicy);
  }
  class UDThread extends Thread{
    
    int id2;
    
    public UDThread(int id2){
      this.id2 = id2;
    }
    @Override
    public void run(){
      logger.info("login " + id2);
      UUID uid = null;
      try {
        uid = udclient.login(FTPAddress, id, pw, id2);
      } catch (SocketTimeoutException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      try {
        Thread.sleep(60000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        logger.error("ie aid : " + id2 +", "+ e.toString());
      } catch (Exception e){
        logger.error("e aid : " + id2 +", "+ e.toString());
      }
      udclient.logout(uid, id2);
      logger.info("logout logout " + id2);
    }
  }
  
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    
    int tn = 1000;
    UDTest db = new UDTest();
    ExecutorService es = Executors.newFixedThreadPool(tn);
     System.out.println("[Submitting tasks...]");
        for (int i = 0; i < tn; i++) {
          es.execute(db.new UDThread(i));
        }
        System.out.println("[Finish submitting!]");
   
        es.shutdown();
        System.out.println("[Shutdown]");


  }

}
