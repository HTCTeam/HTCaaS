
package org.kisti.htc.acmanager.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.util.List;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;

import org.kisti.htc.acmanager.server.ACManager;
import org.kisti.htc.acmanager.client.ACManagerClient;


public final class Client {

  private static String ACManagerURL;
  private static String SSLClientPath;
  private static String SSLClientPassword;
  private static String SSLCAPath;
  private static String SSLCAPassword;
  private static boolean SSL = false;

  
  static {
    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

      ACManagerURL = prop.getProperty("ACManager.Address");
      
      if(prop.getProperty("SSL.Authentication").equals("true")){
        SSL = true;
        ACManagerURL = ACManagerURL.replace("http", "https");
        SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
        SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
        SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
        SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
      }
      
      System.out.println("ACManagerURL: "+ ACManagerURL);
      
    } catch (Exception e) {
      System.out.println("Failed to load config file: " + e.getMessage());
      System.exit(1);
    }
  }   
    
  public static void main(String[] args) throws Exception {

    ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
    factory.setServiceClass(ACManager.class);
    if (args != null && args.length > 0 && !"".equals(args[0])) {
      factory.setAddress(args[0]);
    } else {
      factory.setAddress(ACManagerURL);
    }
        
    factory.getServiceFactory().setDataBinding(new AegisDatabinding());
    ACManager ac = (ACManager)factory.create();
        
    if (SSL) {
      setupTLS(ac);
    }
        
    ACManagerClient ac_client = new ACManagerClientImpl();
        
    String userid = "p258rsw";
    String pw = "kisti4001!@#";
//    String userid = "p260ksy";
//    String pw = "rlatjdud2!";
    String cert = "p258rsw.der";
    String certpw = "shtmddn";
      
    String otp = "724953";
//    System.out.println("flag :" + ac.checkUserID(userid));
//    System.out.println("result : " + ac.Login(userid, pw, ac.checkUserID(userid), otp));
    System.out.println(ac.checkPLSIUserValid(userid, pw, otp));
    //System.out.println("login result:" + ac_client.HTCaasLogin(userid, pw, cert, certpw));
    //String info = ac.getPLSICertInfo(userid, pw);
    //System.out.println(ac.)

  }
  
  private static void setupTLS(ACManager port) throws FileNotFoundException, IOException, GeneralSecurityException {

      HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();

    TLSClientParameters tlsCP = new TLSClientParameters();
    KeyStore keyStore = KeyStore.getInstance("JKS");
    String keyStoreLoc = SSLClientPath;
    keyStore.load(new FileInputStream(keyStoreLoc), SSLClientPassword.toCharArray());
    KeyManager[] myKeyManagers = getKeyManagers(keyStore, SSLClientPassword);
    tlsCP.setKeyManagers(myKeyManagers);

    KeyStore trustStore = KeyStore.getInstance("JKS");
    String trustStoreLoc = SSLCAPath;
    trustStore.load(new FileInputStream(trustStoreLoc), SSLCAPassword.toCharArray());
    TrustManager[] myTrustStoreKeyManagers = getTrustManagers(trustStore);
    tlsCP.setTrustManagers(myTrustStoreKeyManagers);

    // The following is not recommended and would not be done in a
    // prodcution environment,
    // this is just for illustrative purpose
    tlsCP.setDisableCNCheck(true);
    tlsCP.setSecureSocketProtocol("SSL");   // addme
    
    httpConduit.setTlsClientParameters(tlsCP);

  }

  private static TrustManager[] getTrustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
    fac.init(trustStore);
    return fac.getTrustManagers();
  }

  private static KeyManager[] getKeyManagers(KeyStore keyStore, String keyPassword) throws GeneralSecurityException, IOException {
    String alg = KeyManagerFactory.getDefaultAlgorithm();
    char[] keyPass = keyPassword != null ? keyPassword.toCharArray() : null;
    KeyManagerFactory fac = KeyManagerFactory.getInstance(alg);
    fac.init(keyStore, keyPass);
    return fac.getKeyManagers();
  }

}
