package org.kisti.htc.dbmanager.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.kisti.htc.dbmanager.server.Database;


public final class Client {
	private static String DBManagerURL;
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;


    static {
	try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

			DBManagerURL = prop.getProperty("DBManager.Address");

			if(prop.getProperty("SSL.Authentication").equals("true")){
				SSL = true;
				DBManagerURL = DBManagerURL.replace("http", "https");
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}
			
			System.out.println("DBManagerURL: "+DBManagerURL);
			
		} catch (Exception e) {
			System.out.println("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
    }   

    public static void main(String args[]) throws Exception {
        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
        factory.setServiceClass(Database.class);
        if (args != null && args.length > 0 && !"".equals(args[0])) {
            factory.setAddress(args[0]);
        } else {
            factory.setAddress(DBManagerURL);
        }
           
        factory.getServiceFactory().setDataBinding(new AegisDatabinding());
        Database client = (Database)factory.create();

        if(SSL){
        	setupTLS(client);
        }
        
        
        System.out.println(client.addAgent());
//        List<Integer> a =client.getAgentSubmittedZombieList();
//        System.out.println("1");
//        for(Integer b : a){
//          System.out.println("2");
//          System.out.println(b);
//        }
//        System.out.println();
//        
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a"); 
//        System.out.println(sdf.format(new Date()).toString());
//        int i =0;
//        int j =10;
//        long sum1 =0 ;
//        long sum2 = 0;
//        for(i=0;i<j;i++){
//          long time1 = new Date().getTime();
//          System.out.println(client.getMetaJobProgress(236));
//          long time2 = new Date().getTime();
//          sum1 += (time2-time1);
//          sum2 = (time2-time1);
//          System.out.println("elapsed time :" + sum2);
//          
//        }
//        System.out.println("averge time : " + sum1/j);
//        System.out.println(client.getNumAgent(Constant.AGENT_STATUS_SUB));
//        System.out.println(client.getInteger_T(10));
//        System.out.println(client.getInteger(1));
//          client.reEnqueueJob(974832);
//        System.out.println("after getInteger");
//        client.addAgent();
//        client.reportSubmitError(1, 70, null, null, "b");
//        int i = 500;
//        ExecutorService executorService = Executors.newFixedThreadPool(i);
//        for(int j=0;j<i ; j++ ){
//        	executorService.execute(c.new TestThread(client));
//        }
        
//        System.exit(0);

        
//        Thread td = c.new TestThread(client);
//        td.start();
        
        
  
//        System.out.println(client.addAgent());
//        System.out.println(client.getMetaJobProgress(2));
    }     
    
  /*  private class TestThread extends Thread {
    	
    	Database client;
    	
    	public TestThread(Database client){
    		this.client = client;
    	}
    	
    	@Override
    	public void run(){
//    		while(true){
    			System.out.println("thread :" + currentThread().getName() + ", num :" + client.getInteger_T(new Random().nextInt(10)));
//    		}
    	}
    }*/
    
    private static void setupTLS(Database port) throws FileNotFoundException, IOException, GeneralSecurityException {

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
