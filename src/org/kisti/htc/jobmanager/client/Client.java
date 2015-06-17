package org.kisti.htc.jobmanager.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.kisti.htc.jobmanager.server.JobManager;



public final class Client {
	private static String JobManagerURL;
	private static String SSLClientPath;
	private static String SSLClientPassword;
	private static String SSLCAPath;
	private static String SSLCAPassword;
	private static boolean SSL = false;


    static {
    	try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("conf/HTCaaS_Client.conf"));

			JobManagerURL = prop.getProperty("JobManager.Address");
			
			if(prop.getProperty("SSL.Authentication").equals("true")){
				SSL = true;
				JobManagerURL = JobManagerURL.replace("http", "https");
				SSLClientPath = prop.getProperty("SSL.Client.Keystore.Path");
				SSLClientPassword = prop.getProperty("SSL.Client.Keystore.Password");
				SSLCAPath = prop.getProperty("SSL.CA.Keystore.Path");
				SSLCAPassword = prop.getProperty("SSL.CA.Keystore.Password");
			}
			
			System.out.println("JobManagerURL: "+JobManagerURL);
			
		} catch (Exception e) {
			System.out.println("Failed to load config file: " + e.getMessage());
			System.exit(1);
		}
    }   

    public static void main(String args[]) throws Exception {
        ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
        factory.setServiceClass(JobManager.class);
        if (args != null && args.length > 0 && !"".equals(args[0])) {
            factory.setAddress(args[0]);
        } else {
            factory.setAddress(JobManagerURL);
        }
           
        factory.getServiceFactory().setDataBinding(new AegisDatabinding());
        JobManager client = (JobManager)factory.create();
        
        if(SSL){
        	setupTLS(client);
        }
        
//        System.out.println(client.getMetaJobProgress("p258rsw", 1));
//        System.out.println(client.cancelMetaJob("p143ksw", 46));
//        String user1 = "p260ksy";
//        String user1 = "p143ksw";
//          String user1 = "p312kjs";
//    	String filename1 ="jsdl/autodock3_p312kjs.jsdl";
//    	
        String user1 = "p258rsw";
        String filename ="jsdl/sleep_sweep.jsdl";
//        String filename ="jsdl/autodock3_p258rsw.jsdl";
    	StringBuffer sb = new StringBuffer();
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(filename));
    		String line = "";
    		while ((line = br.readLine()) != null) {
    			sb.append(line + "\n");
    		}
    	} catch (FileNotFoundException e1) {
    		// TODO Auto-generated catch block
    		System.exit(1);
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		System.exit(1);
    	}
    	
    	
    	client.submitMetaJob(user1, sb.toString(), 0, "test", "test");
    	
//    	client.cancelMetaJob(user1, 43);
    	
//    	Thread.sleep(1000*60*10);
        
//        List<String> user = new ArrayList<String>();
//        String user1 = "p258rsw";
//        String user2 = "p260ksy";
//        String user3 = "p312kjs";
//        String user4 = "p331ksk";
//        user.add(user1);
//        user.add(user2);
//        user.add(user3);
//        user.add(user4);
//        
//        List<String> filename = new ArrayList<String>();
//        String filename1 ="jsdl/autodock3_p258rsw.jsdl";
//        String filename2 ="jsdl/autodock3_p260ksy.jsdl";
//        String filename3 ="jsdl/autodock3_p312kjs.jsdl";
//        String filename4 ="jsdl/autodock3_p331ksk.jsdl";
//        filename.add(filename1);
//        filename.add(filename2);
//        filename.add(filename3);
//        filename.add(filename4);
//        
//        for(int i=0; i<4 ; i++){
//        	
//        	
//        	StringBuffer sb = new StringBuffer();
//        	try {
//        		BufferedReader br = new BufferedReader(new FileReader(filename.get(i)));
//        		String line = "";
//        		while ((line = br.readLine()) != null) {
//        			sb.append(line + "\n");
//        		}
//        	} catch (FileNotFoundException e1) {
//        		// TODO Auto-generated catch block
//        		System.exit(1);
//        	} catch (IOException e) {
//        		// TODO Auto-generated catch block
//        		System.exit(1);
//        	}
//        	
//        	
//        	client.submitMetaJob(user.get(i), sb.toString(), 0, "autodock3", "test");
//        	
//        	Thread.sleep(1000*60*10);
//        }
//		client.cancelMetaJob("p258rsw", 124);
////		System.out.println(client.resubmitFailedSubJob("seungwoo", 1));
//                        
    }       
    
    private static void setupTLS(JobManager port) throws FileNotFoundException, IOException, GeneralSecurityException {

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
